package technology.tabula;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRectangleSpatialIndex {

	@Test
	public void testIntersects() {
		
		Rectangle r = new Rectangle(0, 0, 0, 0);
		
		RectangleSpatialIndex<Rectangle> rSpatialIndex = new RectangleSpatialIndex<>();
		rSpatialIndex.add(r);
		
		assertTrue(rSpatialIndex.intersects(r).size() > 0);

	}

}
