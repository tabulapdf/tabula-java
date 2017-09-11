package technology.tabula;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class RulingTest {
	
	private Ruling ruling;
	
	@Before
	public void setUpRuling() {
		ruling = new Ruling(0, 0, 10, 10);
	}

	@Test
	public void testGetWidth() {
		assertThat(ruling.getWidth()).isCloseTo(10f, within(1e-5f));
	}

	@Test
	public void testGetHeight() {
		assertThat(ruling.getHeight()).isCloseTo(10f, within(1e-5f));
	}

	@Test
	public void testToString() {
		assertThat(ruling.toString()).isEqualTo("class technology.tabula.Ruling[x1=0.000000 y1=0.000000 x2=10.000000 y2=10.000000]");
	}
	
	@Test
	public void testEqualsOther() {
		Ruling other = new Ruling(0, 0, 11, 10);
		// TODO
//		assertThat(ruling.equals(ruling)).isTrue();
	}
	
	@Test
	public void testEqualsDifferentInstance() {
		assertThat(ruling).isNotEqualTo("test");
	}
	
	@Test
	public void testNearlyIntersects(){
		Ruling another = new Ruling(0, 0, 11, 10);

		assertThat(ruling.nearlyIntersects(another)).isTrue();
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetPositionError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.getPosition();
		fail("unsupportedOperation");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testSetPositionError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.setPosition(5f);
		fail("UnsupportedOperation");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testsetPosition(){
		ruling.setPosition(0);
		fail("UnsupportedOperation");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetStartError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.getStart();
		fail("UnsupportedOperation");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testGetEndError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.getEnd();
		fail("UnsupportedOperation");
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testSetEndError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		other.setEnd(5f);
		fail("UnsupportedOperation");
	}
	
	
	@Test
	public void testColinear(){
//		Ruling another = new Ruling(0, 0, 500, 5);
		java.awt.geom.Point2D.Float float1 = new java.awt.geom.Point2D.Float(20, 20);
		java.awt.geom.Point2D.Float float2 = new java.awt.geom.Point2D.Float(0, 0);
		java.awt.geom.Point2D.Float float3 = new java.awt.geom.Point2D.Float(20, 0);
		java.awt.geom.Point2D.Float float4 = new java.awt.geom.Point2D.Float(0, 20);
		
		assertThat(ruling.colinear(float1)).isFalse();
		assertThat(ruling.colinear(float2)).isTrue();
		assertThat(ruling.colinear(float3)).isFalse();
		assertThat(ruling.colinear(float4)).isFalse();
	}
}
