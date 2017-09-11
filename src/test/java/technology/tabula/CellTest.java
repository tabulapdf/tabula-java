package technology.tabula;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CellTest {

	@Test
	public void testIsSpanning() {
		Cell cell = new Cell(0, 0, 0, 0);
		assertThat(cell.isSpanning()).isFalse();
		cell.setSpanning(true);
		assertThat(cell.isSpanning()).isTrue();
	}

	@Test
	public void testIsPlaceholder() {
		Cell cell = new Cell(0, 0, 0, 0);
		assertThat(cell.isPlaceholder()).isFalse();
		cell.setPlaceholder(true);
		assertThat(cell.isPlaceholder()).isTrue();
	}

	@Test
	public void testGetTextElements() {
		Cell cell = new Cell(0, 0, 0, 0);
		assertThat(cell.getTextElements()).isEmpty();
		
		TextElement tElement = new TextElement(0, 0, 0, 0, PDType1Font.HELVETICA_BOLD, 10, "test", 5);
		TextChunk tChunk = new TextChunk(tElement);
		List<TextChunk> tList = new ArrayList<>();
		tList.add(tChunk);
		cell.setTextElements(tList);
		
		assertThat(cell.getTextElements().get(0).getText()).isEqualTo("test");
	}
}
