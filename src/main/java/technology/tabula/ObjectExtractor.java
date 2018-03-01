package technology.tabula;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    public ObjectExtractor(PDDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    private List<PDAnnotation> getAnnotations(PDPage p) throws IOException {
        List<PDAnnotation> annotations = p.getAnnotations();
        return annotations;
    }

    protected Page extractPage(Integer pageNumber) throws IOException {

        if (pageNumber > this.pdfDocument.getNumberOfPages() || pageNumber < 1) {
            throw new java.lang.IndexOutOfBoundsException(
                    "Page number does not exist");
        }

        PDPage p = this.pdfDocument.getPage(pageNumber - 1);

        ObjectExtractorStreamEngine se = new ObjectExtractorStreamEngine(p);
        se.processPage(p);


        TextStripper pdfTextStripper = new TextStripper(this.pdfDocument, pageNumber);
        pdfTextStripper.process();

        Utils.sort(pdfTextStripper.textElements, Rectangle.ILL_DEFINED_ORDER);

        float w, h;
        int pageRotation = p.getRotation();
        if (Math.abs(pageRotation) == 90 || Math.abs(pageRotation) == 270) {
            w = p.getCropBox().getHeight();
            h = p.getCropBox().getWidth();
        } else {
            w = p.getCropBox().getWidth();
            h = p.getCropBox().getHeight();
        }

        Page page = new Page(0, 0, w, h, pageRotation, pageNumber, p, pdfTextStripper.textElements,
                se.rulings, pdfTextStripper.minCharWidth, pdfTextStripper.minCharHeight, pdfTextStripper.spatialIndex);

        for (PDAnnotation ann: this.getAnnotations(p)) {
            System.out.println(ann.getContents());
            System.out.println(ann.getRectangle());
            System.out.println(ann.getRectangle().toGeneralPath().createTransformedShape(se.getPageTransform()).getBounds2D());
        }

        return page;
    }

    public PageIterator extract(Iterable<Integer> pages) {
        return new PageIterator(this, pages);
    }

    public PageIterator extract() {
        return extract(Utils.range(1, this.pdfDocument.getNumberOfPages() + 1));
    }

    public Page extract(int pageNumber) {
        return extract(Utils.range(pageNumber, pageNumber + 1)).next();
    }

    public void close() throws IOException {
        this.pdfDocument.close();
    }
}
