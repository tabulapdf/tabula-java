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

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ObjectExtractorStreamEngine extends PDFGraphicsStreamEngine {

    private static final String NBSP = "\u00A0";

    protected List<Ruling> rulings;
    private AffineTransform pageTransform;
    private boolean debugClippingPaths;
    private boolean extractRulingLines = true;
    private Logger log;
    private int clipWindingRule = -1;
    private GeneralPath currentPath = new GeneralPath();
    public List<Shape> clippingPaths;

    protected ObjectExtractorStreamEngine(PDPage page) {
        super(page);

        this.log = LoggerFactory.getLogger(ObjectExtractorStreamEngine.class);

        this.rulings = new ArrayList<>();
        this.pageTransform = null;

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

        this.pageTransform.translate(-cb.getLowerLeftX(), -cb.getLowerLeftY());
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3)  {
        currentPath.moveTo((float) p0.getX(), (float) p0.getY());
        currentPath.lineTo((float) p1.getX(), (float) p1.getY());
        currentPath.lineTo((float) p2.getX(), (float) p2.getY());
        currentPath.lineTo((float) p3.getX(), (float) p3.getY());

        currentPath.closePath();
    }

    @Override
    public void clip(int windingRule)  {
        // the clipping path will not be updated until the succeeding painting
        // operator is called
        clipWindingRule = windingRule;
    }

    @Override
    public void closePath()  {
        currentPath.closePath();
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3)  {
        currentPath.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void drawImage(PDImage arg0)  {
        // TODO Auto-generated method stub

    }

    @Override
    public void endPath()  {
        if (clipWindingRule != -1) {
            currentPath.setWindingRule(clipWindingRule);
            getGraphicsState().intersectClippingPath(currentPath);
            clipWindingRule = -1;
        }
        currentPath.reset();
    }

    @Override
    public void fillAndStrokePath(int arg0) {
        strokeOrFillPath(true);
    }

    @Override
    public void fillPath(int arg0)  {
        strokeOrFillPath(true);
    }

    @Override
    public Point2D getCurrentPoint() {
        return currentPath.getCurrentPoint();
    }

    @Override
    public void lineTo(float x, float y) {
        currentPath.lineTo(x, y);
    }

    @Override
    public void moveTo(float x, float y) {
        currentPath.moveTo(x, y);
    }

    @Override
    public void shadingFill(COSName arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void strokePath()  {
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

                    if (start_pos == null || end_pos == null) {
                        break;
                    }

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
                    if (start_pos == null || end_pos == null) {
                        break;
                    }
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

        return transformedClippingPath.getBounds2D();
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
