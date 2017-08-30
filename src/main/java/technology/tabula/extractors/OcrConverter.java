package technology.tabula.extractors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
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
	 * @param pageNumbers		a list of Integers of the pages that should be processed
	 * 							a null value will result in all pages being processed
	 * @return		boolean to indicate success or failure of text extraction
	 */
	public boolean extract(String input_filepath, List<Integer> pageNumbers) {
		Logger log = LoggerFactory.getLogger(OcrConverter.class);
		
		IIORegistry.getDefaultInstance().registerServiceProvider(new TIFFImageWriterSpi(), ImageWriterSpi.class);
		ArrayList<ITesseract.RenderedFormat> list = new ArrayList<ITesseract.RenderedFormat>();
		list.add(ITesseract.RenderedFormat.PDF);

		try {
			File singlePage = File.createTempFile("individual_page", null);
			File selectedPages = File.createTempFile("pages_to_ocr", ".pdf");
			File inputFile = new File(input_filepath);
			PDFMergerUtility pages_to_ocr = new PDFMergerUtility();

			if (pageNumbers == null) {
				// if no page numbers were given, OCR all pages
				selectedPages = inputFile;
			} else {
				// if page numbers were given, create PDF doc of subset of pages
				for (int page : pageNumbers) {
					PdfUtilities.splitPdf(inputFile, singlePage, page, page);
					pages_to_ocr.addSource(singlePage);
				}
				pages_to_ocr.setDestinationFileName(selectedPages.getAbsolutePath());
				pages_to_ocr.mergeDocuments(null);
			}

			// extract images from PDF
			File selectedImages = PdfUtilities.convertPdf2Tiff(selectedPages);
			ITesseract instance = new Tesseract();
			instance.setDatapath(LoadLibs.extractTessResources("tessdata").getParent());

			// output - create PDF from image above
			String selectedImages_filepath = selectedImages.getAbsolutePath();
			String selectedPages_filepath = selectedPages.getAbsolutePath();
			instance.createDocuments(selectedImages_filepath, selectedPages_filepath.substring(0, selectedPages_filepath.length() - 4), list);

			if (pageNumbers != null) {
				// reassemble into a single file
				PDFMergerUtility partially_ocrd_doc = new PDFMergerUtility();
				int total_pages = PDDocument.load(inputFile).getNumberOfPages();
				int ocr_page = 1;
				for (int page = 1; page <= total_pages; page++) {
					if (pageNumbers.contains(new Integer(page))) {
						// if the page was OCR'd, get the next OCR results page
						PdfUtilities.splitPdf(selectedPages, singlePage, ocr_page, ocr_page);
						partially_ocrd_doc.addSource(singlePage);
						ocr_page++;
					} else {
						// if the page wasn't OCR'd, grab this page from the original file
						PdfUtilities.splitPdf(inputFile, singlePage, page, page);
						partially_ocrd_doc.addSource(singlePage);
					}
				}

				// write these pages to the original file
				partially_ocrd_doc.setDestinationFileName(input_filepath);
				partially_ocrd_doc.mergeDocuments(null);
			}

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