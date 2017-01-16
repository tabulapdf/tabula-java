package technology.tabula;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ObjectExtractorStreamEngine extends PDFGraphicsStreamEngine {

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
			this.pageTransform.concatenate(AffineTransform.getTranslateInstance(0, cb.getHeight()));
			this.pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
		}
		else {
			this.pageTransform.concatenate(AffineTransform.getTranslateInstance(0, cb.getHeight()));
			this.pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
		}
	}

	@Override
	protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode, Vector displacement)
			throws IOException {
		
        PDGraphicsState state = getGraphicsState();
        float fontSize = state.getTextState().getFontSize();		
		
		Rectangle2D bbox = new Rectangle2D.Float(0, 0, font.getWidth(code) / 1000, 1);
		AffineTransform at = textRenderingMatrix.createAffineTransform();
		//bbox = at.createTransformedShape(bbox).getBounds2D();
		bbox = this.getPageTransform().createTransformedShape(at.createTransformedShape(bbox)).getBounds2D();
		
		BoundingBox fontBbox = font.getBoundingBox();
		float glyphHeight = fontBbox.getHeight() / 2;
		float height = glyphHeight / 1000;
		float h = height * textRenderingMatrix.getScalingFactorY();
		
		
        float pageHeight = this.pageSize.getHeight();
        float yDirAdj = pageHeight - textRenderingMatrix.getTranslateY();
		
        TextElement te = new TextElement(
                Utils.round(yDirAdj - h, 2),
                Utils.round(bbox.getX(), 2),
                Utils.round(bbox.getWidth(), 2),
                Utils.round(h,2),
                font,
                fontSize,
                unicode,
                this.widthOfSpace(font, textRenderingMatrix));

        if (this.currentClippingPath().intersects(te)) {

            this.minCharWidth = (float) Math.min(this.minCharWidth, te.getWidth());
            this.minCharHeight = (float) Math.min(this.minCharHeight, te.getHeight());

            this.spatialIndex.add(te);
            this.characters.add(te);
        }

        if (this.isDebugClippingPaths() && !this.clippingPaths.contains(this.currentClippingPath())) {
            this.clippingPaths.add(this.currentClippingPath());
        }
		
		//this.log.warn("showGlyph called with string: {} {} {}", unicode, bbox.getBounds(), getPage().getCropBox().getWidth());
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
		 // the clipping path will not be updated until the succeeding painting operator is called
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
        if (clipWindingRule != -1)
        {
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
	
	private float widthOfSpace(PDFont font, Matrix textRenderingMatrix) {
        float glyphSpaceToTextSpaceFactor = 1 / 1000f;
        if (font instanceof PDType3Font)
        {
            glyphSpaceToTextSpaceFactor = font.getFontMatrix().getScaleX();
        }

        float spaceWidthText = 0;
        try
        {
            // to avoid crash as described in PDFBOX-614, see what the space displacement should be
            spaceWidthText = font.getSpaceWidth() * glyphSpaceToTextSpaceFactor;
        }
        catch (Throwable exception)
        {
            log.warn(exception.toString());
        }

        if (spaceWidthText == 0)
        {
            spaceWidthText = font.getAverageFontWidth() * glyphSpaceToTextSpaceFactor;
            // the average space width appears to be higher than necessary so make it smaller
            spaceWidthText *= .80f;
        }
        if (spaceWidthText == 0)
        {
            spaceWidthText = 1.0f; // if could not find font, use a generic value
        }

        // the space width has to be transformed into display units
        float spaceWidthDisplay = spaceWidthText * textRenderingMatrix.getScalingFactorX();
        
        return spaceWidthDisplay;

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
