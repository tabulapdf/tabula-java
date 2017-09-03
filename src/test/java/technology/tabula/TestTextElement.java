package technology.tabula;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Assert;
import org.junit.Test;

public class TestTextElement {
	
	
	@Test
	public void createTextElement() {
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f);
		
		Assert.assertNotNull(textElement);
		Assert.assertEquals("A", textElement.getText());
		Assert.assertEquals(1f, textElement.getFontSize(), 0);
		Assert.assertEquals(15f, textElement.getLeft(), 0);
		Assert.assertEquals(5f, textElement.getTop(), 0);
		Assert.assertEquals(10f, textElement.getWidth(), 0);
		Assert.assertEquals(20f, textElement.getHeight(), 0);
		Assert.assertEquals(PDType1Font.HELVETICA, textElement.getFont());
		Assert.assertEquals(1f, textElement.getWidthOfSpace(), 0);
		Assert.assertEquals(0f, textElement.getDirection(), 0);
		
		
	}
	
	@Test
	public void createTextElementWithDirection() {
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f);
		
		Assert.assertNotNull(textElement);
		Assert.assertEquals("A", textElement.getText());
		Assert.assertEquals(1f, textElement.getFontSize(), 0);
		Assert.assertEquals(15f, textElement.getLeft(), 0);
		Assert.assertEquals(5f, textElement.getTop(), 0);
		Assert.assertEquals(10f, textElement.getWidth(), 0);
		Assert.assertEquals(20f, textElement.getHeight(), 0);
		Assert.assertEquals(PDType1Font.HELVETICA, textElement.getFont());
		Assert.assertEquals(1f, textElement.getWidthOfSpace(), 0);
		Assert.assertEquals(6f, textElement.getDirection(), 0);
		
		
	}
	
	@Test
	public void mergeFourElementsIntoFourWords() {
		
		List<TextElement> elements = new ArrayList<>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(20f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(40f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(60f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<>();
		expectedWords.add(new TextChunk(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f)));
		expectedWords.add(new TextChunk(new TextElement(20f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f)));
		expectedWords.add(new TextChunk(new TextElement(40f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f)));
		expectedWords.add(new TextChunk(new TextElement(60f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f)));
		
		Assert.assertEquals(expectedWords, words);
		
	}
	
	@Test
	public void mergeFourElementsIntoOneWord() {
		
		List<TextElement> elements = new ArrayList<>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<>();
		TextChunk textChunk = new TextChunk(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		textChunk.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		textChunk.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		textChunk.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		expectedWords.add(textChunk);
		
		Assert.assertEquals(expectedWords, words);
		
	}
	
	@Test
	public void mergeElementsShouldBeIdempotent() {
		/*
	   * a bug in TextElement.merge_words would delete the first TextElement in the array
	   * it was called with. Discussion here: https://github.com/tabulapdf/tabula-java/issues/78
	   */

		List<TextElement> elements = new ArrayList<>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.HELVETICA, 1f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		List<TextChunk> words2 = TextElement.mergeWords(elements);
		Assert.assertEquals(words, words2);
	}

	@Test
	public void mergeElementsWithSkippingRules() {
		
		List<TextElement> elements = new ArrayList<>();
		elements.add(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 17f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		elements.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		elements.add(new TextElement(0.001f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, " ", 1f, 6f));
		elements.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		elements.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.TIMES_ROMAN, 10f, "D", 1f, 6f));
		
		List<TextChunk> words = TextElement.mergeWords(elements);
		
		List<TextChunk> expectedWords = new ArrayList<>();
		TextChunk textChunk = new TextChunk(new TextElement(0f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "A", 1f, 6f));
		textChunk.add(new TextElement(0f, 25f, 10f, 20f, PDType1Font.HELVETICA, 1f, "B", 1f, 6f));
		textChunk.add(new TextElement(0f, 35f, 10f, 20f, PDType1Font.HELVETICA, 1f, "C", 1f, 6f));
		textChunk.add(new TextElement(0f, 45f, 10f, 20f, PDType1Font.TIMES_ROMAN, 10f, "D", 1f, 6f));
		expectedWords.add(textChunk);
		
		Assert.assertEquals(expectedWords, words);
		
	}
	
	@Test
	public void mergeTenElementsIntoTwoWords() {
		
		List<TextElement> elements = new ArrayList<>();
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
		
		List<TextChunk> expectedWords = new ArrayList<>();
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
		
		Assert.assertEquals(2, words.size());
		Assert.assertEquals(expectedWords, words);
		
	}
	
	@Test
	public void mergeTenElementsIntoTwoLines() {
		
		List<TextElement> elements = new ArrayList<>();
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
		
		List<TextChunk> expectedWords = new ArrayList<>();
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
		
		Assert.assertEquals(2, words.size());
		Assert.assertEquals(expectedWords, words);
		
	}	
	
	

}
