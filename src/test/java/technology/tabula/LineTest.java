package technology.tabula;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class LineTest {

	@Test
	public void testSetTextElements() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		List<TextChunk> tList = new ArrayList<>();
		tList.add(tChunk);
		line.setTextElements(tList);
		
		assertThat(line.getTextElements().get(0).getText()).isEqualTo("test");
	}

	@Test
	public void testAddTextChunkIntTextChunk() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(3, tChunk);
		
		assertThat(line.getTextElements().get(3).getText()).isEqualTo("test");
	}
	
	@Test
	public void testLessThanAddTextChunkIntTextChunk() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(0, tChunk);
		line.addTextChunk(0, tChunk);
		
		assertThat(line.getTextElements().get(0).getText()).isEqualTo("testtest");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testErrorAddTextChunkIntTextChunk() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(-1, tChunk);
		fail("Illegal chunk");
	}
	
	@Test
	public void testToString() {
		Line line = new Line();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		line.addTextChunk(0, tChunk);
		line.addTextChunk(0, tChunk);
		
		assertThat(line.toString()).isEqualTo("technology.tabula.Line[x=0.0,y=0.0,w=0.0,h=0.0,bottom=0.000000,right=0.000000,chunks='testtest', ]");
	}

}
