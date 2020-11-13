package technology.tabula;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;
import technology.tabula.text.Line;
import technology.tabula.text.TextChunk;
import technology.tabula.text.TextElement;

import static org.junit.Assert.assertEquals;

public class TestLine {

	@Test
	public void testSetTextElements() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(tChunk);
		
		assertEquals("test", line.getTextElements().get(0).getText());
	}

	@Test
	public void testAddTextChunkIntTextChunk() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(3, tChunk);
		
		assertEquals("test", line.getTextElements().get(3).getText());
		}
	
	@Test
	public void testLessThanAddTextChunkIntTextChunk() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(0, tChunk);
		line.addTextChunk(0, tChunk);
		
		assertEquals("testtest", line.getTextElements().get(0).getText());
		}
	
	@Test(expected = IllegalArgumentException.class)
	public void testErrorAddTextChunkIntTextChunk() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(-1, tChunk);
		}
	
	@Test
	public void testToString() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(0, tChunk);
		line.addTextChunk(0, tChunk);
		
		assertEquals("technology.tabula.text.Line[x=0.0,y=0.0,w=0.0,h=0.0,bottom=0.000000,right=0.000000,chunks='testtest', ]", line.toString());
	}

}
