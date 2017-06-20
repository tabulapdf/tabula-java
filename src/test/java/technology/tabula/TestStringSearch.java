package technology.tabula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import technology.tabula.detectors.StringSearch;

public class TestStringSearch {
	@Test
	public void testTwoString() {
		try {
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_textBased.pdf", 1);

			String[] inputs = new String[4];
			inputs[0] = "Arsenic";
			inputs[2] = "Zinc";
			StringSearch stringSearch = new StringSearch();
			List<Rectangle> stringRectangles = stringSearch.detect(page, inputs);
			
			assertEquals(1, stringRectangles.size());	// check that table was found
			
			Rectangle table = stringRectangles.get(0);
			
			// check that found table is correct, within tolerance
			assertTrue(Math.abs(table.height - 253) < 1);
			assertTrue(Math.abs(table.width - 612) < 1);
			assertTrue(Math.abs(table.x - 0) < 1);
			assertTrue(Math.abs(table.y - 392.8) < 1);

		} catch (IOException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testThreeString() {
		try {
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_textBased.pdf", 1);

			String[] inputs = new String[4];
			inputs[0] = "Arsenic";
			inputs[1] = "mg/L";
			inputs[2] = "Zinc";
			StringSearch stringSearch = new StringSearch();
			List<Rectangle> stringRectangles = stringSearch.detect(page, inputs);
			
			assertEquals(1, stringRectangles.size());	// check that table was found
			
			Rectangle table = stringRectangles.get(0);
			
			// check that found table is correct, within tolerance
			assertTrue(Math.abs(table.height - 257.5) < 1);
			assertTrue(Math.abs(table.width - 433) < 1);
			assertTrue(Math.abs(table.x - 51.3) < 1);
			assertTrue(Math.abs(table.y - 388.3) < 1);
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testFourString() {
		try {
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_textBased.pdf", 1);

			String[] inputs = new String[4];
			inputs[0] = "Arsenic";
			inputs[1] = "mg/L";
			inputs[2] = "Zinc";
			inputs[3] = "mg/L";
			StringSearch stringSearch = new StringSearch();
			List<Rectangle> stringRectangles = stringSearch.detect(page, inputs);
			
			assertEquals(1, stringRectangles.size());	// check that table was found
			
			Rectangle table = stringRectangles.get(0);
			
			// check that found table is correct, within tolerance
			assertTrue(Math.abs(table.height - 257.5) < 1);
			assertTrue(Math.abs(table.width - 433) < 1);
			assertTrue(Math.abs(table.x - 51.3) < 1);
			assertTrue(Math.abs(table.y - 388.3) < 1);
		} catch (IOException e) {
			fail(e.getMessage());
		} catch (ParseException e) {
			fail(e.getMessage());
		}
	}
}
