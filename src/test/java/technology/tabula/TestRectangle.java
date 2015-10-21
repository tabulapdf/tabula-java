package technology.tabula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		Rectangle six = new Rectangle(169.2100067138672f, 161.16000366210938f, 4.329999923706055f, 4.309999942779541f); //R
		
		List<Rectangle> expectedList = new ArrayList<Rectangle>();
		expectedList.add(first);
		expectedList.add(six);
		expectedList.add(second);
		expectedList.add(third);
		expectedList.add(fourth);
		expectedList.add(fifth);
		List<Rectangle> toSortList = new ArrayList<Rectangle>();
		toSortList.add(six);
		toSortList.add(second);
		toSortList.add(third);
		toSortList.add(fifth);
		toSortList.add(first);
		toSortList.add(fourth);
		
		Collections.sort(toSortList);
		
		assertEquals(expectedList, toSortList);
	}
	
	@Test
	public void testGetVerticalOverlapShouldReturnZero() {
		
		Rectangle lower = new Rectangle(10f, 0f, 10f, 10f);
		Rectangle upper = new Rectangle(20f,0f, 10f, 10f);
		
		float overlap = lower.verticalOverlap(upper);
		
		assertEquals(0f, overlap, 0);
		assertTrue(!lower.verticallyOverlaps(upper));
			
	}
	
	@Test
	public void testGetVerticalOverlapShouldReturnMoreThanZero() {
		
		Rectangle lower = new Rectangle(15f, 10f, 10f, 10f);
		Rectangle upper = new Rectangle(20f, 0f, 10f, 10f);
		
		float overlap = lower.verticalOverlap(upper);
		
		assertEquals(5f, overlap, 0);
		assertTrue(lower.verticallyOverlaps(upper));
			
	}
	
	@Test
	public void testGetHorizontalOverlapShouldReturnZero() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(10f, 10f, 10f, 10f);
		
		assertTrue(!one.horizontallyOverlaps(two));
		assertEquals(0f, one.horizontalOverlapRatio(two), 0);
			
	}
	
	@Test
	public void testGetHorizontalOverlapShouldReturnMoreThanZero() {
		
		Rectangle one = new Rectangle(0f, 0f, 10f, 10f);
		Rectangle two = new Rectangle(10f, 5f, 10f, 10f);
		
		assertTrue(one.horizontallyOverlaps(two));
		assertEquals(5f, one.horizontalOverlap(two), 0);
		assertEquals(0f, one.horizontalOverlapRatio(two), 0);
			
	}
	
}
