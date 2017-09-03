package technology.tabula;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Before;
import org.junit.Test;

public class TestProjectionProfile {
	
	ProjectionProfile pProfile;
	Page page;

	@Before
	public void setUpProjectionProfile() {
		PDPage pdPage = new PDPage();
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "test", 1f);
		TextElement textElement2 = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "test", 1f);
		List<TextElement> textList = new ArrayList<>();
		textList.add(textElement);
		textList.add(textElement2);

		Ruling ruling = new Ruling(0, 0, 10, 10);
		List<Ruling> rulingList = new ArrayList<>();
		rulingList.add(ruling);


		page = new Page(0, 0, 1, 1, 0, 1, pdPage, textList, rulingList);
		
		List<Rectangle> rectangles = new ArrayList<>();
		rectangles.add(new Rectangle(0f, 0f, 500f, 5f));
		
		pProfile = new ProjectionProfile(page, rectangles, 5, 5);
	}

	@Test
	public void testGetVerticalProjection() {
		float[] projection = pProfile.getVerticalProjection();
		assertTrue(projection.length == 10);
		}

	@Test
	public void testGetHorizontalProjection() {
		float[] projection = pProfile.getHorizontalProjection();
		assertTrue(projection.length == 10);
	}

	@Test
	public void testFindVerticalSeparators() {
		float[] seperators = pProfile.findVerticalSeparators(page.getText().size() * 2.5f);
		assertTrue(seperators.length == 0);
	}

	@Test
	public void testFindHorizontalSeparators() {
		float[] seperators = pProfile.findHorizontalSeparators(page.getText().size() * 2.5f);
		assertTrue(seperators.length == 0);
	}

	@Test
	public void testSmooth() {
		float[] data = {0, 1, 2};
		float[] rv = ProjectionProfile.smooth(data, 3);

		assertEquals(1f, rv[2], 1e-5);
	}

	@Test
	public void testFilter() {
		float[] data = {0, 1, 2};
		float[] rv = ProjectionProfile.filter(data, 3);

		assertEquals(3f, rv[1], 1e-5);
		}

	@Test
	public void testGetAutocorrelation() {
		float[] projection = {0, 1, 2};
		float[] rv = ProjectionProfile.getAutocorrelation(projection);

		assertEquals(0f, rv[0], 1e-5);
		assertTrue(rv.length == 2);

	}

	@Test
	public void testGetFirstDeriv() {
//		float[]
//		float[] projection = pProfile.getFirstDeriv(new float[]{0.0, 0.0)
//		System.out.println(Arrays.toString(projection));    
//		assertEquals(10, projection[0], 1e-15);
		}

}
