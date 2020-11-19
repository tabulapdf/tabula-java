package technology.tabula;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.awt.geom.PathIterator.*;

class ObjectExtractorStreamEngine extends PDFGraphicsStreamEngine {

    protected List<Ruling> rulings;
    private AffineTransform pageTransform;
    private boolean extractRulingLines = true;
    private Logger logger;
    private int clipWindingRule = -1;
    private GeneralPath currentPath = new GeneralPath();

    protected ObjectExtractorStreamEngine(PDPage page) {
        super(page);
        logger = LoggerFactory.getLogger(ObjectExtractorStreamEngine.class);
        rulings = new ArrayList<>();

        // Calculate page transform:
        pageTransform = new AffineTransform();
        PDRectangle pageCropBox = getPage().getCropBox();
        int rotationAngleInDegrees = getPage().getRotation();

        if (Math.abs(rotationAngleInDegrees) == 90 || Math.abs(rotationAngleInDegrees) == 270) {
            double rotationAngleInRadians = rotationAngleInDegrees * (Math.PI / 180.0);
            pageTransform = AffineTransform.getRotateInstance(rotationAngleInRadians, 0, 0);
        } else {
            double deltaX = 0;
            double deltaY = pageCropBox.getHeight();
            pageTransform.concatenate(AffineTransform.getTranslateInstance(deltaX, deltaY));
        }

        pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
        pageTransform.translate(-pageCropBox.getLowerLeftX(), -pageCropBox.getLowerLeftY());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        currentPath.moveTo((float) p0.getX(), (float) p0.getY());
        currentPath.lineTo((float) p1.getX(), (float) p1.getY());
        currentPath.lineTo((float) p2.getX(), (float) p2.getY());
        currentPath.lineTo((float) p3.getX(), (float) p3.getY());
        currentPath.closePath();
    }

    @Override
    public void clip(int windingRule) {
        // The clipping path will not be updated until the succeeding painting
        // operator is called.
        clipWindingRule = windingRule;
    }

    @Override
    public void closePath() {
        currentPath.closePath();
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        currentPath.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void drawImage(PDImage arg0) {}

    @Override
    public void endPath() {
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
    public void fillPath(int arg0) {
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
    public void shadingFill(COSName arg0) {}

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Override
    public void strokePath()  {
        strokeOrFillPath(false);
    }

    private void strokeOrFillPath(boolean isFill) {
        if (!extractRulingLines) {
            currentPath.reset();
            return;
        }

        boolean didNotPassedTheFilter = filterPathBySegmentType();
        if (didNotPassedTheFilter) return;

        // TODO: how to implement color filter?

        // skip the first path operation and save it as the starting position
        float[] first = new float[6];
        PathIterator pathIterator = currentPath.getPathIterator(this.getPageTransform());
        float[] c = new float[6];
        int currentSegment;
        pathIterator.currentSegment(first);
        // last move
        Point2D.Float start_pos = new Point2D.Float(Utils.round(first[0], 2), Utils.round(first[1], 2));
        Point2D.Float last_move = start_pos;
        Point2D.Float end_pos = null;
        Line2D.Float line;
        PointComparator pc = new PointComparator();
        while (!pathIterator.isDone()) {
            pathIterator.next();
            // This can be the last segment, when pi.isDone, but we need to
            // process it
            // otherwise us-017.pdf fails the last value.
            try {
                currentSegment = pathIterator.currentSegment(c);
            } catch (IndexOutOfBoundsException ex) {
                continue;
            }
            switch (currentSegment) {
                case SEG_LINETO:
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
                case SEG_MOVETO:
                    last_move = new Point2D.Float(c[0], c[1]);
                    end_pos = last_move;
                    break;
                case SEG_CLOSE:
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
        currentPath.reset();
    }

    private boolean filterPathBySegmentType() {
        PathIterator pathIterator = currentPath.getPathIterator(pageTransform);
        float[] coordinates = new float[6];
        int currentSegmentType = pathIterator.currentSegment(coordinates);
        if (currentSegmentType != SEG_MOVETO) {
            currentPath.reset();
            return true;
        }
        pathIterator.next();
        while (!pathIterator.isDone()) {
            currentSegmentType = pathIterator.currentSegment(coordinates);
            if (currentSegmentType != SEG_LINETO && currentSegmentType != SEG_CLOSE && currentSegmentType != SEG_MOVETO) {
                currentPath.reset();
                return true;
            }
            pathIterator.next();
        }
        return false;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public AffineTransform getPageTransform() {
        return pageTransform;
    }

    public Rectangle2D currentClippingPath() {
        Shape currentClippingPath = getGraphicsState().getCurrentClippingPath();
        Shape transformedClippingPath = getPageTransform().createTransformedShape(currentClippingPath);
        return transformedClippingPath.getBounds2D();
    }

    // TODO: repeated in SpreadsheetExtractionAlgorithm.
    class PointComparator implements Comparator<Point2D> {
        @Override
        public int compare(Point2D p1, Point2D p2) {
            float p1X = Utils.round(p1.getX(), 2);
            float p1Y = Utils.round(p1.getY(), 2);
            float p2X = Utils.round(p2.getX(), 2);
            float p2Y = Utils.round(p2.getY(), 2);

            if (p1Y > p2Y)
                return 1;
            if (p1Y < p2Y)
                return -1;
            if (p1X > p2X)
                return 1;
            if (p1X < p2X)
                return -1;
            return 0;
        }
    }

}
