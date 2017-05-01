package technology.tabula.extractors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

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
	 * @return		string to indicate success or failure of text extraction
	 */
	public String extract(String input_filepath, boolean OCR_rename) {
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
			if (OCR_rename) {
				instance.createDocuments(image_filepath, input_filepath.substring(0, input_filepath.length() - 4) + "_OCR", list);
			} else {
				instance.createDocuments(image_filepath, input_filepath.substring(0, input_filepath.length() - 4), list);
			}
			System.out.println("OCR Done");
			return "Success";
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return "Failed";
	}
}