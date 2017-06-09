package technology.tabula;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import technology.tabula.extractors.StringSearch;

public class TestStringSearch {

	@Test
	public void testSingleString() {
		try {
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_textBased.pdf", 0);
			
			String[] inputs = new String[4];
			input[0] = "Arsenic";
			StringSearch stringSearch = new StringSearch();

			List<Rectangle> StringRectangles = stringSearch.detect(page, input);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testTwoString() {
		try {
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_textBased.pdf", 0);
			
			String[] inputs = new String[4];
			input[0] = "Arsenic";
			input[2] = "Zinc";
			StringSearch stringSearch = new StringSearch();
			List<Rectangle> StringRectangles = stringSearch.detect(page, input);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFourString() {
		try {
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_textBased.pdf", 0);
			input[0] = "Arsenic";
			input[1] = "mg/L"
			input[2] = "Zinc";
			String[] inputs = new String[4];
			input[0] = "Arsenic";
			StringSearch stringSearch = new StringSearch();
			List<Rectangle> StringRectangles = stringSearch.detect(page, input);
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
