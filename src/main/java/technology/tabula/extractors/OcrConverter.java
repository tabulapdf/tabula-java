package technology.tabula.extractors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;

// Tess4j imports
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import net.sourceforge.tess4j.util.PdfUtilities;

public class OcrConverter {
	/**
	 * method to run OCR on a given input_file.
	 * @param input_filepath	file to image-based input PDF
	 * @param OCR_rename		boolean to determine if output should have "_OCR" appended to the filename
	 * @return		boolean to indicate success or failure of text extraction
	 */
	public boolean extract(String input_filepath) {
		Logger log = LoggerFactory.getLogger(OcrConverter.class);
		
		IIORegistry.getDefaultInstance().registerServiceProvider(new TIFFImageWriterSpi(), ImageWriterSpi.class);
		ArrayList<ITesseract.RenderedFormat> list = new ArrayList<ITesseract.RenderedFormat>();
		list.add(ITesseract.RenderedFormat.PDF);
		
		File image;
		try {
			// extract images from PDF
			image = PdfUtilities.convertPdf2Tiff(new File(input_filepath));
			ITesseract instance = new Tesseract();
			instance.setDatapath(LoadLibs.extractTessResources("tessdata").getParent());

			// output - create PDF from image above
			String image_filepath = image.getAbsolutePath();
			instance.createDocuments(image_filepath, input_filepath.substring(0, input_filepath.length() - 4), list);

			log.debug("OCR Done");
			return true;
		} catch (TesseractException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (SecurityException e) {
			log.error(e.getMessage());
		} catch (NullPointerException e) {
			log.error(e.getMessage());
		}
		return false;
	}
}