package technology.tabula;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RectangleSpatialIndexTest {

	@Test
	public void testIntersects() {
		Rectangle r = new Rectangle(0, 0, 0, 0);
		
		RectangleSpatialIndex<Rectangle> rSpatialIndex = new RectangleSpatialIndex<>();
		rSpatialIndex.add(r);
		
		assertThat(rSpatialIndex.intersects(r)).isNotEmpty();
	}
}
