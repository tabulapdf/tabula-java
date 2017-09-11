package technology.tabula;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RectangleTest {

	@Test
	public void testCompareEqualsRectangles() {
		Rectangle first = new Rectangle();
		Rectangle second = new Rectangle();
		
		assertThat(first).isEqualTo(second);
		assertThat(second).isEqualTo(first);
	}
	
	@Test
	public void testCompareAlignedHorizontalRectangle() {
		Rectangle lower = new Rectangle(0f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(0f,20f, 10f, 10f);
		
		assertThat(lower).isLessThan(upper);
	}
	
	@Test
	public void testCompareAlignedVerticalRectangle() {
		Rectangle lower = new Rectangle(10f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(20f,0f, 10f, 10f);
		
		assertThat(lower).isLessThan(upper);
	}
	
	@Test
	public void testCompareVerticalOverlapRectangle() {
		Rectangle lower = new Rectangle(5f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(0f, 10f, 10f, 10f);
		
		assertThat(lower).isLessThan(upper);
	}
	
	@Test
	public void testCompareVerticalOverlapLessThresholdRectangle() {
		Rectangle lower = new Rectangle(0f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(9.8f, 0f, 10f, 10f);
		
		assertThat(lower).isLessThan(upper);
	}

	@Test
	public void testQuickSortOneUpperThanOther() {
		
		Rectangle lower = new Rectangle(175.72f, 72.72f, 1.67f, 1.52f); //, (Comma after AARON)
		Rectangle upper = new Rectangle(169.21f, 161.16f, 4.33f, 4.31f); // R (REGIONAL PULMONARY)
		
		assertThat(lower).isGreaterThan(upper);
	}
	
	@Test
	public void testQuickSortRectangleList() {
		
		//Testing wrong sorting
		// Expected: AARON, JOSHUA, N
		// but was: AARON JOSHUA N , ,
		Rectangle first = new Rectangle(172.92999267578125f, 51.47999954223633f, 4.0f, 4.309999942779541f); //A 
		Rectangle second = new Rectangle(175.72000122070312f, 72.72000122070312f, 1.6699999570846558f, 1.5199999809265137f); //,
		Rectangle third = new Rectangle(172.92999267578125f, 96.36000061035156f, 4.0f, 4.309999942779541f); //A
		Rectangle fourth = new Rectangle(175.72000122070312f, 100.31999969482422f, 1.6699999570846558f, 1.5199999809265137f); //,
		Rectangle fifth = new Rectangle(172.92999267578125f, 103.68000030517578f, 4.329999923706055f, 4.309999942779541f); //N
		Rectangle sixth = new Rectangle(169.2100067138672f, 161.16000366210938f, 4.329999923706055f, 4.309999942779541f); //R
		
		List<Rectangle> expectedList = new ArrayList<>();
		expectedList.add(first);
		expectedList.add(sixth);
		expectedList.add(second);
		expectedList.add(third);
		expectedList.add(fourth);
		expectedList.add(fifth);
		List<Rectangle> toSortList = new ArrayList<>();
		toSortList.add(sixth);
		toSortList.add(second);
		toSortList.add(third);
		toSortList.add(fifth);
		toSortList.add(first);
		toSortList.add(fourth);
		
		Collections.sort(toSortList);
		
		assertThat(toSortList).isEqualTo(expectedList);
	}
	
	@Test
	public void testGetVerticalOverlapShouldReturnZero() {
		
		Rectangle lower = new Rectangle(10f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(20f,0f, 10f, 10f);
		
		float overlap = lower.verticalOverlap(upper);
		
		assertThat(overlap).isEqualTo(0f);
		assertThat(!lower.verticallyOverlaps(upper)).isTrue();
		assertThat(lower.verticalOverlapRatio(upper)).isEqualTo(0f);
		assertThat(lower.overlapRatio(upper)).isEqualTo(0f);
	}
	
	@Test
	public void testGetVerticalOverlapShouldReturnMoreThanZero() {
		
		Rectangle lower = new Rectangle(15f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(20f, 0f, 10f, 10f);
		
		float overlap = lower.verticalOverlap(upper);
		
		assertThat(overlap).isEqualTo(5f);
		assertThat(lower.verticallyOverlaps(upper)).isTrue();
		assertThat(lower.verticalOverlapRatio(upper)).isEqualTo(0.5f);
		assertThat(lower.overlapRatio(upper)).isEqualTo(0f);
	}
	
	@Test
	public void testGetHorizontalOverlapShouldReturnZero() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(10f, 10f, 10f, 10f);
		
		assertThat(one.horizontallyOverlaps(two)).isFalse();
		assertThat(one.overlapRatio(two)).isEqualTo(0f);
	}
	
	@Test
	public void testGetHorizontalOverlapShouldReturnMoreThanZero() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(10f, 5f, 10f, 10f);
		
		assertThat(one.horizontallyOverlaps(two)).isTrue();
		assertThat(one.horizontalOverlap(two)).isEqualTo(5f);
		assertThat(one.overlapRatio(two)).isEqualTo(0f);
	}
	
	@Test
	public void testGetOverlapShouldReturnMoreThanZero() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(5f, 5f, 10f, 10f);
		
		assertThat(one.horizontallyOverlaps(two)).isTrue();
		assertThat(one.verticallyOverlaps(two)).isTrue();
		assertThat(one.horizontalOverlap(two)).isEqualTo(5f);
		assertThat(one.verticalOverlap(two)).isEqualTo(5f);
		assertThat(one.overlapRatio(two)).isEqualTo((25f/175));
	}
	
	@Test
	public void testMergeNoOverlappingRectangles() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(0f, 10f, 10f, 10f);
		
		one.merge(two);
		
		assertThat(one.getWidth()).isEqualTo(20d);
		assertThat(one.getHeight()).isEqualTo(10d);
		assertThat(one.getLeft()).isEqualTo(0f);
		assertThat(one.getTop()).isEqualTo(0f);
		assertThat(one.getBottom()).isEqualTo(10f);
		assertThat(one.getArea()).isEqualTo(20f * 10f);
	}
	
	@Test
	public void testMergeOverlappingRectangles() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(5f, 5f, 10f, 10f);
		
		one.merge(two);
		
		assertThat(one.getWidth()).isCloseTo(15d, within(0d));
		assertThat(one.getHeight()).isCloseTo(15d, within(0d));
		assertThat(one.getLeft()).isCloseTo(0f, within(0f));
		assertThat(one.getTop()).isCloseTo(0f, within(0f));
	}
	
	@Test
	public void testRectangleGetPoints() {
		
		Rectangle one = new Rectangle(10f, 20f, 30f, 40f);
		
		Point2D[] points = one.getPoints();
		
		Point2D[] expectedPoints = new Point2D[]{ 
				new Point2D.Float(20f, 10f),
				new Point2D.Float(50f, 10f),
				new Point2D.Float(50f, 50f),
				new Point2D.Float(20f, 50f)
		};
		
		assertThat(points).isEqualTo(expectedPoints);
	}
	
	@Test
	public void testGetBoundingBox() {
		
		List<Rectangle> rectangles = new ArrayList<>();
		rectangles.add(new Rectangle(0f, 0f, 10f, 10f));
		rectangles.add(new Rectangle(20f, 30f, 10f, 10f));
		
		Rectangle boundingBoxOf = Rectangle.boundingBoxOf(rectangles);
		
//		assertThat(30f).isCloseTo(new Rectangle(0f, 0f, 40f, 0f));
	}
}
