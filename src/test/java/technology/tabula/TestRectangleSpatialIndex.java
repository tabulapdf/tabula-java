package technology.tabula;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRectangleSpatialIndex {

    @Test
    public void testIntersects() {
        Rectangle rectangle = new Rectangle(0, 0, 0, 0);

        RectangleSpatialIndex<Rectangle> rectangleSpatialIndex = new RectangleSpatialIndex<>();
        rectangleSpatialIndex.add(rectangle);

        assertTrue(rectangleSpatialIndex.intersects(rectangle).size() > 0);
    }

}
