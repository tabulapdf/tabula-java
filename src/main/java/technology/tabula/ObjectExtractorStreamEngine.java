package technology.tabula;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ObjectExtractorStreamEngine extends PDFGraphicsStreamEngine {

    private static final String NBSP = "\u00A0";

    protected float minCharWidth;
    protected float minCharHeight;
    protected List<TextElement> characters;
    protected List<Ruling> rulings;
    protected RectangleSpatialIndex<TextElement> spatialIndex;
    private AffineTransform pageTransform;
    private boolean debugClippingPaths;
    private boolean extractRulingLines = true;
    private Logger log;
    private int clipWindingRule = -1;
    private GeneralPath currentPath = new GeneralPath();
    public List<Shape> clippingPaths;
    private int pageRotation;
    private PDRectangle pageSize;

    private Matrix translateMatrix;
    private GlyphList glyphList;

    protected ObjectExtractorStreamEngine(PDPage page) {
        super(page);

        this.log = LoggerFactory.getLogger(ObjectExtractorStreamEngine.class);

        this.characters = new ArrayList<TextElement>();
        this.rulings = new ArrayList<Ruling>();
        this.pageTransform = null;
        this.spatialIndex = new RectangleSpatialIndex<TextElement>();
        this.minCharWidth = Float.MAX_VALUE;
        this.minCharHeight = Float.MAX_VALUE;
        this.pageRotation = page.getRotation();
        this.pageSize = page.getCropBox();

        // calculate page transform
        PDRectangle cb = this.getPage().getCropBox();
        int rotation = this.getPage().getRotation();

        this.pageTransform = new AffineTransform();

        if (Math.abs(rotation) == 90 || Math.abs(rotation) == 270) {
            this.pageTransform = AffineTransform.getRotateInstance(rotation * (Math.PI / 180.0), 0, 0);
            this.pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
        } else {
            this.pageTransform.concatenate(AffineTransform.getTranslateInstance(0, cb.getHeight()));
            this.pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
        }

        // load additional glyph list for Unicode mapping
        String path = "org/apache/pdfbox/resources/glyphlist/additional.txt";
        InputStream input = GlyphList.class.getClassLoader().getResourceAsStream(path);
        this.glyphList = null;
        try {
            this.glyphList = new GlyphList(GlyphList.getAdobeGlyphList(), input);
        } catch (IOException e) {
            this.log.error("Error loading glyph list", e);
        }
    }

    @Override
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode, Vector displacement)
            throws IOException {

        TextPosition textPosition = getTextPosition(textRenderingMatrix, font, code, unicode, displacement);

        if (textPosition != null) {

            String c = textPosition.getUnicode();

            // if c not printable, return
            if (!isPrintable(c)) {
                return;
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

    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        currentPath.moveTo((float) p0.getX(), (float) p0.getY());
        currentPath.lineTo((float) p1.getX(), (float) p1.getY());
        currentPath.lineTo((float) p2.getX(), (float) p2.getY());
        currentPath.lineTo((float) p3.getX(), (float) p3.getY());

        currentPath.closePath();
    }

    @Override
    public void clip(int windingRule) throws IOException {
        // the clipping path will not be updated until the succeeding painting
        // operator is called
        clipWindingRule = windingRule;
    }

    @Override
    public void closePath() throws IOException {
        currentPath.closePath();
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        currentPath.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void drawImage(PDImage arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void endPath() throws IOException {
        if (clipWindingRule != -1) {
            currentPath.setWindingRule(clipWindingRule);
            getGraphicsState().intersectClippingPath(currentPath);
            clipWindingRule = -1;
        }
        currentPath.reset();
    }

    @Override
    public void fillAndStrokePath(int arg0) throws IOException {
        strokeOrFillPath(true);
    }

    @Override
    public void fillPath(int arg0) throws IOException {
        strokeOrFillPath(true);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return currentPath.getCurrentPoint();
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        currentPath.lineTo(x, y);
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        currentPath.moveTo(x, y);
    }

    @Override
    public void shadingFill(COSName arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void strokePath() throws IOException {
        strokeOrFillPath(false);
    }

    private void strokeOrFillPath(boolean isFill) {
        GeneralPath path = this.currentPath;

        if (!this.extractRulingLines) {
            this.currentPath.reset();
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
            if (currentSegment != PathIterator.SEG_LINETO && currentSegment != PathIterator.SEG_CLOSE
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
            // This can be the last segment, when pi.isDone, but we need to
            // process it
            // otherwise us-017.pdf fails the last value.
            try {
                currentSegment = pi.currentSegment(c);
            } catch (IndexOutOfBoundsException ex) {
                continue;
            }
            switch (currentSegment) {
                case PathIterator.SEG_LINETO:
                    end_pos = new Point2D.Float(c[0], c[1]);

                    line = pc.compare(start_pos, end_pos) == -1 ? new Line2D.Float(start_pos, end_pos)
                            : new Line2D.Float(end_pos, start_pos);

                    if (line.intersects(this.currentClippingPath())) {
                        Ruling r = new Ruling(line.getP1(), line.getP2()).intersect(this.currentClippingPath());

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
                    line = pc.compare(end_pos, last_move) == -1 ? new Line2D.Float(end_pos, last_move)
                            : new Line2D.Float(last_move, end_pos);

                    if (line.intersects(this.currentClippingPath())) {
                        Ruling r = new Ruling(line.getP1(), line.getP2()).intersect(this.currentClippingPath());

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

    public AffineTransform getPageTransform() {
        return this.pageTransform;
    }

    public Rectangle2D currentClippingPath() {
        Shape clippingPath = this.getGraphicsState().getCurrentClippingPath();
        Shape transformedClippingPath = this.getPageTransform().createTransformedShape(clippingPath);
        Rectangle2D transformedClippingPathBounds = transformedClippingPath.getBounds2D();

        return transformedClippingPathBounds;
    }

    private static boolean isPrintable(String s) {
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

    private TextPosition getTextPosition(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                         Vector displacement) throws IOException {

        // LegacyPDFStreamEngine
        PDGraphicsState state = getGraphicsState();
        Matrix ctm = state.getCurrentTransformationMatrix();
        float fontSize = state.getTextState().getFontSize();
        float horizontalScaling = state.getTextState().getHorizontalScaling() / 100f;
        Matrix textMatrix = getTextMatrix();

        BoundingBox bbox = font.getBoundingBox();
        if (bbox.getLowerLeftY() < Short.MIN_VALUE) {
            // PDFBOX-2158 and PDFBOX-3130
            // files by Salmat eSolutions / ClibPDF Library
            bbox.setLowerLeftY(-(bbox.getLowerLeftY() + 65536));
        }
        // 1/2 the bbox is used as the height todo: why?
        float glyphHeight = bbox.getHeight() / 2;
        
        PDFontDescriptor fontDescriptor = font.getFontDescriptor();
        if (fontDescriptor != null)
        {
            float capHeight = fontDescriptor.getCapHeight();
            if (capHeight != 0 && capHeight < glyphHeight)
            {
                glyphHeight = capHeight;
            }
        }        

        // transformPoint from glyph space -> text space
        float height;
        if (font instanceof PDType3Font) {
            height = font.getFontMatrix().transformPoint(0, glyphHeight).y;
        } else {
            height = glyphHeight / 1000;
        }

        float displacementX = displacement.getX();
        // the sorting algorithm is based on the width of the character. As the
        // displacement
        // for vertical characters doesn't provide any suitable value for it, we
        // have to
        // calculate our own
        if (font.isVertical()) {
            displacementX = font.getWidth(code) / 1000;
            // there may be an additional scaling factor for true type fonts
            TrueTypeFont ttf = null;
            if (font instanceof PDTrueTypeFont) {
                ttf = ((PDTrueTypeFont) font).getTrueTypeFont();
            } else if (font instanceof PDType0Font) {
                PDCIDFont cidFont = ((PDType0Font) font).getDescendantFont();
                if (cidFont instanceof PDCIDFontType2) {
                    ttf = ((PDCIDFontType2) cidFont).getTrueTypeFont();
                }
            }
            if (ttf != null && ttf.getUnitsPerEm() != 1000) {
                displacementX *= 1000f / ttf.getUnitsPerEm();
            }
        }

        // (modified) combined displacement, this is calculated *without* taking
        // the character
        // spacing and word spacing into account, due to legacy code in
        // TextStripper
        float tx = displacementX * fontSize * horizontalScaling;
        float ty = displacement.getY() * fontSize;

        // (modified) combined displacement matrix
        Matrix td = Matrix.getTranslateInstance(tx, ty);

        // (modified) text rendering matrix
        Matrix nextTextRenderingMatrix = td.multiply(textMatrix).multiply(ctm); // text
        // space
        // ->
        // device
        // space
        float nextX = nextTextRenderingMatrix.getTranslateX();
        float nextY = nextTextRenderingMatrix.getTranslateY();

        // (modified) width and height calculations
        float dxDisplay = nextX - textRenderingMatrix.getTranslateX();
        float dyDisplay = height * textRenderingMatrix.getScalingFactorY();

        //
        // start of the original method
        //

        // Note on variable names. There are three different units being used in
        // this code.
        // Character sizes are given in glyph units, text locations are
        // initially given in text
        // units, and we want to save the data in display units. The variable
        // names should end with
        // Text or Disp to represent if the values are in text or disp units (no
        // glyph units are
        // saved).

        float glyphSpaceToTextSpaceFactor = 1 / 1000f;
        if (font instanceof PDType3Font) {
            glyphSpaceToTextSpaceFactor = font.getFontMatrix().getScaleX();
        }

        float spaceWidthText = 0;
        try {
            // to avoid crash as described in PDFBOX-614, see what the space
            // displacement should be
            spaceWidthText = font.getSpaceWidth() * glyphSpaceToTextSpaceFactor;
        } catch (Throwable exception) {
            this.log.warn("Error getting spaceWidthText", exception);
        }

        if (spaceWidthText == 0) {
            spaceWidthText = font.getAverageFontWidth() * glyphSpaceToTextSpaceFactor;
            // the average space width appears to be higher than necessary so
            // make it smaller
            spaceWidthText *= .80f;
        }
        if (spaceWidthText == 0) {
            spaceWidthText = 1.0f; // if could not find font, use a generic
            // value
        }

        // the space width has to be transformed into display units
        float spaceWidthDisplay = spaceWidthText * textRenderingMatrix.getScalingFactorX();

        // use our additional glyph list for Unicode mapping
        unicode = font.toUnicode(code, glyphList);

        // when there is no Unicode mapping available, Acrobat simply coerces
        // the character code
        // into Unicode, so we do the same. Subclasses of PDFStreamEngine don't
        // necessarily want
        // this, which is why we leave it until this point in
        // PDFTextStreamEngine.
        if (unicode == null) {
            if (font instanceof PDSimpleFont) {
                char c = (char) code;
                unicode = new String(new char[]{c});
            } else {
                // Acrobat doesn't seem to coerce composite font's character
                // codes, instead it
                // skips them. See the "allah2.pdf" TestTextStripper file.
                return null;
            }
        }

        // adjust for cropbox if needed
        Matrix translatedTextRenderingMatrix;
        if (translateMatrix == null) {
            translatedTextRenderingMatrix = textRenderingMatrix;
        } else {
            translatedTextRenderingMatrix = Matrix.concatenate(translateMatrix, textRenderingMatrix);
            nextX -= pageSize.getLowerLeftX();
            nextY -= pageSize.getLowerLeftY();

        }

        return new TextPosition(pageRotation, pageSize.getWidth(), pageSize.getHeight(), translatedTextRenderingMatrix,
                nextX, nextY, Math.abs(dyDisplay), dxDisplay, Math.abs(spaceWidthDisplay), unicode, new int[]{code},
                font, fontSize, (int) (fontSize * textMatrix.getScalingFactorX()));
    }

    public boolean isDebugClippingPaths() {
        return debugClippingPaths;
    }

    public void setDebugClippingPaths(boolean debugClippingPaths) {
        this.debugClippingPaths = debugClippingPaths;
    }

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
}
