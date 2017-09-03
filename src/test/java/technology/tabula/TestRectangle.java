package technology.tabula;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestRectangle {


	@Test
	public void testCompareEqualsRectangles() {
		Rectangle first = new Rectangle();
		Rectangle second = new Rectangle();

		assertTrue(first.equals(second));
		assertTrue(second.equals(first));
	}

	@Test
	public void testCompareAlignedHorizontalRectangle() {
		Rectangle lower = new Rectangle(0f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(0f,20f, 10f, 10f);

		assertTrue(lower.compareTo(upper) < 0);
	}

	@Test
	public void testCompareAlignedVerticalRectangle() {
		Rectangle lower = new Rectangle(10f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(20f,0f, 10f, 10f);

		assertTrue(lower.compareTo(upper) < 0);
	}

	@Test
	public void testCompareVerticalOverlapRectangle() {
		Rectangle lower = new Rectangle(5f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(0f, 10f, 10f, 10f);

		assertTrue(lower.compareTo(upper) < 0);
	}

	@Test
	public void testCompareVerticalOverlapLessThresholdRectangle() {
		Rectangle lower = new Rectangle(0f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(9.8f, 0f, 10f, 10f);

		assertTrue(lower.compareTo(upper) < 0);
	}



	@Test
	public void testQuickSortOneUpperThanOther() {

		Rectangle lower = new Rectangle(175.72f, 72.72f, 1.67f, 1.52f); //, (Comma after AARON)
		Rectangle upper = new Rectangle(169.21f, 161.16f, 4.33f, 4.31f); // R (REGIONAL PULMONARY)

		assertTrue(lower.compareTo(upper) > 0);

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

		Collections.sort(toSortList, Rectangle.ILL_DEFINED_ORDER);

		assertEquals(expectedList, toSortList);
	}

	@Test
	public void testGetVerticalOverlapShouldReturnZero() {

		Rectangle lower = new Rectangle(10f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(20f,0f, 10f, 10f);

		float overlap = lower.verticalOverlap(upper);

		assertEquals(0f, overlap, 0);
		assertTrue(!lower.verticallyOverlaps(upper));
		assertEquals(0f, lower.verticalOverlapRatio(upper), 0);
		assertEquals(0f, lower.overlapRatio(upper), 0);

	}

	@Test
	public void testGetVerticalOverlapShouldReturnMoreThanZero() {

		Rectangle lower = new Rectangle(15f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(20f, 0f, 10f, 10f);

		float overlap = lower.verticalOverlap(upper);

		assertEquals(5f, overlap, 0);
		assertTrue(lower.verticallyOverlaps(upper));
		assertEquals(0.5f, lower.verticalOverlapRatio(upper), 0);
		assertEquals(0f, lower.overlapRatio(upper), 0);

	}

	@Test
	public void testGetHorizontalOverlapShouldReturnZero() {

		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(10f, 10f, 10f, 10f);

		assertTrue(!one.horizontallyOverlaps(two));
		assertEquals(0f, one.overlapRatio(two), 0);

	}

	@Test
	public void testGetHorizontalOverlapShouldReturnMoreThanZero() {

		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(10f, 5f, 10f, 10f);

		assertTrue(one.horizontallyOverlaps(two));
		assertEquals(5f, one.horizontalOverlap(two), 0);
		assertEquals(0f, one.overlapRatio(two), 0);

	}

	@Test
	public void testGetOverlapShouldReturnMoreThanZero() {

		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(5f, 5f, 10f, 10f);

		assertTrue(one.horizontallyOverlaps(two));
		assertTrue(one.verticallyOverlaps(two));
		assertEquals(5f, one.horizontalOverlap(two), 0);
		assertEquals(5f, one.verticalOverlap(two), 0);
		assertEquals((25f/175), one.overlapRatio(two), 0);

	}

	@Test
	public void testMergeNoOverlappingRectangles() {

		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(0f, 10f, 10f, 10f);

		one.merge(two);

		assertEquals(20f, one.getWidth(), 0);
		assertEquals(10f, one.getHeight(), 0);
		assertEquals(0f, one.getLeft(), 0);
		assertEquals(0f, one.getTop(), 0);
		assertEquals(10f, one.getBottom(), 0);
		assertEquals(20f * 10f, one.getArea(), 0);

	}

	@Test
	public void testMergeOverlappingRectangles() {

		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(5f, 5f, 10f, 10f);

		one.merge(two);

		assertEquals(15f, one.getWidth(), 0);
		assertEquals(15f, one.getHeight(), 0);
		assertEquals(0f, one.getLeft(), 0);
		assertEquals(0f, one.getTop(), 0);

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

		Assert.assertArrayEquals(expectedPoints, points);

	}

	@Test
	public void testGetBoundingBox() {

		List<Rectangle> rectangles = new ArrayList<>();
		rectangles.add(new Rectangle(0f, 0f, 10f, 10f));
		rectangles.add(new Rectangle(20f, 30f, 10f, 10f));

		Rectangle boundingBoxOf = Rectangle.boundingBoxOf(rectangles);

		assertEquals(new Rectangle(0f, 0f, 40f, 30f), boundingBoxOf);




	}

	@Test
	public void testTransitiveComparison1() {
		// +-------+
		// |       |
		// |   A   | +-------+
		// |       | |       |
		// +-------+ |   B   | +-------+
		//           |       | |       |
		//           +-------+ |   C   |
		//                     |       |
		//                     +-------+
		Rectangle a = new Rectangle(0,0,2,2);
		Rectangle b = new Rectangle(1,1,2,2);
		Rectangle c = new Rectangle(2,2,2,2);
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(c) < 0);
		assertTrue(a.compareTo(c) < 0);
	}

	@Test @Ignore
	public void testTransitiveComparison2() {
		//                     +-------+
		//                     |       |
		//           +-------+ |   C   |
		//           |       | |       |
		// +-------+ |   B   | +-------+
		// |       | |       |
		// |   A   | +-------+
		// |       |
		// +-------+
		Rectangle a = new Rectangle(2,0,2,2);
		Rectangle b = new Rectangle(1,1,2,2);
		Rectangle c = new Rectangle(0,2,2,2);
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(c) < 0);
		assertTrue(a.compareTo(c) < 0);
	}

	@Test @Ignore
	public void testWellDefinedComparison1() {
		Rectangle a = new Rectangle(2,0,2,2);
		Rectangle b = new Rectangle(1,1,2,2);
		Rectangle c = new Rectangle(0,2,2,2);
		List<Rectangle> l1 = new ArrayList<>(Arrays.asList(b, a, c));
		List<Rectangle> l2 = new ArrayList<>(Arrays.asList(c, b, a));
		QuickSort.sort(l1, Rectangle.ILL_DEFINED_ORDER);
		QuickSort.sort(l2, Rectangle.ILL_DEFINED_ORDER);
		assertEquals(l1.get(0), l2.get(0));
		assertEquals(l1.get(1), l2.get(1));
		assertEquals(l1.get(2), l2.get(2));
	}

}
