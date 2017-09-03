package technology.tabula;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

public class TestCell {

	@Test
	public void testIsSpanning() {
		Cell cell = new Cell(0, 0, 0, 0);
		assertFalse(cell.isSpanning());
		cell.setSpanning(true);
		assertTrue(cell.isSpanning());
	}

	@Test
	public void testIsPlaceholder() {
		Cell cell = new Cell(0, 0, 0, 0);
		assertFalse(cell.isPlaceholder());
		cell.setPlaceholder(true);
		assertTrue(cell.isPlaceholder());
		}

	@Test
	public void testGetTextElements() {
		Cell cell = new Cell(0, 0, 0, 0);
		assertTrue(cell.getTextElements().isEmpty());
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		List<TextChunk> tList = new ArrayList<>();
		tList.add(tChunk);
		cell.setTextElements(tList);
		
		assertEquals("test", cell.getTextElements().get(0).getText());
		
		
		}

}
