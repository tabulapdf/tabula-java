package technology.tabula;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import technology.tabula.extractors.OcrConverter;

public class TestOCRConverter {

	@Test
	public void testConvert() {
		OcrConverter myConverter = new OcrConverter();
		String conversionResponse = myConverter
				.extract("src/test/resources/technology/tabula/wellExample_imageBased.pdf", true);
		try {
			Page page = UtilsForTesting.getPage(conversionResponse, 0);
			List<TextElement> textElements = page.getText();
			assert textElements.size() == 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
