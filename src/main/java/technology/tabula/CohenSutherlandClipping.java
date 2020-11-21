/*
 * CohenSutherland.java
 * --------------------
 * (c) 2007 by Intevation GmbH
 *
 * @author Sascha L. Teichmann (teichmann@intevation.de)
 * @author Ludwig Reiter       (ludwig@intevation.de)
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LICENSE.txt coming with the sources for details.
 */
package technology.tabula;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

/**
 * Implements the well known Cohen Sutherland line
 * clipping algorithm (line against clip rectangle).
 */
public final class CohenSutherlandClipping {

    private double xMin;
    private double yMin;
    private double xMax;
    private double yMax;

    private static final int INSIDE = 0;
    private static final int LEFT   = 1;
    private static final int RIGHT  = 2;
    private static final int BOTTOM = 4;
    private static final int TOP    = 8;

    /**
     * Creates a Cohen Sutherland clipper with clip rect (0, 0, 0, 0).
     */
    public CohenSutherlandClipping() {}

    /**
     * Creates a Cohen Sutherland clipper with the given clip rectangle.
     * @param clip the clip rectangle to use
     */
    public CohenSutherlandClipping(Rectangle2D clip) {
        setClip(clip);
    }

    /**
     * Sets the clip rectangle.
     * @param clip the clip rectangle
     */
    public void setClip(Rectangle2D clip) {
        xMin = clip.getX();
        xMax = xMin + clip.getWidth();
        yMin = clip.getY();
        yMax = yMin + clip.getHeight();
    }

    private final int regionCode(double x, double y) {
        int code = (x < xMin) ? LEFT : (x > xMax) ? RIGHT : INSIDE;
        if (y < yMin)
            code |= BOTTOM;
        else if (y > yMax)
            code |= TOP;
        return code;
    }

    /**
     * Clips a given line against the clip rectangle.
     * The modification (if needed) is done in place.
     * @param line the line to clip.
     * @return true if line is clipped, false if line is
     * totally outside the clip rect.
     */
    public boolean clip(Line2D.Float line) {
        double point1X = line.getX1(), point1Y = line.getY1();
        double point2X = line.getX2(), point2Y = line.getY2();
        double outsidePointX = 0d, outsidePointY = 0d;

        boolean lineIsVertical = (point1X == point2X);
        double lineSlope = lineIsVertical ? 0d : (point2Y-point1Y)/(point2X-point1X);

        int point1Region = regionCode(point1X, point1Y);
        int point2Region = regionCode(point2X, point2Y);

        while (point1Region != INSIDE || point2Region != INSIDE) {
            if ((point1Region & point2Region) != INSIDE)
                return false;

            int outsidePointRegion = (point1Region == INSIDE) ? point2Region : point1Region;

            if ((outsidePointRegion & LEFT) != INSIDE) {
                outsidePointX = xMin;
                outsidePointY = (Utils.feq(outsidePointX, point1X) ? 0 : outsidePointX-point1X)*lineSlope + point1Y;
            }
            else if ((outsidePointRegion & RIGHT) != INSIDE) {
                outsidePointX = xMax;
                outsidePointY = (Utils.feq(outsidePointX, point1X) ? 0 : outsidePointX-point1X)*lineSlope + point1Y;
            }
            else if ((outsidePointRegion & BOTTOM) != INSIDE) {
                outsidePointY = yMin;
                outsidePointX = lineIsVertical
                    ? point1X
                    : (Utils.feq(outsidePointY, point1Y) ? 0 : outsidePointY-point1Y)/lineSlope + point1X;
            }
            else if ((outsidePointRegion & TOP) != INSIDE) {
                outsidePointY = yMax;
                outsidePointX = lineIsVertical
                    ? point1X
                    : (Utils.feq(outsidePointY, point1Y) ? 0 : outsidePointY-point1Y)/lineSlope + point1X;
            }

            if (outsidePointRegion == point1Region) {
                point1X = outsidePointX;
                point1Y = outsidePointY;
                point1Region  = regionCode(point1X, point1Y);
            }
            else {
                point2X = outsidePointX;
                point2Y = outsidePointY;
                point2Region = regionCode(point2X, point2Y);
            }
        }
        line.setLine(point1X, point1Y, point2X, point2Y);
        return true;
    }

}
