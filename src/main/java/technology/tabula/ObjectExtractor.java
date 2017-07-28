package technology.tabula;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class ObjectExtractor {

    private final PDDocument pdfDocument;

    public ObjectExtractor(PDDocument pdfDocument) throws IOException {
        this.pdfDocument = pdfDocument;
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

        Utils.sort(pdfTextStripper.textElements);

        float w, h;
        int pageRotation = p.getRotation();
        if (Math.abs(pageRotation) == 90 || Math.abs(pageRotation) == 270) {
            w = p.getCropBox().getHeight();
            h = p.getCropBox().getWidth();
        } else {
            w = p.getCropBox().getWidth();
            h = p.getCropBox().getHeight();
        }

        return new Page(0, 0, w, h, pageRotation, pageNumber, p, pdfTextStripper.textElements,
                se.rulings, pdfTextStripper.minCharWidth, pdfTextStripper.minCharHeight, pdfTextStripper.spatialIndex);
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

    class TextStripper extends PDFTextStripper {
        private static final String NBSP = "\u00A0";
        private PDDocument document;
        public ArrayList<TextElement> textElements;
        public RectangleSpatialIndex<TextElement> spatialIndex;
        public float minCharWidth = Float.MAX_VALUE;;
        public float minCharHeight = Float.MAX_VALUE;;

        public TextStripper(PDDocument document, int pageNumber) throws IOException {
            super();
            this.document = document;
            this.setStartPage(pageNumber);
            this.setEndPage(pageNumber);
            this.textElements = new ArrayList<TextElement>();
            this.spatialIndex = new RectangleSpatialIndex<TextElement>();
        }

        public void process() throws IOException {
            this.getText(this.document);
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException
        {
            for (TextPosition textPosition: textPositions)
            {
                if (textPosition == null) {
                    continue;
                }

                String c = textPosition.getUnicode();

                // if c not printable, return
                if (!isPrintable(c)) {
                    continue;
                }

                Float h = textPosition.getHeightDir();

                if (c.equals(NBSP)) { // replace non-breaking space for space
                    c = " ";
                }

                float wos = textPosition.getWidthOfSpace();

                TextElement te = new TextElement(Utils.round(textPosition.getYDirAdj() - h, 2),
                        Utils.round(textPosition.getXDirAdj(), 2), Utils.round(textPosition.getWidthDirAdj(), 2),
                        Utils.round(textPosition.getHeightDir(), 2), textPosition.getFont(), textPosition.getFontSize(), c,
                        // workaround a possible bug in PDFBox:
                        // https://issues.apache.org/jira/browse/PDFBOX-1755
                        wos, textPosition.getDir());

                this.minCharWidth = (float) Math.min(this.minCharWidth, te.getWidth());
                this.minCharHeight = (float) Math.min(this.minCharHeight, te.getHeight());

                this.spatialIndex.add(te);
                this.textElements.add(te);
            }
        }

        private boolean isPrintable(String s) {
            Character c;
            Character.UnicodeBlock block;
            boolean printable = false;
            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                block = Character.UnicodeBlock.of(c);
                printable |= !Character.isISOControl(c) && block != null && block != Character.UnicodeBlock.SPECIALS;
            }
            return printable;
        }
    }

}
