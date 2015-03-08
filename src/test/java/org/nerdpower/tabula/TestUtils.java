package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
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
    
    @Test
    public void testParsePagesOption() throws ParseException {
        
        List<Integer> rv = Utils.parsePagesOption("1");
        assertArrayEquals(new Integer[] { 1 }, rv.toArray());
        
        rv = Utils.parsePagesOption("1-4");
        assertArrayEquals(new Integer[] { 1,2,3,4 }, rv.toArray());
        
        rv = Utils.parsePagesOption("1-4,20-24");
        assertArrayEquals(new Integer[] { 1,2,3,4,20,21,22,23,24 }, rv.toArray());
        
        rv = Utils.parsePagesOption("all");
        assertNull(rv);
    }
    
    @Test(expected=ParseException.class)
    public void testExceptionInParsePages() throws ParseException {
        Utils.parsePagesOption("1-4,24-22");
    }

    @Test(expected=ParseException.class)
    public void testAnotherExceptionInParsePages() throws ParseException {
        Utils.parsePagesOption("quuxor");
    }

}
