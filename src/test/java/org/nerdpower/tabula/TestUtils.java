package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.junit.Test;

public class TestUtils {
    
    public static final Ruling[] RULINGS = { 
        new Ruling(new Point2D.Float(0, 0), new Point2D.Float(1,1)),
        new Ruling(new Point2D.Float(2, 2), new Point2D.Float(3,3)) 
    };
    
    public static final Rectangle[] RECTANGLES = {
        new Rectangle(),
        new Rectangle(0, 0, 2, 4)
    };


    @Test
    public void testBoundsOfTwoRulings() {
        Rectangle r = Utils.bounds(Arrays.asList(RULINGS));
        assertEquals(0, r.getMinX(), 0);
        assertEquals(0, r.getMinY(), 0);
        assertEquals(3, r.getWidth(), 0);
        assertEquals(3, r.getHeight(), 0);
    }
    
    @Test
    public void testBoundsOfOneEmptyRectangleAndAnotherNonEmpty() {
        Rectangle r = Utils.bounds(Arrays.asList(RECTANGLES));
        assertEquals(r, RECTANGLES[1]);
    }

}
