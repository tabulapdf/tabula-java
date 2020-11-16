package technology.tabula;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    public ObjectExtractor(PDDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    protected Page extractPage(Integer pageNumber) throws IOException {
        if (pageNumber > pdfDocument.getNumberOfPages() || pageNumber < 1) {
            throw new java.lang.IndexOutOfBoundsException("Page number does not exist.");
        }
        PDPage page = pdfDocument.getPage(pageNumber - 1);

        ObjectExtractorStreamEngine streamEngine = new ObjectExtractorStreamEngine(page);
        streamEngine.processPage(page);

        TextStripper textStripper = new TextStripper(this.pdfDocument, pageNumber);
        textStripper.process();

        Utils.sort(textStripper.textElements, Rectangle.ILL_DEFINED_ORDER);

        float w, h;
        int rotation = page.getRotation();
        if (Math.abs(rotation) == 90 || Math.abs(rotation) == 270) {
            w = page.getCropBox().getHeight();
            h = page.getCropBox().getWidth();
        } else {
            w = page.getCropBox().getWidth();
            h = page.getCropBox().getHeight();
        }

        return new Page(0, 0, w, h, rotation, pageNumber, page, pdfDocument, textStripper.textElements,
                streamEngine.rulings, textStripper.minCharWidth, textStripper.minCharHeight, textStripper.spatialIndex);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public PageIterator extract(Iterable<Integer> pages) {
        return new PageIterator(this, pages);
    }

    public PageIterator extract() {
        return extract(Utils.range(1, pdfDocument.getNumberOfPages() + 1));
    }

    public Page extract(int pageNumber) {
        return extract(Utils.range(pageNumber, pageNumber + 1)).next();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public void close() throws IOException {
        pdfDocument.close();
    }

}
