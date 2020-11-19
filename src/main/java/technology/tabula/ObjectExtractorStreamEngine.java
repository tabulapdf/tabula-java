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

    private static final float RULING_MINIMUM_LENGTH = 0.01f;

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

        // Skip the first path operation and save it as the starting point.
        PathIterator pathIterator = currentPath.getPathIterator(getPageTransform());

        float[] coordinates = new float[6];
        int currentSegment;

        Point2D.Float startPoint = getStartPoint(pathIterator);
        Point2D.Float last_move = startPoint;
        Point2D.Float endPoint = null;
        Line2D.Float line;
        PointComparator pointComparator = new PointComparator();

        while (!pathIterator.isDone()) {
            pathIterator.next();
            // This can be the last segment, when pathIterator.isDone, but we need to
            // process it otherwise us-017.pdf fails the last value.
            try {
                currentSegment = pathIterator.currentSegment(coordinates);
            } catch (IndexOutOfBoundsException ex) {
                continue;
            }
            switch (currentSegment) {
                case SEG_LINETO:
                    endPoint = new Point2D.Float(coordinates[0], coordinates[1]);
                    if (startPoint == null || endPoint == null) {
                        break;
                    }
                    line = getLineBetween(startPoint, endPoint, pointComparator);
                    verifyLineIntersectsClipping(line);
                    break;
                case SEG_MOVETO:
                    last_move = new Point2D.Float(coordinates[0], coordinates[1]);
                    endPoint = last_move;
                    break;
                case SEG_CLOSE:
                    // According to PathIterator docs:
                    // "The preceding sub-path should be closed by appending a line
                    // segment back to the point corresponding to the most recent
                    // SEG_MOVETO."
                    if (startPoint == null || endPoint == null) {
                        break;
                    }
                    line = getLineBetween(endPoint, last_move, pointComparator);
                    verifyLineIntersectsClipping(line);
                    break;
            }
            startPoint = endPoint;
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
        Rectangle2D currentClippingPath = currentClippingPath();
        if (line.intersects(currentClippingPath)) {
            Ruling ruling = new Ruling(line.getP1(), line.getP2()).intersect(currentClippingPath);
            if (ruling.length() > RULING_MINIMUM_LENGTH) {
                rulings.add(ruling);
            }
        }
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
