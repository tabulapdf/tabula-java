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
//
//		REGEX CONTAINER NO LONGER OF ANY REAL USE
public class BatchSelectionExtractor {

	// may not want to use globals, but works better for splitting up code into method calls
	//private BasicExtractionAlgorithm basicExtractor;// = new BasicExtractionAlgorithm(); // needed as global?
	//private List<RegexContainer> regexList;// = new ArrayList<RegexContainer>();
	//private List<String> pageList;// = new ArrayList<String>();
	//private List<Rectangle> coordList;// = new ArrayList<Rectangle>();
	//private List<Table> tables;

	// test finding all tables between upper and lower bound strings in input
	// directory
	// outputs csv files for individual pdfs to output director
	// could instead use matrix of strings (2xN, 4xN, etc)

	BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm(); // useful as global?
	
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
			File inputFile = new File(inputPath); // inputPath could point to pdf or directory of pdfs
												  // currently does not check subfolders

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

			// NEED TO CHECK THAT PARSED FILE IS VALID
			FileReader fr = new FileReader(jsonPath);
			BufferedReader br = new BufferedReader(fr);
			
			
			List<String[]> regexList = new ArrayList<String[]>();
			List<String> pageList = new ArrayList<String>();
			List<Rectangle> coordList = new ArrayList<Rectangle>();
						
			String currentString;

			if (processType.equals("regex")) {

				//regexList = new ArrayList<RegexContainer>();
				while ((currentString = br.readLine()) != null) {
					// System.out.println(currentString);

					regexList.add(currentString.split(",")); //split("[\\s,]+")));
				}
			}

			else if (processType.equals("coords")) {

				//pageList = new ArrayList<String>();
				//coordList = new ArrayList<Rectangle>();
				
				while ((currentString = br.readLine()) != null) {
					// System.out.println(currentString);

					String array[] = currentString.split("[\\s,]+");

					pageList.add(array[0]);
					float leftBound = Float.valueOf(array[1]);
					float topBound = Float.valueOf(array[2]);
					float width = Float.valueOf(array[3]);
					float height = Float.valueOf(array[4]);

					coordList.add(new Rectangle(leftBound, topBound, width, height));
				}
			}

			br.close();
			fr.close();

			// Create list of files in input directory
			File[] fileList = parentPath.listFiles();

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
						
						// check if PDF document is text or image based
						
						// if image based and OCR is allowed, try to convert
						
						// reload pdfDocument as new ocr'd version

						List<Table> tables = new ArrayList<Table>();
						// List<Integer> pages;
						
						ObjectExtractor extractor = new ObjectExtractor(pdfDocument);						
						basicExtractor = new BasicExtractionAlgorithm();
						PageIterator pageIterator = extractor.extract(); 
											 /*
											 * (pages == null) ?
											 * extractor.extract() :
											 * extractor.extract(pages);
											 */

						// iterator over each page and search for table with identifiers
						// extract and append data to table if found
						//
						// scan assuming that document is text based	
						
						boolean textFound = scanDocForMatchedTables(pageIterator, overlapThreshold, processType, regexList, coordList, pageList, tables);
						
						pdfDocument.close();
						
						// try to OCR document if allowed and no text found in original document
						if(!textFound){// && ocr){							
							System.out.println("Possible image based document: " + currentFile.getAbsolutePath());
							
							if(ocrAllowed){ // attempt to ocr document and processs again								
								
								OcrConverter ocr = new OcrConverter();
								
								ocr.extract(currentFile.getAbsolutePath());
								
								File ocrFile = new File(currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().length() - 4) + "_OCR.pdf");
								
								pdfDocument = PDDocument.load(ocrFile);
								
								extractor = new ObjectExtractor(pdfDocument);
								
								pageIterator = extractor.extract();
								
								textFound = false;
								
								textFound = scanDocForMatchedTables(pageIterator, overlapThreshold, processType, regexList, coordList, pageList, tables);
							}
						}
						else System.out.println("Possible text based document: " + currentFile.getAbsolutePath());
						
						pdfDocument.close();

						
						// dont write file if tables is empty or no text found
						
						// write data to new file
						// currently overwrites existing files and creates a
						// file even if no tables extracted
						// should check if file already exists
						// may want to add table header data
						Writer writer = null;
						BufferedWriter bufferedWriter = null;
						File testFile = new File(
								outputPath + "\\" + fileName.substring(0, fileName.lastIndexOf(".")) + ".csv");
						testFile.createNewFile();
						FileWriter fileWriter = new FileWriter(testFile);
						bufferedWriter = new BufferedWriter(fileWriter);
						// assuming CSV format for demo
						writer = new CSVWriter();
						writer.write(bufferedWriter, tables);
						// need to write empty line between tables

						// shut the door on your way out
						bufferedWriter.close();
						//pdfDocument.close();						
					}

					catch (Exception e) {
						System.out.println(e);
						continue;
					}
				}
			}
			System.out.println("\nEnd of processing");
			return 1;

		} catch (Exception e) {
			System.out.println(e);
			return 0;
		}
	}
	
	private boolean scanDocForMatchedTables(PageIterator pageIterator, int overlapThreshold, String processType, List<String[]> regexList, List<Rectangle> coordList, List<String> pageList, List<Table> tables){
		boolean textFound = false;
		while (pageIterator.hasNext()) {
			Page page = pageIterator.next();

			if(page.hasText()){
				textFound = true;
			}
			else continue;
			
			if (processType.equals("regex")) {
				for (int i = 0; i < regexList.size(); i++) {
					try{
						String[] identifiers = regexList.get(i);
						//String[] identifiers = container.getIdentifiers();
	
						// get list of rectangles which match string
						// inputs
						RegexSearch regexSearch = new RegexSearch();
	
						// List<Rectangle> guesses =
						// regexSearch.detect(page, upperBound,
						// lowerBound);
	
						List<Rectangle> guesses = null;
	
						// one string version?
						guesses = regexSearch.detect(page, identifiers);
						
						/*
						if (container.getType() == 2) { // two
														// string
							guesses = regexSearch.detect(page, identifiers[0], null, identifiers[1], null); // guesses = regexSearch.detect(page, identifiers[0], identifiers[1]);
						}
	
						else if (container.getType() == 4) { // four
																// string
	
							guesses = regexSearch.detect(page, identifiers[0], identifiers[1],
									identifiers[2], identifiers[3]);
						}
						*/
	
						/*
						else
							continue; // procede to next regex
										// container
						*/
						
						if (guesses.isEmpty()) {
							continue; // go to next page, do not
										// attempt to extract data
						}
	
						// get auto detected list of rectangles
						NurminenDetectionAlgorithm nurmDetect = new NurminenDetectionAlgorithm();
						List<Rectangle> auto = nurmDetect.detect(page);
	
						// try spreadsheet if auto doesnt work?
						SpreadsheetDetectionAlgorithm spreadDetect = new SpreadsheetDetectionAlgorithm();
						List<Rectangle> spread = spreadDetect.detect(page);
	
						// print test
						System.out.println(guesses.toString());
						System.out.println(auto.toString());
						// System.out.println(spread.toString());
	
						// compare rectangle lists, create worklist
						// by removing elements from both lists and
						// using auto detect
						List<Rectangle> worklist = new ArrayList<Rectangle>();
	
						//float bestOverlap = 0;
						float bestOverlap = (float)overlapThreshold/100;
						Rectangle bestGuess = null;
	
						// check if list is null or just take
						// exception?
						// try over lap of 1(2) and 2(1)? XX <--
						// same ratio
						for (int j = 0; j < guesses.size(); j++) {
							Rectangle eval = guesses.get(j);
							bestGuess = eval;
	
							if (!auto.isEmpty()) {
								for (int k = 0; k < auto.size(); k++) {
									Rectangle eval1 = auto.get(k);
	
									// look for rectangle overlap or
									// similarity?
									float overlap = eval.overlapRatio(eval1);
									float vertOverlap = eval.verticalOverlapRatio(eval1);
	
									System.out.println("Auto - Page #" + page.getPageNumber()
											+ " overlap = " + overlap);
									System.out.println("Auto - Page #" + page.getPageNumber()
											+ " vertOverlap = " + vertOverlap);
	
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
	
									// look for rectangle overlap or
									// similarity?
									float overlap = eval.overlapRatio(eval2);
									float vertOverlap = eval.verticalOverlapRatio(eval2);
	
									System.out.println("Spread - Page #" + page.getPageNumber()
											+ " overlap1 = " + overlap);
									System.out.println("Spread - Page #" + page.getPageNumber()
											+ " vertOverlap = " + vertOverlap);
	
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
	
						// *** NOTE: two identifiers on the same
						// line give exception
						/*
						 * for (Rectangle guessRect : guesses) {
						 * Page guess = page.getArea(guessRect); //
						 * may want to add a break after each table
						 * is appended
						 * tables.addAll(basicExtractor.extract(
						 * guess)); }
						 */
						// NOTE:
						// dont add the same table twice
						for (Rectangle guessRect : worklist) {
							Page guess = page.getArea(guessRect);
							// may want to add a break after each
							// table is appended
							tables.addAll(basicExtractor.extract(guess));
						}
					}
					catch(Exception e){
						System.out.println(e);
					}
				}
			} else if (processType.equals("coords")) {
				for (int i = 0; i < coordList.size(); i++) {
					if (page.getPageNumber() == Integer.parseInt(pageList.get(i))) {
						Page guess = page.getArea(coordList.get(i));
						tables.addAll(basicExtractor.extract(guess));
					}
				}
			}
		}
		return textFound;
	}

	public static void main(String[] args) {
		try {
			// try input path, output path, regex list
			if (args.length != 4) {
				throw new Exception("Command line parameters must be:\n" + "inputPath outputPath jsonPath processType");
			}

			String inputPath = args[0];
			String outputPath = args[1];
			String jsonPath = args[2]; // unused
			String processType = args[3]; // unused

			/*
			 * List<RegexContainer> regexList = new ArrayList<RegexContainer>();
			 * //regexList.add(new RegexContainer("6.40", "5.00"));
			 * 
			 * //regexList.add(new RegexContainer("Beverage", "Table"));
			 * //regexList.add(new RegexContainer("Analyte", "Qualifier",
			 * "Date", "By")); //regexList.add(new RegexContainer("A^", "B^",
			 * "C^", "D^"));
			 * 
			 * 
			 * // testing failed file // Report,Name,GASTONIA,DALLAS
			 * regexList.add(new RegexContainer("Report", "Name", "GASTONIA",
			 * "DALLAS")); regexList.add(new RegexContainer("Analyte",
			 * "Qualifier", "Date", "By")); //regexList.add(new
			 * RegexContainer("New Well", "Unit", "Date", "By"));
			 * //regexList.add(new RegexContainer("Report 1", "Report 2",
			 * "Report 3", "Report 4")); regexList.add(new
			 * RegexContainer("Report 1", "Report 3"));
			 */
			
			//RegexSearch search = new RegexSearch();

			BatchSelectionExtractor test = new BatchSelectionExtractor();
			
			int overlap = 0;

			int result = test.extract(inputPath, outputPath, jsonPath, processType, true, overlap);
			System.out.println(result);

		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}

		System.exit(0);
	}

	// reference for current page iterator

	/*
	 * private PageIterator getPageIterator(PDDocument pdfDocument) throws
	 * IOException { ObjectExtractor extractor = new
	 * ObjectExtractor(pdfDocument); PageIterator pageIterator = (pages == null)
	 * ? extractor.extract() : extractor.extract(pages); return pageIterator; }
	 */

	// may want to use this for different output extensions

	/*
	 * private String getOutputFilename(File pdfFile) { String extension =
	 * ".csv"; switch (outputFormat) { case CSV: extension = ".csv"; break; case
	 * JSON: extension = ".json"; break; case TSV: extension = ".tsv"; break; }
	 * return pdfFile.getPath().replaceFirst("(\\.pdf|)$", extension); }
	 */

}