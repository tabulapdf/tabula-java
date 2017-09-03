package technology.tabula;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

public class TestLine {

	@Test
	public void testSetTextElements() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		List<TextChunk> tList = new ArrayList<>();
		tList.add(tChunk);
		line.setTextElements(tList);
		
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
		
		assertEquals("technology.tabula.Line[x=0.0,y=0.0,w=0.0,h=0.0,bottom=0.000000,right=0.000000,chunks='testtest', ]", line.toString());
	}

}
