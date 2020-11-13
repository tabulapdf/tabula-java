package technology.tabula;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import technology.tabula.page.Page;
import technology.tabula.page.PageIterator;
import technology.tabula.text.TextStripper;

import java.io.IOException;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    public ObjectExtractor(PDDocument pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    public Page extractPage(Integer pageNumber) throws IOException {
        if (pageNumber > pdfDocument.getNumberOfPages() || pageNumber < 1) {
            throw new java.lang.IndexOutOfBoundsException("Page number does not exist");
        }
        PDPage page = pdfDocument.getPage(pageNumber - 1);

        ObjectExtractorStreamEngine streamEngine = new ObjectExtractorStreamEngine(page);
        streamEngine.processPage(page);

        TextStripper textStripper = new TextStripper(pdfDocument, pageNumber);
        textStripper.process();

        Utils.sort(textStripper.textElements, Rectangle.ILL_DEFINED_ORDER);

        int rotation = page.getRotation();
        float width = getPageWidth(page, rotation);
        float height = getPageHeight(page, rotation);

        return new Page(0, 0, width, height, rotation, pageNumber, page, pdfDocument, textStripper.textElements,
                streamEngine.rulings, textStripper.minCharWidth, textStripper.minCharHeight, textStripper.spatialIndex);
    }

    private float getPageWidth(PDPage page, int rotation) {
        if (Math.abs(rotation) == 90 || Math.abs(rotation) == 270) {
            return page.getCropBox().getHeight();
        }
        return page.getCropBox().getWidth();
    }

    private float getPageHeight(PDPage page, int rotation) {
        if (Math.abs(rotation) == 90 || Math.abs(rotation) == 270) {
            return page.getCropBox().getWidth();
        }
        return page.getCropBox().getHeight();
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
