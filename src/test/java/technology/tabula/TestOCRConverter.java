package technology.tabula;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import technology.tabula.extractors.OcrConverter;

public class TestOCRConverter {

	@Test
	public void testConvert() {
		try {
			// check for old version of OCR output
			Files.deleteIfExists(FileSystems.getDefault().getPath("src/test/resources/technology/tabula/wellExample_imageBased_OCR.pdf"));
			
			// convert document to text
			OcrConverter ocrConverter = new OcrConverter();
			boolean conversionResponse = ocrConverter.extract("src/test/resources/technology/tabula/wellExample_imageBased.pdf", true);
			assertTrue(conversionResponse);	// check for valid response
			
			// check that some text is as expected
			Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/wellExample_imageBased_OCR.pdf", 1);
			List<TextElement> textElements = page.getText();
			
			assertTrue(textElements.size() > 1200);	// check that text was extracted and is around approximate acceptable limit
													// this limit may change if Tesseract is updated
			
			// delete unneeded file
			Files.deleteIfExists(FileSystems.getDefault().getPath("src/test/resources/technology/tabula/wellExample_imageBased_OCR.pdf"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
