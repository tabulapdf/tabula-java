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
package technology.tabula.clippers;

import technology.tabula.Utils;

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
     * Creates a Cohen Sutherland clipper with the given clip rectangle.
     * @param clip the clip rectangle to use.
     */
    public CohenSutherlandClipping(Rectangle2D clip) {
        xMin = clip.getX();
        xMax = xMin + clip.getWidth();
        yMin = clip.getY();
        yMax = yMin + clip.getHeight();
    }

    private final int regionCode(double x, double y) {
        int code = (x < xMin) ? LEFT : (x > xMax) ? RIGHT : INSIDE;
        if (y < yMin) code |= BOTTOM;
        else if (y > yMax) code |= TOP;
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
        /*
        Point point1 = new Point(line.getX1(), line.getY1());
        Point point2 = new Point(line.getX2(), line.getY2());

        if (point1.regionIs(INSIDE) && point2.regionIs(INSIDE)) {
            return true;
        }
        if (point1.isInTheSameRegionAs(point2)) {
            return false;
        }

        double deltaX = 0d;
        double deltaY = 0d;*/

        double p1x = line.getX1();
        double p1y = line.getY1();
        double p2x = line.getX2();
        double p2y = line.getY2();

        double qx = 0d;
        double qy = 0d;

        boolean vertical = p1x == p2x;

        double slope = vertical 
            ? 0d
            : (p2y-p1y)/(p2x-p1x);

        int c1 = regionCode(p1x, p1y);
        int c2 = regionCode(p2x, p2y);

        while (c1 != INSIDE || c2 != INSIDE) {

            if ((c1 & c2) != INSIDE)
                return false;

            int c = c1 == INSIDE ? c2 : c1;

            if ((c & LEFT) != INSIDE) {
                qx = xMin;
                qy = (Utils.feq(qx, p1x) ? 0 : qx-p1x)*slope + p1y;
            }
            else if ((c & RIGHT) != INSIDE) {
                qx = xMax;
                qy = (Utils.feq(qx, p1x) ? 0 : qx-p1x)*slope + p1y;
            }
            else if ((c & BOTTOM) != INSIDE) {
                qy = yMin;
                qx = vertical
                    ? p1x
                    : (Utils.feq(qy, p1y) ? 0 : qy-p1y)/slope + p1x;
            }
            else if ((c & TOP) != INSIDE) {
                qy = yMax;
                qx = vertical
                    ? p1x
                    : (Utils.feq(qy, p1y) ? 0 : qy-p1y)/slope + p1x;
            }

            if (c == c1) {
                p1x = qx;
                p1y = qy;
                c1  = regionCode(p1x, p1y);
            }
            else {
                p2x = qx;
                p2y = qy;
                c2 = regionCode(p2x, p2y);
            }
        }
        line.setLine(p1x, p1y, p2x, p2y);
        return true;
    }

    class Point {

        double x;
        double y;
        int region;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
            defineRegion();
        }

        void defineRegion() {
            int regionCode = (x < xMin) ? LEFT : (x > xMax) ? RIGHT : INSIDE;
            if (y < yMin) regionCode |= BOTTOM;
            else if (y > yMax) regionCode |= TOP;
            region = regionCode;
        }

        boolean isInTheSameRegionAs(Point otherPoint) {
            return this.region == otherPoint.region;
        }

        boolean regionIs(int region) {
            return this.region == region;
        }

    }

}
