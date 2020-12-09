package technology.tabula;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    private final List<String> tableNames;

    public ObjectExtractor(PDDocument pdfDocument, List<String> tableNames) {
        this.pdfDocument = pdfDocument;
        this.tableNames = tableNames;
    }

    protected Page extractPage(Integer pageNumber) throws IOException {

        if (pageNumber > this.pdfDocument.getNumberOfPages() || pageNumber < 1) {
            throw new java.lang.IndexOutOfBoundsException(
                    "Page number does not exist");
        }

        PDPage p = this.pdfDocument.getPage(pageNumber - 1);
        TextStripper pdfTextStripper = new TextStripper(this.pdfDocument, pageNumber);
        pdfTextStripper.process();
        String tableName = "";
        //TODO 判断表名是否存在
        if (tableNames != null){
            //采用文本包含方式判断表名,后续需优化
            tableName = Utils.findTableName(tableNames, pdfTextStripper.getContent());
            if ("".equals(tableName)) {
                return null;
            }
        }
        Utils.sort(pdfTextStripper.textElements, Rectangle.ILL_DEFINED_ORDER);

        ObjectExtractorStreamEngine se = new ObjectExtractorStreamEngine(p);
        se.processPage(p);

        float w, h;
        int pageRotation = p.getRotation();
        if (Math.abs(pageRotation) == 90 || Math.abs(pageRotation) == 270) {
            w = p.getCropBox().getHeight();
            h = p.getCropBox().getWidth();
        } else {
            w = p.getCropBox().getWidth();
            h = p.getCropBox().getHeight();
        }

        return new Page(0, 0, w, h, pageRotation, pageNumber, p, this.pdfDocument, pdfTextStripper.textElements,
                se.rulings, pdfTextStripper.minCharWidth, pdfTextStripper.minCharHeight, pdfTextStripper.spatialIndex, pdfTextStripper.getContent(), tableName);
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
