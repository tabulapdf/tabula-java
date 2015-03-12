package org.nerdpower.tabula;

import java.awt.Image;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.util.TextPosition;

public class ObjectExtractor extends org.apache.pdfbox.pdfviewer.PageDrawer {

    class PointComparator implements Comparator<Point2D> {
        @Override
        public int compare(Point2D o1, Point2D o2) {
            float o1X = Utils.round(o1.getX(), 2);
            float o1Y = Utils.round(o1.getY(), 2);
            float o2X = Utils.round(o2.getX(), 2);
            float o2Y = Utils.round(o2.getY(), 2);

            if (o1Y > o2Y)
                return 1;
            if (o1Y < o2Y)
                return -1;
            if (o1X > o2X)
                return 1;
            if (o1X < o2X)
                return -1;
            return 0;
        }
    }

    private static final char[] spaceLikeChars = { ' ', '-', '1', 'i' };
    private static final String NBSP = "\u00A0";

    private float minCharWidth = Float.MAX_VALUE,
            minCharHeight = Float.MAX_VALUE;
    private List<TextElement> characters;
    private List<Ruling> rulings;
    private RectangleSpatialIndex<TextElement> spatialIndex;
    private AffineTransform pageTransform;
    private Shape clippingPath;
    public List<Shape> clippingPaths = new ArrayList<Shape>();
    private boolean debugClippingPaths = false;
    private Rectangle2D transformedClippingPathBounds;
    private Shape transformedClippingPath;
    private boolean extractRulingLines = true;
    private final PDDocument pdf_document;
    protected List pdf_document_pages;
    private PDPage page;


    public ObjectExtractor(PDDocument pdf_document) throws IOException {
        this(pdf_document, null);
    }

    public ObjectExtractor(PDDocument pdf_document, String password)
            throws IOException {
        super();
        if (pdf_document.isEncrypted()) {
            try {
                pdf_document
                        .openProtection(new StandardDecryptionMaterial(password));
            } catch (BadSecurityHandlerException e) {
                // TODO Auto-generated catch block
                throw new IOException("BadSecurityHandler");
            } catch (CryptographyException e) {
                throw new IOException("Document is encrypted");
            }
        }
        this.pdf_document = pdf_document;
        this.pdf_document_pages = this.pdf_document.getDocumentCatalog()
                .getAllPages();

    }


    protected Page extractPage(Integer page_number) throws IOException {

        if (page_number - 1 > this.pdf_document_pages.size() || page_number < 1) {
            throw new java.lang.IndexOutOfBoundsException(
                    "Page number does not exist");
        }

        PDPage p = (PDPage) this.pdf_document_pages.get(page_number - 1);
        PDStream contents = p.getContents();

        if (contents == null) {
            return null;
        }
        this.clear();

        this.drawPage(p);

        Utils.sort(this.characters);

        float w, h;
        int pageRotation = p.findRotation();
        if (Math.abs(pageRotation) == 90 || Math.abs(pageRotation) == 270) {
            w = p.findCropBox().getHeight();
            h = p.findCropBox().getWidth();
        }
        else {
            w = p.findCropBox().getWidth();
            h = p.findCropBox().getHeight();
        }

        return new Page(0, 0, w, h, pageRotation, page_number, this.characters,
                this.getRulings(), this.minCharWidth, this.minCharHeight,
                this.spatialIndex);
    }

    public PageIterator extract(Iterable<Integer> pages) {
        return new PageIterator(this, pages);
    }

    public PageIterator extract() {
        return extract(Utils.range(1, this.pdf_document_pages.size() + 1));
    }

    public Page extract(int pageNumber) {
        return extract(Utils.range(pageNumber, pageNumber + 1)).next();
    }

    public void close() throws IOException {
        this.pdf_document.close();
    }

    public void drawPage(PDPage p) throws IOException {
        page = p;
        PDStream contents = p.getContents();
        if (contents != null) {
            ensurePageSize();
            this.processStream(p, p.findResources(), contents.getStream());/*,
                    p.findCropBox(), p.findRotation());*/
        }
    }

    private void ensurePageSize() {
        if (this.pageSize == null && this.page != null) {
            PDRectangle mediaBox = this.page.findMediaBox();
            this.pageSize = mediaBox == null ? null : mediaBox
                    .createDimension();
        }
    }

    private void clear() {
        this.characters = new ArrayList<TextElement>();
        this.rulings = new ArrayList<Ruling>();
        this.pageTransform = null;
        this.spatialIndex = new RectangleSpatialIndex<TextElement>();
        this.minCharWidth = Float.MAX_VALUE;
        this.minCharHeight = Float.MAX_VALUE;
    }

    @Override
    public void drawImage(Image awtImage, AffineTransform at) {
        // we just ignore images (for now)
    }

    public void strokeOrFillPath(boolean isFill) {
        GeneralPath path = this.getLinePath();

        if (!this.extractRulingLines) {
            this.getLinePath().reset();
            return;
        }

        PathIterator pi = path.getPathIterator(this.getPageTransform());
        float[] c = new float[6];
        int currentSegment;

        // skip paths whose first operation is not a MOVETO
        // or contains operations other than LINETO, MOVETO or CLOSE
        if ((pi.currentSegment(c) != PathIterator.SEG_MOVETO)) {
            path.reset();
            return;
        }
        pi.next();
        while (!pi.isDone()) {
            currentSegment = pi.currentSegment(c);
            if (currentSegment != PathIterator.SEG_LINETO
                    && currentSegment != PathIterator.SEG_CLOSE
                    && currentSegment != PathIterator.SEG_MOVETO) {
                path.reset();
                return;
            }
            pi.next();
        }

        // TODO: how to implement color filter?

        // skip the first path operation and save it as the starting position
        float[] first = new float[6];
        pi = path.getPathIterator(this.getPageTransform());
        pi.currentSegment(first);
        // last move
        Point2D.Float start_pos = new Point2D.Float(Utils.round(first[0], 2), Utils.round(first[1], 2));
        Point2D.Float last_move = start_pos;
        Point2D.Float end_pos = null;
        Line2D.Float line;
        PointComparator pc = new PointComparator();

        while (!pi.isDone()) {
            pi.next();
            currentSegment = pi.currentSegment(c);
            switch (currentSegment) {
            case PathIterator.SEG_LINETO:
                end_pos = new Point2D.Float(c[0], c[1]);

                line = pc.compare(start_pos, end_pos) == -1 ? new Line2D.Float(
                        start_pos, end_pos) : new Line2D.Float(end_pos,
                        start_pos);

                if (line.intersects(this.currentClippingPath())) {
                    Ruling r = new Ruling(line.getP1(), line.getP2())
                            .intersect(this.currentClippingPath());

                    if (r.length() > 0.01) {
                        this.rulings.add(r);
                    }
                }
                break;
            case PathIterator.SEG_MOVETO:
                last_move = new Point2D.Float(c[0], c[1]);
                end_pos = last_move;
                break;
            case PathIterator.SEG_CLOSE:
                // according to PathIterator docs:
                // "the preceding subpath should be closed by appending a line
                // segment
                // back to the point corresponding to the most recent
                // SEG_MOVETO."
                line = pc.compare(end_pos, last_move) == -1 ? new Line2D.Float(
                        end_pos, last_move) : new Line2D.Float(last_move,
                        end_pos);

                if (line.intersects(this.currentClippingPath())) {
                    Ruling r = new Ruling(line.getP1(), line.getP2())
                            .intersect(this.currentClippingPath());

                    if (r.length() > 0.01) {
                        this.rulings.add(r);
                    }
                }
                break;
            }
            start_pos = end_pos;
        }
        path.reset();
    }

    @Override
    public void strokePath() throws IOException {
        this.strokeOrFillPath(false);
    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        //
        // float[] color_comps =
        // this.getGraphicsState().getNonStrokingColor().getJavaColor().getRGBColorComponents(null);
        float[] color = this.getGraphicsState().getNonStrokingColor().getJavaColor().getComponents(null);
        // TODO use color_comps as filter_by_color
        this.strokeOrFillPath(true);
    }

    private float currentSpaceWidth() {
        PDGraphicsState gs = this.getGraphicsState();
        PDTextState ts = gs.getTextState();
        PDFont font = ts.getFont();
        float fontSizeText = ts.getFontSize();
        float horizontalScalingText = ts.getHorizontalScalingPercent() / 100.0f;
        float spaceWidthText = 1000;

        if (font instanceof PDType3Font) {
            // TODO WHAT?
        }

        for (int i = 0; i < spaceLikeChars.length; i++) {
            spaceWidthText = font.getFontWidth(spaceLikeChars[i]);
            if (spaceWidthText > 0)
                break;
        }

        float ctm00 = gs.getCurrentTransformationMatrix().getValue(0, 0);

        return (float) ((spaceWidthText / 1000.0) * fontSizeText
                * horizontalScalingText * (ctm00 == 0 ? 1 : ctm00));
    }

    @Override
    protected void processTextPosition(TextPosition textPosition) {
        String c = textPosition.getCharacter();

        // if c not printable, return
        if (!isPrintable(c)) {
            return;
        }

        Float h = textPosition.getHeightDir();

        if (c.equals(NBSP)) { // replace non-breaking space for space
            c = " ";
        }

        float wos = textPosition.getWidthOfSpace();

        TextElement te = new TextElement(
                Utils.round(textPosition.getYDirAdj() - h, 2),
                Utils.round(textPosition.getXDirAdj(), 2),
                Utils.round(textPosition.getWidthDirAdj(), 2),
                Utils.round(textPosition.getHeightDir(), 2),
                textPosition.getFont(),
                textPosition.getFontSize(),
                c,
                // workaround a possible bug in PDFBox:
                // https://issues.apache.org/jira/browse/PDFBOX-1755
                (Float.isNaN(wos) || wos == 0) ? this.currentSpaceWidth() : wos,
                textPosition.getDir());

       if (this.currentClippingPath().intersects(te)) {

            this.minCharWidth = (float) Math.min(this.minCharWidth, te.getWidth());
            this.minCharHeight = (float) Math.min(this.minCharHeight, te.getHeight());

            this.spatialIndex.add(te);
            this.characters.add(te);
        }

        if (this.isDebugClippingPaths() && !this.clippingPaths.contains(this.currentClippingPath())) {
            this.clippingPaths.add(this.currentClippingPath());
        }

    }

    public float getMinCharWidth() {
        return minCharWidth;
    }

    public float getMinCharHeight() {
        return minCharHeight;
    }

    public AffineTransform getPageTransform() {

        if (this.pageTransform != null) {
            return this.pageTransform;
        }

        PDRectangle cb = page.findCropBox();
        int rotation = Math.abs(page.findRotation());

        this.pageTransform = new AffineTransform();

        if (rotation == 90 || rotation == 270) {
            this.pageTransform = AffineTransform.getRotateInstance(rotation * (Math.PI / 180.0), 0, 0);
            this.pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
            this.pageTransform.concatenate(AffineTransform.getTranslateInstance(0, cb.getHeight()));
            this.pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
        }
        return this.pageTransform;
    }

    //public Rectangle2D currentClippingPath() {
    public Rectangle2D currentClippingPath() {
        // Shape cp = this.getGraphicsState().getCurrentClippingPath();
        // if (cp == this.clippingPath) {
        // return this.transformedClippingPathBounds;
        // }

        this.clippingPath = this.getGraphicsState().getCurrentClippingPath();
        this.transformedClippingPath = this.getPageTransform()
                .createTransformedShape(this.clippingPath);
        this.transformedClippingPathBounds = this.transformedClippingPath
                .getBounds2D();

        return this.transformedClippingPathBounds;
    }

    public boolean isExtractRulingLines() {
        return extractRulingLines;
    }

    public void setExtractRulingLines(boolean extractRulingLines) {
        this.extractRulingLines = extractRulingLines;
    }

    public List<Ruling> getRulings() {
        return rulings;
    }

    public List<TextElement> getCharacters() {
        return characters;
    }

    private static boolean isPrintable(String s) {
        Character c = s.charAt(0);
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED
                && block != null && block != Character.UnicodeBlock.SPECIALS;
    }

    public boolean isDebugClippingPaths() {
        return debugClippingPaths;
    }

    public void setDebugClippingPaths(boolean debugClippingPaths) {
        this.debugClippingPaths = debugClippingPaths;
    }

    public int getPageCount() {
        return this.pdf_document_pages.size();
    }

}
