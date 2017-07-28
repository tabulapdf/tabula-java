package technology.tabula;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestRuling {
	
	Ruling ruling;
	
	@Before
	public void setUpRuling() {
		ruling = new Ruling(0, 0, 10, 10);
	}

	@Test
	public void testGetWidth() {
		assertEquals(10f, ruling.getWidth(), 1e-5);
	}

	@Test
	public void testGetHeight() {
		assertEquals(10f, ruling.getHeight(), 1e-5);
	}

	@Test
	public void testToString() {
		assertEquals("class technology.tabula.Ruling[x1=0.000000 y1=0.000000 x2=10.000000 y2=10.000000]",ruling.toString());
	}
	
	@Test
	public void testEqualsOther() {
		Ruling other = new Ruling(0, 0, 11, 10);
		assertTrue(ruling.equals(ruling));
	}
	
	@Test
	public void testEqualsDifferentInstance() {
		assertFalse(ruling.equals("test"));
	}
	
	@Test
	public void testNearlyIntersects(){
		Ruling another = new Ruling(0, 0, 11, 10);

		assertTrue(ruling.nearlyIntersects(another));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetPositionError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.getPosition();
		fail();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testSetPositionError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.setPosition(5f);
		fail();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testsetPosition(){
		ruling.setPosition(0);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetStartError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.getStart();
		fail();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetEndError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.getEnd();
		fail();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testSetEndError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.setEnd(5f);
		fail();
	}
	
	
	@Test
	public void testColinear(){
//		Ruling another = new Ruling(0, 0, 500, 5);
		java.awt.geom.Point2D.Float float1 = new java.awt.geom.Point2D.Float(20, 20);
		java.awt.geom.Point2D.Float float2 = new java.awt.geom.Point2D.Float(0, 0);
		java.awt.geom.Point2D.Float float3 = new java.awt.geom.Point2D.Float(20, 0);
		java.awt.geom.Point2D.Float float4 = new java.awt.geom.Point2D.Float(0, 20);
		
		assertFalse(ruling.colinear(float1));
		assertTrue(ruling.colinear(float2));
		assertFalse(ruling.colinear(float3));
		assertFalse(ruling.colinear(float4));


	}

}
