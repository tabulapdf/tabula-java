package technology.tabula.extractors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	public String extract(String input_filepath) {
		IIORegistry.getDefaultInstance().registerServiceProvider(new TIFFImageWriterSpi(), ImageWriterSpi.class);
		ArrayList<ITesseract.RenderedFormat> list = new ArrayList<ITesseract.RenderedFormat>();
		list.add(ITesseract.RenderedFormat.PDF);

		// prepare input PDF - specify page subset
		// PdfUtilities.splitPdf("WellExample.pdf", "parsedInput.pdf", "1",
		// "1");
		File image;
		try {
			// rename document.pdf -> document_image.pdf
			//Path source = new File(input_filepath).toPath();
			//Files.move(source, source.resolveSibling("document_image.pdf"));
			
			//String document_imagePath = input_filepath.substring(0, input_filepath.length() - 4) + "_image.pdf";
			
			//File document_imageFile = new File(document_imagePath);
			//new File(input_filepath).renameTo(document_imageFile);	// document.pdf -> document_image.pdf
			
			// extract images from PDF
			image = PdfUtilities.convertPdf2Tiff(new File(input_filepath));
			ITesseract instance = new Tesseract();
			instance.setDatapath(LoadLibs.extractTessResources("tessdata").getParent());

			// output - create PDF from image above
			String image_filepath = image.getAbsolutePath();
			instance.createDocuments(image_filepath, input_filepath.substring(0, input_filepath.length() - 4), list);
			System.out.println("OCR Done");
			
//			new File(input_filepath.substring(0, input_filepath.length() - 4) + "_OCR.pdf").renameTo(new File(input_filepath));	// document_OCR.pdf -> document.pdf
			return "Success";
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println(input_filepath);
			System.out.println(input_filepath.substring(0, input_filepath.length() - 4) + "_image.pdf");
		}
		return "Failed";
	}
}