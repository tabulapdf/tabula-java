package technology.tabula;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ProjectionProfileTest {
	
	private ProjectionProfile pProfile;
	private Page page;

	@Before
	public void setUpProjectionProfile() {
		PDPage pdPage = new PDPage();
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "test", 1f);
		TextElement textElement2 = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "test", 1f);
		List<TextElement> textList = new ArrayList<TextElement>();
		textList.add(textElement);
		textList.add(textElement2);

		Ruling ruling = new Ruling(0, 0, 10, 10);
		List<Ruling> rulingList = new ArrayList<>();
		rulingList.add(ruling);

		page = new Page(0, 0, 1, 1, 0, 1, pdPage, textList, rulingList);
		
		List<Rectangle> rectangles = new ArrayList<Rectangle>();
		rectangles.add(new Rectangle(0f, 0f, 500f, 5f));
		
		pProfile = new ProjectionProfile(page, rectangles, 5, 5);
	}

	@Test
	public void testGetVerticalProjection() {
		float[] projection = pProfile.getVerticalProjection();
		assertThat(projection).hasSize(10);
		}

	@Test
	public void testGetHorizontalProjection() {
		float[] projection = pProfile.getHorizontalProjection();
		assertThat(projection).hasSize(10);
	}

	@Test
	public void testFindVerticalSeparators() {
		float[] seperators = pProfile.findVerticalSeparators(page.getText().size() * 2.5f);
		assertThat(seperators).isEmpty();
	}

	@Test
	public void testFindHorizontalSeparators() {
		float[] seperators = pProfile.findHorizontalSeparators(page.getText().size() * 2.5f);
		assertThat(seperators).isEmpty();
	}

	@Test
	public void testSmooth() {
		float[] data = {0, 1, 2};
		float[] rv = ProjectionProfile.smooth(data, 3);

		assertThat(rv[2]).isCloseTo(1f, within(1e-5f));
	}

	@Test
	public void testFilter() {
		float[] data = {0, 1, 2};
		float[] rv = ProjectionProfile.filter(data, 3);

		assertThat(rv[1]).isCloseTo(3f, within(1e-5f));
		}

	@Test
	public void testGetAutocorrelation() {
		float[] projection = {0, 1, 2};
		float[] rv = ProjectionProfile.getAutocorrelation(projection);

		assertThat(rv[0]).isCloseTo(0f, within(1e-5f));
		assertThat(rv).hasSize(2);

	}

	@Test
	public void testGetFirstDeriv() {
//		float[]
//		float[] projection = pProfile.getFirstDeriv(new float[]{0.0, 0.0)
//		System.out.println(Arrays.toString(projection));    
//		assertThat(projection[0]).isCloseTo(10, within(1e-15));
		}

}
