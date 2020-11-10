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
    private Logger logger;
    private int clipWindingRule = -1;
    private GeneralPath currentPath = new GeneralPath();
    private static final float RULING_MINIMUM_LENGTH = 0.01f;

    protected ObjectExtractorStreamEngine(PDPage page) {
        super(page);
        rulings = new ArrayList<>();
        logger = LoggerFactory.getLogger(ObjectExtractorStreamEngine.class);
        makesPageTransformation();
    }

    private void makesPageTransformation() {
        PDRectangle cropBox = getPage().getCropBox();
        int rotation = getPage().getRotation();
        pageTransform = new AffineTransform();
        if (Math.abs(rotation) == 90 || Math.abs(rotation) == 270) {
            pageTransform = AffineTransform.getRotateInstance(rotation * (Math.PI / 180.0), 0, 0);
        } else {
            pageTransform.concatenate(AffineTransform.getTranslateInstance(0, cropBox.getHeight()));
        }
        pageTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
        pageTransform.translate(-cropBox.getLowerLeftX(), -cropBox.getLowerLeftY());
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
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
        strokeOrFillPath();
    }

    @Override
    public void fillPath(int arg0)  {
        strokeOrFillPath();
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
    }

    @Override
    public void strokePath()  {
        strokeOrFillPath();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    private void strokeOrFillPath() {
        boolean didNotPassedTheFilter = filterPathBySegmentType();
        if (didNotPassedTheFilter) return;

        // TODO: how to implement color filter?

        PathIterator pathIterator = currentPath.getPathIterator(pageTransform);

        Point2D.Float start_point = getStartPoint(pathIterator);
        Point2D.Float last_move = start_point;
        Point2D.Float end_point = null;
        Line2D.Float line;
        PointComparator pointComparator = new PointComparator();

        int currentSegmentType;
        float[] coordinates = new float[6];
        while (!pathIterator.isDone()) {
            pathIterator.next();
            // This can be the last segment, when pi.isDone, but we need to
            // process it otherwise us-017.pdf fails the last value.
            try {
                currentSegmentType = pathIterator.currentSegment(coordinates);
            } catch (IndexOutOfBoundsException ex) {
                continue;
            }
            switch (currentSegmentType) {
                case SEG_LINETO:
                    end_point = new Point2D.Float(coordinates[0], coordinates[1]);
                    if (start_point == null || end_point == null) { break; }
                    line = getLineBetween(start_point, end_point, pointComparator);
                    verifyLineIntersectsClipping(line);
                    break;
                case SEG_MOVETO:
                    last_move = new Point2D.Float(coordinates[0], coordinates[1]);
                    end_point = last_move;
                    break;
                case SEG_CLOSE:
                    // according to PathIterator docs:
                    // "the preceding subpath should be closed by appending a line
                    // segment back to the point corresponding to the most recent
                    // SEG_MOVETO."
                    if (start_point == null || end_point == null) { break; }
                    line = getLineBetween(end_point, last_move, pointComparator);
                    verifyLineIntersectsClipping(line);
                    break;
            }
            start_point = end_point;
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

    private Point2D.Float getStartPoint(PathIterator pathIterator) {
        float[] startPointCoordinates = new float[6];
        pathIterator.currentSegment(startPointCoordinates);
        float x = Utils.round(startPointCoordinates[0], 2);
        float y = Utils.round(startPointCoordinates[1], 2);
        return new Point2D.Float(x, y);
    }

    private Line2D.Float getLineBetween(Point2D.Float pointA, Point2D.Float pointB, PointComparator pointComparator) {
        if (pointComparator.compare(pointA, pointB) == -1) {
            return new Line2D.Float(pointA, pointB);
        }
        return new Line2D.Float(pointB, pointA);
    }

    private void verifyLineIntersectsClipping(Line2D.Float line) {
        Rectangle2D currentClippingPath =  currentClippingPath();
        if (line.intersects(currentClippingPath)) {
            Ruling ruling = new Ruling(line.getP1(), line.getP2()).intersect(currentClippingPath);
            if (ruling.length() > RULING_MINIMUM_LENGTH) {
                this.rulings.add(ruling);
            }
        }
    }

    public Rectangle2D currentClippingPath() {
        Shape clippingPath = this.getGraphicsState().getCurrentClippingPath();
        Shape transformedClippingPath = pageTransform.createTransformedShape(clippingPath);
        return transformedClippingPath.getBounds2D();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    class PointComparator implements Comparator<Point2D> {
        @Override
        public int compare(Point2D o1, Point2D o2) {
            float o1X = Utils.round(o1.getX(), 2);
            float o1Y = Utils.round(o1.getY(), 2);
            float o2X = Utils.round(o2.getX(), 2);
            float o2Y = Utils.round(o2.getY(), 2);
            if (o1Y > o2Y) return 1;
            if (o1Y < o2Y) return -1;
            if (o1X > o2X) return 1;
            if (o1X < o2X) return -1;
            return 0;
        }
    }

}
