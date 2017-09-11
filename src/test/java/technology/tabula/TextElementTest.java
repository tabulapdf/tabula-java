package technology.tabula;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TextElementTest {
	
	
	@Test
	public void createTextElement() throws IOException {
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f);
		
		assertThat(textElement).isNotNull();
		assertThat(textElement.getText()).isEqualTo("A");
		assertThat(textElement.getFontSize()).isEqualTo(1f);
		assertThat(textElement.getLeft()).isEqualTo(15f);
		assertThat(textElement.getTop()).isEqualTo(5f);
		assertThat(textElement.getWidth()).isEqualTo(10f);
		assertThat(textElement.getHeight()).isEqualTo(20f);
		assertThat(textElement.getFont()).isEqualTo(PDType1Font.HELVETICA);
		assertThat(textElement.getWidthOfSpace()).isEqualTo(1f);
		assertThat(textElement.getDirection()).isEqualTo(0f);
		
		
	}
	
	@Test
	public void createTextElementWithDirection() throws IOException {
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f);
		
		assertThat(textElement).isNotNull();
		assertThat(textElement.getText()).isEqualTo("A");
		assertThat(textElement.getFontSize()).isEqualTo(1f);
		assertThat(textElement.getLeft()).isEqualTo(15f);
		assertThat(textElement.getTop()).isEqualTo(5f);
		assertThat(textElement.getWidth()).isCloseTo(10f, within(0d));
		assertThat(textElement.getHeight()).isCloseTo(20f, within(0d));
		assertThat(textElement.getFont()).isEqualTo(PDType1Font.HELVETICA);
		assertThat(textElement.getWidthOfSpace()).isEqualTo(1f);
		assertThat(textElement.getDirection()).isEqualTo(6f);
		
		
	}
	
	@Test
	public void mergeFourElementsIntoFourWords() {
		
		List<TextElement> elements = new ArrayList<TextElement>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(20f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(40f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(60f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<TextChunk>();
		expectedWords.add(new TextChunk(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f)));
		expectedWords.add(new TextChunk(new TextElement(20f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f)));
		expectedWords.add(new TextChunk(new TextElement(40f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f)));
		expectedWords.add(new TextChunk(new TextElement(60f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f)));
		
		assertThat(words).isEqualTo(expectedWords);
		
	}
	
	@Test
	public void mergeFourElementsIntoOneWord() {
		
		List<TextElement> elements = new ArrayList<TextElement>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<TextChunk>();
		TextChunk textChunk = new TextChunk(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		textChunk.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		textChunk.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		textChunk.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		expectedWords.add(textChunk);
		
		assertThat(words).isEqualTo(expectedWords);
		
	}
	
	@Test
	public void mergeElementsShouldBeIdempotent() {
		/*
	   * a bug in TextElement.merge_words would delete the first TextElement in the array
	   * it was called with. Discussion here: https://github.com/tabulapdf/tabula-java/issues/78
	   */

		List<TextElement> elements = new ArrayList<TextElement>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		List<TextChunk> words2 = TextElement.mergeWords(elements);

		assertThat(words2).isEqualTo(words);
	}

	@Test
	public void mergeElementsWithSkippingRules() {
		
		List<TextElement> elements = new ArrayList<TextElement>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 17f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(0.001f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, " ", 1f, 6f));
		elements.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.TIMES_ROMAN, 10f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<TextChunk>();
		TextChunk textChunk = new TextChunk(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		textChunk.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		textChunk.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		textChunk.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.TIMES_ROMAN, 10f, "D", 1f, 6f));
		expectedWords.add(textChunk);
		
		assertThat(words).isEqualTo(expectedWords);
		
	}
	
	@Test
	public void mergeTenElementsIntoTwoWords() {
		
		List<TextElement> elements = new ArrayList<TextElement>();
		elements.add(new TextElement(0f, 0f, 10f, 20f, PDType1Font.HELVETICA, 1f, "H", 1f, 6f));
		elements.add(new TextElement(0f, 10f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		elements.add(new TextElement(0f, 20f, 10f, 20f, PDType1Font.HELVETICA, 1f, "L", 1f, 6f));
		elements.add(new TextElement(0f, 30f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 60f, 10f, 20f, PDType1Font.HELVETICA, 1f, "M", 1f, 6f));
		elements.add(new TextElement(0f, 70f, 10f, 20f, PDType1Font.HELVETICA, 1f, "U", 1f, 6f));
		elements.add(new TextElement(0f, 80f, 10f, 20f, PDType1Font.HELVETICA, 1f, "N", 1f, 6f));
		elements.add(new TextElement(0f, 90f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		elements.add(new TextElement(0f, 100f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<TextChunk>();
		TextChunk textChunk = new TextChunk(new TextElement(0f, 0f, 10f, 20f, PDType1Font.HELVETICA, 1f, "H", 1f, 6f));
		textChunk.add(new TextElement(0f, 10f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		textChunk.add(new TextElement(0f, 20f, 10f, 20f, PDType1Font.HELVETICA, 1f, "L", 1f, 6f));
		textChunk.add(new TextElement(0f, 30f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		textChunk.add(new TextElement(0f, 30f, 10.5f, 20f, PDType1Font.HELVETICA, 1f, " ", 1f)); //Check why width=10.5?
		expectedWords.add(textChunk);

		TextChunk textChunk2 = new TextChunk(new TextElement(0f, 60f, 10f, 20f, PDType1Font.HELVETICA, 1f, "M", 1f, 6f));
		textChunk2.add(new TextElement(0f, 70f, 10f, 20f, PDType1Font.HELVETICA, 1f, "U", 1f, 6f));
		textChunk2.add(new TextElement(0f, 80f, 10f, 20f, PDType1Font.HELVETICA, 1f, "N", 1f, 6f));
		textChunk2.add(new TextElement(0f, 90f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		textChunk2.add(new TextElement(0f, 100f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		expectedWords.add(textChunk2);
		
		assertThat(words).hasSize(2);
		assertThat(words).isEqualTo(expectedWords);
		
	}
	
	@Test
	public void mergeTenElementsIntoTwoLines() {
		
		List<TextElement> elements = new ArrayList<TextElement>();
		elements.add(new TextElement(0f, 0f, 10f, 20f, PDType1Font.HELVETICA, 1f, "H", 1f, 6f));
		elements.add(new TextElement(0f, 10f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		elements.add(new TextElement(0f, 20f, 10f, 20f, PDType1Font.HELVETICA, 1f, "L", 1f, 6f));
		elements.add(new TextElement(0f, 30f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(20f, 0f, 10f, 20f, PDType1Font.HELVETICA, 1f, "M", 1f, 6f));
		elements.add(new TextElement(20f, 10f, 10f, 20f, PDType1Font.HELVETICA, 1f, "U", 1f, 6f));
		elements.add(new TextElement(20f, 20f, 10f, 20f, PDType1Font.HELVETICA, 1f, "N", 1f, 6f));
		elements.add(new TextElement(20f, 30f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		elements.add(new TextElement(20f, 40f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<TextChunk>();
		TextChunk textChunk = new TextChunk(new TextElement(0f, 0f, 10f, 20f, PDType1Font.HELVETICA, 1f, "H", 1f, 6f));
		textChunk.add(new TextElement(0f, 10f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		textChunk.add(new TextElement(0f, 20f, 10f, 20f, PDType1Font.HELVETICA, 1f, "L", 1f, 6f));
		textChunk.add(new TextElement(0f, 30f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		expectedWords.add(textChunk);

		TextChunk textChunk2 = new TextChunk(new TextElement(20f, 0f, 10f, 20f, PDType1Font.HELVETICA, 1f, "M", 1f, 6f));
		textChunk2.add(new TextElement(20f, 10f, 10f, 20f, PDType1Font.HELVETICA, 1f, "U", 1f, 6f));
		textChunk2.add(new TextElement(20f, 20f, 10f, 20f, PDType1Font.HELVETICA, 1f, "N", 1f, 6f));
		textChunk2.add(new TextElement(20f, 30f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		textChunk2.add(new TextElement(20f, 40f, 10f, 20f, PDType1Font.HELVETICA, 1f, "O", 1f, 6f));
		expectedWords.add(textChunk2);
		
		assertThat(words).hasSize(2);
		assertThat(words).isEqualTo(expectedWords);
	}
}
