package technology.tabula.extractors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Rectangle;
import technology.tabula.Table;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.detectors.RegexSearch;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.Writer;

// NOTES:
//		need to remove tables from auto/spread list if they are used as a best guess
//		or remove very similar tables from the list before extracting data
public class BatchSelectionExtractor {

	public int extract(String inputPath, String outputPath, String jsonPath, String processType, boolean ocrAllowed,
			int overlapThreshold) {
		System.out.println("OCR Selection: " + ocrAllowed);
		try {
			// ----------------------------------------------------------
			// Confirm process type is valid
			// ----------------------------------------------------------
			
			if (!processType.equals("both") && !processType.equals("coords") && !processType.equals("regex")) {
				// invalid process type
				return 0;
			}

			// ----------------------------------------------------------
			// Confirm input and output paths exist
			// ----------------------------------------------------------
			
			File inputFile = new File(inputPath); // inputPath could point to
													// pdf or directory of pdfs
													// currently does not check
													// subfolders

			File outputFile = new File(outputPath);

			if (!inputFile.exists()) {
				throw new ParseException("Input file or directory does not exist");
			}

			if (!outputFile.exists()) {
				throw new ParseException("Output directory does not exist");
			}

			if (!outputFile.isDirectory()) {
				throw new ParseException("Output path does not point to a directory");
			}

			File parentPath;

			// get folder containing template
			if (!inputFile.isDirectory()) {
				parentPath = new File(inputFile.getAbsoluteFile().getParent());
			} else {
				parentPath = inputFile;
			}

			if (parentPath.isDirectory()) {
				System.out.println("Valid folder"); // check that path is valid
			} else {
				throw new Exception("Invalid file path");
			}

			// ----------------------------------------------------------
			// Confirm json file is valid
			// ----------------------------------------------------------
			
			// STILL NEED TO CHECK THAT PARSED FILE IS VALID
			FileReader fr = new FileReader(jsonPath);
			BufferedReader br = new BufferedReader(fr);

			List<String[]> regexList = new ArrayList<String[]>();
			List<String> pageList = new ArrayList<String>();
			List<Rectangle> coordList = new ArrayList<Rectangle>();

			String currentString;

			if (processType.equals("regex")) {

				while ((currentString = br.readLine()) != null) {
					// split the input stream
					String[] splitString = currentString.split(",");

					if (splitString.length > 4) {
						System.out.println("Too many arguments for Regex Search: " + splitString);
						continue; // untested
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
					regexList.add(addString); // split("[\\s,]+")));

					// if string is invalid, don't continue searching?
				}

				// if the regexList is empty, don't continue searching?
			}

			else if (processType.equals("coords")) {

				while ((currentString = br.readLine()) != null) {
					// System.out.println(currentString);

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

			// ----------------------------------------------------------
			// Scan through input folder for PDF documents
			// ----------------------------------------------------------
			
			// Create list of files in input directory
			File[] fileList = parentPath.listFiles();
			List<String> deleteList = new ArrayList<String>();

			// iterate over all files in directory
			// does not go into subfolders yet
			for (File currentFile : fileList) {
				// get file name without path
				String fileName = currentFile.getName();

				// get file extension
				String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

				// could check if file is directory for subfolder scanning
				if (!extension.equals("pdf")) {
					System.out.println(fileName + " is not a valid PDF file and will not be processed.");
				} else {
					System.out.println(fileName + " is a valid PDF file and an attempt to process it will be made.");

					// fix brackets
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
								processType, regexList, coordList, pageList, tables, tableHeaders);

						pdfDocument.close();
						reading = false;

						// try to OCR document if allowed and no text found in
						// original document
						if (!textFound) {// && ocr){
							System.out.println("Possible image based document: " + currentFile.getAbsolutePath());

							// attempt to ocr document and processs again
							if (ocrAllowed) {
								System.out.println("Attempting to convert document to text based format...");

								try {
									OcrConverter ocr = new OcrConverter();

									ocr.extract(currentFile.getAbsolutePath(), true);

									File ocrFile = new File(currentFile.getAbsolutePath().substring(0,
											currentFile.getAbsolutePath().length() - 4) + "_OCR.pdf");

									deleteList.add(ocrFile.getAbsolutePath());

									pdfDocument = PDDocument.load(ocrFile);

									reading = true;

									extractor = new ObjectExtractor(pdfDocument);

									pageIterator = extractor.extract();

									textFound = false;

									textFound = scanDocForMatchedTables(basicExtractor, pageIterator, overlapThreshold, processType,
											regexList, coordList, pageList, tables, tableHeaders);

									pdfDocument.close();

									reading = false;

									// add document to list of created files instead? clean up later?
									// can this trigger an exception too?
									// need another try-catch?
								} catch (Exception e) {
									// e.printStackTrace();
									System.out.println("Unable to properly convert or extract data from document");
								}

							} else
								System.out.println("Ignored document based on input parameters");
						}

						// just in case
						if (reading) {
							pdfDocument.close();
							reading = false;
						}
						// write data to new file
						// currently overwrites existing files and creates a
						// 	file even if no tables extracted
						// may be better to check if file already exists
						Writer writer = null;
						BufferedWriter bufferedWriter = null;
						File testFile = new File(
								outputPath + "\\" + fileName.substring(0, fileName.lastIndexOf(".")) + ".csv");
						testFile.createNewFile();
						FileWriter fileWriter = new FileWriter(testFile);
						bufferedWriter = new BufferedWriter(fileWriter);
						// assuming CSV format for demo
						writer = new CSVWriter();

						int tableNum = 0; // needed?

						// add header for OCR'd files?
						for (Table table : tables) {
							List<Table> fauxTableList = new ArrayList<Table>();
							fauxTableList.add(table);

							// write table header
							bufferedWriter.write(tableHeaders.get(tableNum) + "\n");

							// write single table
							writer.write(bufferedWriter, fauxTableList);

							// write line break
							bufferedWriter.write("\n");

							// increment table number?
							tableNum++;
						}

						// shut the door on your way out
						bufferedWriter.close();
						// pdfDocument.close();
					}

					catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				} // end of current pdf processing 
			} // end of file list

			if (!deleteList.isEmpty())
				System.out.println("Deleting OCR'd files...");
			// clean up created OCR files
			for (String ocrPath : deleteList) {
				System.out.println("Attempting to delete " + ocrPath);
				try {
					File ocrFile = new File(ocrPath);
					ocrFile.delete();
					System.out.println("File deleted");
				} catch (Exception e) {
					System.out.println("Unable to delete file!!!");
				}

			}

			System.out.println("\nEnd of processing");
			return 1;

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// should be rewritten so that fewer paramters are required
	// more method calls? globals?
	private boolean scanDocForMatchedTables(BasicExtractionAlgorithm basicExtractor, PageIterator pageIterator, int overlapThreshold, String processType,
			List<String[]> regexList, List<Rectangle> coordList, List<String> pageList, List<Table> tables,
			List<String> tableHeaders) {
		boolean textFound = false;

		int tableNumber = 1;

		while (pageIterator.hasNext()) {
			Page page = pageIterator.next();

			if (page.hasText()) {
				textFound = true;
			} else
				continue;

			if (processType.equals("regex")) {
				for (int i = 0; i < regexList.size(); i++) {
					try {
						String[] identifiers = regexList.get(i);

						// get list of rectangles which match string inputs
						RegexSearch regexSearch = new RegexSearch();

						List<Rectangle> guesses = null;

						// one string version?
						guesses = regexSearch.detect(page, identifiers);

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
						// 	multiple times based on overlap
						for (Rectangle guessRect : worklist) {
							Page guess = page.getArea(guessRect);
							// may want to add a break after each
							// table is appended

							// add table header?
							tableHeaders.add("Table #" + tableNumber + "  ul: " + identifiers[0] + "  ur: "
									+ identifiers[1] + "  ll: " + identifiers[2] + "  lr: " + identifiers[3]);

							// add new table
							tables.addAll(basicExtractor.extract(guess));

							tableNumber++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// no need for exception handling?
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

	public static void main(String[] args) {
		try {
			if (args.length != 6) {
				throw new Exception("Command line parameters must be:\n" + "inputPath outputPath jsonPath processType ocrAllowed overlap");
			}

			String inputPath = args[0];
			String outputPath = args[1];
			String jsonPath = args[2];
			String processType = args[3];
			boolean ocrAllowed = Boolean.valueOf(args[4]);
			int overlap = Integer.valueOf(args[5]);

			BatchSelectionExtractor test = new BatchSelectionExtractor();

			int result = test.extract(inputPath, outputPath, jsonPath, processType, ocrAllowed, overlap);
			System.out.println(result);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		System.exit(0);
	}
}
