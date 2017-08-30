package technology.tabula;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import technology.tabula.extractors.OcrConverter;

public class TestOCRConverter {

	@Test
	public void testConvert() {
		try {
			// create backup of wellExample_imageBased.pdf
			File tmpFile = Files.createTempFile("", ".tmp").toFile();
			File testFile = new File("src/test/resources/technology/tabula/wellExample_imageBased.pdf");
			FileUtils.copyFile(testFile, tmpFile);
			
			// convert document to text
			OcrConverter ocrConverter = new OcrConverter();
			boolean conversionResponse = ocrConverter.extract(testFile.getAbsolutePath(), null);
			assertTrue(conversionResponse);	// check for valid response
			
			// check that some text is as expected
			Page page = UtilsForTesting.getPage(testFile.getAbsolutePath(), 1);
			List<TextElement> textElements = page.getText();
			
			assertTrue(textElements.size() > 1200);	// check that text was extracted and is around approximate acceptable limit
													// this limit may change if Tesseract is updated
			
			// restore original copy of wellExample_imageBased.pdf
			FileUtils.copyFile(tmpFile, testFile);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
