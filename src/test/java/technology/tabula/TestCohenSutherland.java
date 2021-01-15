package technology.tabula;

import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.*;

public class TestCohenSutherland {

    private Rectangle2D clipWindow;
    private CohenSutherlandClipping algorithm;
    private static final double DELTA = 0.001;

    @Before
    public void set() {
        clipWindow = new Rectangle(10, 10, 50, 50);
        algorithm = new CohenSutherlandClipping(clipWindow);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    // TODO: How to parameterize the tests?
    @Test
    public void theLineIsCompletelyInside() {
        Line2D.Float line = new Line2D.Float(20, 20, 30, 30);
        assertTrue(algorithm.clip(line));
        assertEquals(20, line.x1, DELTA);
        assertEquals(20, line.y1, DELTA);
        assertEquals(30, line.x2, DELTA);
        assertEquals(30, line.y2, DELTA);
    }

    @Test
    public void theLineIsCompletelyOnTheLeft() {
        float x1 = 3, y1 = 13, x2 = 6, y2 = 16;
        Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
        assertFalse(algorithm.clip(line));
        assertEquals(x1, line.x1, DELTA);
        assertEquals(y1, line.y1, DELTA);
        assertEquals(x2, line.x2, DELTA);
        assertEquals(y2, line.y2, DELTA);
    }

    @Test
    public void theLineIsCompletelyOnTheUp() {
        float x1 = 15, y1 = 5, x2 = 25, y2 = 2;
        Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
        assertFalse(algorithm.clip(line));
        assertEquals(x1, line.x1, DELTA);
        assertEquals(y1, line.y1, DELTA);
        assertEquals(x2, line.x2, DELTA);
        assertEquals(y2, line.y2, DELTA);
    }

    @Test
    public void theLineIsCompletelyOnTheRight() {
        float x1 = 65, y1 = 15, x2 = 70, y2 = 20;
        Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
        assertFalse(algorithm.clip(line));
        assertEquals(x1, line.x1, DELTA);
        assertEquals(y1, line.y1, DELTA);
        assertEquals(x2, line.x2, DELTA);
        assertEquals(y2, line.y2, DELTA);
    }

    @Test
    public void theLineIsCompletelyOnTheBottom() {
        float x1 = 15, y1 = 65, x2 = 25, y2 = 70;
        Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
        assertFalse(algorithm.clip(line));
        assertEquals(x1, line.x1, DELTA);
        assertEquals(y1, line.y1, DELTA);
        assertEquals(x2, line.x2, DELTA);
        assertEquals(y2, line.y2, DELTA);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Test
    public void lineCrossesTopLeftCorner() {
        float x1 = 5, y1 = 25, x2 = 25, y2 = 5;
        Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
        assertTrue(algorithm.clip(line));
        assertEquals(10, line.x1, DELTA);
        assertEquals(20, line.y1, DELTA);
        assertEquals(20, line.x2, DELTA);
        assertEquals(10, line.y2, DELTA);
    }

    @Test
    public void lineCrossesPartiallyTopLeftCorner() {
        float x1 = 15, y1 = 15, x2 = 25, y2 = 5;
        Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
        assertTrue(algorithm.clip(line));
        assertEquals(x1, line.x1, DELTA);
        assertEquals(y1, line.y1, DELTA);
        assertEquals(20, line.x2, DELTA);
        assertEquals(10, line.y2, DELTA);
    }

}
