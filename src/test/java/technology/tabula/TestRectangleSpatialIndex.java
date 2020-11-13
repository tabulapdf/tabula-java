package technology.tabula;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestRectangleSpatialIndex {

	@Test
	public void testIntersects() {
		
		Rectangle r = new Rectangle(0, 0, 0, 0);
		
		RectangleSpatialIndex<Rectangle> rSpatialIndex = new RectangleSpatialIndex<>();
		rSpatialIndex.add(r);
		
		assertTrue(rSpatialIndex.intersects(r).size() > 0);

	}

}
