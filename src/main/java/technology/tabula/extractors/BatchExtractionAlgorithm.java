package technology.tabula.extractors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Rectangle;
import technology.tabula.Table;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.detectors.StringSearch;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;

// NOTES:
//		need to remove tables from auto/spread list if they are used as a best guess
//		or remove very similar tables from the list before extracting data
public class BatchExtractionAlgorithm {
	public Map<String, List<Table>> extract(String inputPath, String jsonPath, String processType,
			boolean ocrAllowed, int overlapThreshold) {
		Logger log = LoggerFactory.getLogger(BatchExtractionAlgorithm.class);

		File parentPath;
		List<String[]> stringList = new ArrayList<String[]>();
		List<String> pageList = new ArrayList<String>();
		List<Rectangle> coordList = new ArrayList<Rectangle>();

		try {
			// ----------------------------------------------------------
			// Confirm process type is valid
			// ----------------------------------------------------------

			if (!processType.equals("both") && !processType.equals("coords") && !processType.equals("string")) {
				// invalid process type
				log.error("Process type invalid");
				return null;
			}

			// ----------------------------------------------------------
			// Confirm input and output paths exist
			// ----------------------------------------------------------

			File inputFile = new File(inputPath); // inputPath could point to
													// pdf or directory of pdfs
													// currently does not check
													// subfolders

	

			if (!inputFile.exists()) {
				throw new FileNotFoundException("Input file or directory does not exist");
			}

			// get folder containing template
			if (!inputFile.isDirectory()) {
				parentPath = new File(inputFile.getAbsoluteFile().getParent());
			} else {
				parentPath = inputFile;
			}

			if (!parentPath.isDirectory()) {
				throw new FileNotFoundException("Invalid file path");
			}

			// ----------------------------------------------------------
			// Confirm json file is valid
			// ----------------------------------------------------------

			// STILL NEED TO CHECK THAT PARSED FILE IS VALID
			FileReader fr = new FileReader(jsonPath);
			BufferedReader br = new BufferedReader(fr);

			String currentString;

			if (processType.equals("string")) {

				while ((currentString = br.readLine()) != null) {
					// split the input stream
					String[] splitString = currentString.split(",");

					if (splitString.length > 4) {
						log.debug("Too many arguments for String Search: " + splitString);
						continue;
					}

					String[] addString = new String[4];

					// copy to add string
					for (int i = 0; i < splitString.length; i++) {
						addString[i] = splitString[i];
					}

					// empty null fields
					for (int i = 0; i < addString.length; i++) {
						if (addString[i] == null)
							addString[i] = "";
					}

					// what happens if line doesnt match expectation?
					stringList.add(addString); // split("[\\s,]+")));

					// if string is invalid, don't continue searching?
				}

				// if the stringList is empty, don't continue searching?
			}

			else if (processType.equals("coords")) {
				while ((currentString = br.readLine()) != null) {
					String array[] = currentString.split("[\\s,]+");

					pageList.add(array[0]);
					float leftBound = Float.valueOf(array[1]);
					float topBound = Float.valueOf(array[2]);
					float width = Float.valueOf(array[3]);
					float height = Float.valueOf(array[4]);

					coordList.add(new Rectangle(topBound, leftBound, width, height));
				}

				// if the coordList is empty, don't continue searching?
			}

			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			return null;
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		}

		// ----------------------------------------------------------
		// Scan through input folder for PDF documents
		// ----------------------------------------------------------

		// Create list of files in input directory
		File[] fileList = parentPath.listFiles();
		Map<String, List<Table>> FileTables = new HashMap<String, List<Table>>();
		List<String> deleteList = new ArrayList<String>();

		// iterate over all files in directory
		// does not go into subfolders yet
		for (File currentFile : fileList) {
			// get file name without path
			String fileName = currentFile.getName();

			// get file extension
			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

			// could check if file is directory for subfolder scanning
			if (extension.equals("pdf")) {
				log.debug(fileName + " is a valid PDF file.");

				try {
					PDDocument pdfDocument = PDDocument.load(currentFile);
					boolean reading = true;

					List<Table> tables = new ArrayList<Table>();
					List<String> tableHeaders = new ArrayList<String>();

					ObjectExtractor extractor = new ObjectExtractor(pdfDocument);
					BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
					PageIterator pageIterator = extractor.extract();

					// iterator over each page and search for table with identifiers
					// extract and append data to table if found
					// scan assuming that document is text based
					boolean textFound = scanDocForMatchedTables(basicExtractor, pageIterator, overlapThreshold,
							processType, stringList, coordList, pageList, tables, tableHeaders);

					pdfDocument.close();
					reading = false;

					// try to OCR document if allowed and no text found in
					// original document
					if (!textFound) {
						log.debug("Possible image based document: " + currentFile.getAbsolutePath());

						// attempt to OCR document and process again
						if (ocrAllowed) {
							log.debug("Attempting to convert document to text based format...");

							try {
								// copy file to temporary directory before extraction
								File ocrFile = Files.createTempFile("ocr_", ".pdf").toFile();
								FileUtils.copyFile(currentFile, ocrFile);

								OcrConverter ocr = new OcrConverter();
								ocr.extract(ocrFile.getAbsolutePath());

								deleteList.add(ocrFile.getAbsolutePath());

								pdfDocument = PDDocument.load(ocrFile);
								reading = true;
								extractor = new ObjectExtractor(pdfDocument);
								pageIterator = extractor.extract();
								textFound = scanDocForMatchedTables(basicExtractor, pageIterator, overlapThreshold,
										processType, stringList, coordList, pageList, tables, tableHeaders);

								pdfDocument.close();

								reading = false;
							} catch (IOException e) {
								log.error("Unable to properly convert or extract data from document");
							}

						} else
							log.debug("Ignored document based on input parameters");
					}

					// just in case
					if (reading) {
						pdfDocument.close();
						reading = false;
					}

					FileTables.put(fileName, tables);
				} catch (IOException e) {
					log.error(e.getMessage());
					continue;
				}
			} // end of current pdf processing
		} // end of file list

		if (!deleteList.isEmpty())
			log.debug("Deleting OCR'd files...");
		// clean up created OCR files
		for (String ocrPath : deleteList) {
			try {
				File ocrFile = new File(ocrPath);
				ocrFile.delete();
			} catch (SecurityException e) {
				log.error(e.getMessage());
			}

		}
		return FileTables;
	}

	// should be rewritten so that fewer paramters are required
	// more method calls? globals?
	private boolean scanDocForMatchedTables(BasicExtractionAlgorithm basicExtractor, PageIterator pageIterator,
			int overlapThreshold, String processType, List<String[]> stringList, List<Rectangle> coordList,
			List<String> pageList, List<Table> tables, List<String> tableHeaders) {
		boolean textFound = false;

		int tableNumber = 1;

		while (pageIterator.hasNext()) {
			Page page = pageIterator.next();

			if (page.hasText()) {
				textFound = true;
			} else
				continue;

			if (processType.equals("string")) {
				for (int i = 0; i < stringList.size(); i++) {
					String[] identifiers = stringList.get(i);

					// get list of rectangles which match string inputs
					StringSearch stringSearch = new StringSearch();

					List<Rectangle> guesses = null;

					// one string version?
					guesses = stringSearch.detect(page, identifiers);

					if (guesses.isEmpty()) {
						continue; // go to next page, do not
									// attempt to extract data
					}

					// get auto detected list of rectangles
					NurminenDetectionAlgorithm nurmDetect = new NurminenDetectionAlgorithm();
					List<Rectangle> auto = nurmDetect.detect(page);

					// try spreadsheet in case auto doesnt work
					SpreadsheetDetectionAlgorithm spreadDetect = new SpreadsheetDetectionAlgorithm();
					List<Rectangle> spread = spreadDetect.detect(page);

					// compare rectangle lists, create worklist
					// by removing elements from both lists and
					// using auto detect
					List<Rectangle> worklist = new ArrayList<Rectangle>();

					float bestOverlap = (float) overlapThreshold / 100;
					Rectangle bestGuess = null;

					for (int j = 0; j < guesses.size(); j++) {
						Rectangle eval = guesses.get(j);
						bestGuess = eval;

						if (!auto.isEmpty()) {
							for (int k = 0; k < auto.size(); k++) {
								Rectangle eval1 = auto.get(k);

								// look for rectangle overlap
								float overlap = eval.overlapRatio(eval1);

								if (overlap > bestOverlap) {
									bestOverlap = overlap;
									bestGuess = eval1;
								}
							}
						}

						// try spreadsheet?
						if (!spread.isEmpty()) {
							for (int k = 0; k < spread.size(); k++) {
								Rectangle eval2 = spread.get(k);

								// look for rectangle overlap
								float overlap = eval.overlapRatio(eval2);

								if (overlap > bestOverlap) {
									bestOverlap = overlap;
									bestGuess = eval2;
								}
							}
						}

						if (!bestGuess.isEmpty()) {
							worklist.add(bestGuess);
						}
					}

					// todo: determine if table has been added to the worklist
					// multiple times based on overlap
					for (Rectangle guessRect : worklist) {
						Page guess = page.getArea(guessRect);
						// may want to add a break after each
						// table is appended

						// add table header?
						tableHeaders.add("Table #" + tableNumber + "  ul: " + identifiers[0] + "  ur: " + identifiers[1]
								+ "  ll: " + identifiers[2] + "  lr: " + identifiers[3]);

						// add new table
						tables.addAll(basicExtractor.extract(guess));

						tableNumber++;
					}
				}
			} else if (processType.equals("coords")) {
				for (int i = 0; i < coordList.size(); i++) {
					if (page.getPageNumber() == Integer.parseInt(pageList.get(i))) {
						Page guess = page.getArea(coordList.get(i));

						// String test = coordList.get(i).toString();

						// add table header?
						tableHeaders.add("Table #" + tableNumber + " Coord: " + coordList.get(i));

						// add new table
						tables.addAll(basicExtractor.extract(guess));

						tableNumber++;
					}
				}
			}
		}
		return textFound;
	}
}
