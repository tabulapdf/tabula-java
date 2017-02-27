package technology.tabula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Rectangle;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.detectors.RegexSearch;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.Writer;

// NOTES:
//		need to remove tables from auto/spread list if they are used as a best guess
//
public class BatchSelectionTest {
	
	private BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
	
	// test finding all tables between upper and lower bound strings in input directory
	// outputs csv files for individual pdfs to output director
	// could instead use matrix of strings (2xN, 4xN, etc)
    public BatchSelectionTest(String inputPath, String outputPath, List<RegexContainer> regexList) {
    	try {
    		// inputPath could point to pdf or directory of pdfs
        	File inputFile = new File(inputPath);
        	
        	File outputFile = new File(outputPath);        	
        	
        	if (!inputFile.exists()) {
	            throw new ParseException("Input file or directory does not exist");
	        }
        	
        	if (!outputFile.exists()) {
	            throw new ParseException("Output directory does not exist");
	        }
        	
        	if(!outputFile.isDirectory()) {
        		throw new ParseException("Output path does not point to a directory");
        	}
        	
        	File parentPath;
        	
        	// get folder containing template
        	if(!inputFile.isDirectory()) {
        		parentPath = new File(inputFile.getAbsoluteFile().getParent());
        	}
        	else {
        		parentPath = inputFile;
        	}
        	
        	if(parentPath.isDirectory()) System.out.println("Valid folder"); // check that path is valid
        	else throw new Exception("Invalid file path");
        	
        	File[] fileList = parentPath.listFiles();
        	
        	// iterate over all files in directory
        	// does not go into subfolders yet
        	for(File currentFile : fileList)
        	{
        		// get file name without path      		
        		String fileName = currentFile.getName();
        		
        		// get file extension
        		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        		
        		// could check if file is directory for subfolder scanning
        		if(!extension.equals("pdf")) {
        			System.out.println(fileName + " is not a valid PDF file and will not be processed.");
        		}        	
        		else {
        			System.out.println(fileName + " is a valid PDF file and an attempt to process it will be made.");
        			
			        PDDocument pdfDocument = PDDocument.load(currentFile);
			        List<Table> tables = new ArrayList<Table>();
			        //List<Integer> pages;
			        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);	        
				    PageIterator pageIterator = extractor.extract(); /*(pages == null) ?
				      extractor.extract() :
				      extractor.extract(pages);*/				    
		        	
				    // iterator over each page and search for table with identifiers
				    // extract and append data to table if found
				    while (pageIterator.hasNext()) {
				    	Page page = pageIterator.next();
				    	
				    	for(int i = 0; i < regexList.size(); i++)
				    	{
				    		RegexContainer container = regexList.get(i);
				    		
					    	// get list of rectangles which match string inputs
					    	RegexSearch regexSearch = new RegexSearch(); 
					    	
					    	//List<Rectangle> guesses = regexSearch.detect(page, upperBound, lowerBound);

					    	List<Rectangle> guesses = null;
					    	
					    	if(container.getType() == 2){ // two string
					    		guesses = regexSearch.detect(
						    			page,
						    			container.getTop(),
						    			container.getBot()
						    			);
					    	}
					    		
					    	else if(container.getType() == 4){ // four string
					    		
					    		guesses = regexSearch.detect(
						    			page,
						    			container.getUpperLeft(),
						    			container.getUpperRight(),
						    			container.getLowerLeft(),
						    			container.getLowerRight()
						    			);
					    	}
					    	
					    	else continue; // procede to next regex container					    	
					    	
					    	if(guesses.isEmpty())
					    	{
					    		continue; // go to next page, do not attempt to extract data
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
					    	System.out.println(spread.toString());
					    	
					    	// compare rectangle lists, create worklist by removing elements from both lists and using auto detect
					    	List<Rectangle> worklist = new ArrayList<Rectangle>();
					    	
					    	float bestOverlap = 0;
					    	Rectangle bestGuess = null;
					    	
					    	// check if list is null or just take exception?
					    	// try over lap of 1(2) and 2(1)? XX <-- same ratio
					    	for(int j = 0; j < guesses.size(); j++)
					    	{
					    		Rectangle eval = guesses.get(j);
					    		bestGuess = eval;
					    		
					    		if(!auto.isEmpty()) {
						    		for(int k = 0; k < auto.size(); k++) {
						    			Rectangle eval1 = auto.get(k);
						    			
						    			// look for rectangle overlap or similarity?
						    			float overlap = eval.overlapRatio(eval1);
						    			float vertOverlap = eval.verticalOverlapRatio(eval1);
						    			
						    			System.out.println("Auto - Page #" + page.getPageNumber() + " overlap = " + overlap);
						    			System.out.println("Auto - Page #" + page.getPageNumber() + " vertOverlap = " + vertOverlap);
						    			
						    			if(overlap > bestOverlap)
						    			{
						    				bestOverlap = overlap;
						    				bestGuess = eval1;
						    			}
						    		}
					    		}
					    		
					    		// try spreadsheet?
					    		if(!spread.isEmpty()) {
						    		for(int k = 0; k < spread.size(); k++) {
						    			Rectangle eval2 = spread.get(k);
						    			
						    			// look for rectangle overlap or similarity?
						    			float overlap = eval.overlapRatio(eval2);
						    			float vertOverlap = eval.verticalOverlapRatio(eval2);
						    			
						    			System.out.println("Spread - Page #" + page.getPageNumber() + " overlap1 = " + overlap);
						    			System.out.println("Spread - Page #" + page.getPageNumber() + " vertOverlap = " + vertOverlap);
						    			
						    			if(overlap > bestOverlap)
						    			{
						    				bestOverlap = overlap;
						    				bestGuess = eval2;
						    			}
						    		}
					    		}
					    		
					    		if(!bestGuess.isEmpty())
					    		{
					    			worklist.add(bestGuess);
					    		}
					    	}
					    	
					    	// *** NOTE: two identifiers on the same line give exception
					    	/*
					    	for (Rectangle guessRect : guesses) {
					    		Page guess = page.getArea(guessRect);
					    		// may want to add a break after each table is appended
					    		tables.addAll(basicExtractor.extract(guess));
					    	}
					    	*/
					    	for (Rectangle guessRect : worklist) {
					    		Page guess = page.getArea(guessRect);
					    		// may want to add a break after each table is appended
					    		tables.addAll(basicExtractor.extract(guess));
					    	}
				    	}
		        	}
				    
				    // write data to new file 
				    // currently overwrites existing files and creates a file even if no tables extracted
			    	// should check if file already exists
				    // may want to add table header data
			    	Writer writer = null;
			    	BufferedWriter bufferedWriter = null;
			    	File testFile = new File(outputPath + "\\" + fileName.substring(0, fileName.lastIndexOf(".")) + ".csv");
			    	testFile.createNewFile();
			    	FileWriter fileWriter = new FileWriter(testFile);
		            bufferedWriter = new BufferedWriter(fileWriter);
		            // assuming CSV format for demo
			    	writer = new CSVWriter();
			    	writer.write(bufferedWriter, tables);
			    	// need to write empty line between tables
			    	
			    	// shut the door on your way out
			    	bufferedWriter.close();				    
				    pdfDocument.close();
        		}
        	}
		    
    	}
	    catch(Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
    }
    
    public static void main(String[] args) {
    	try {
    		// try input path, output path, regex list
	    	if(args.length != 4){
				throw new Exception("Command line parameters must be:\n"
									+ "inputPath outputPath upperBound lowerBound");
			}
			
			String inputPath = args[0];
			String outputPath = args[1];
			//String upperBound = args[2]; // unused
			//String lowerBound = args[3]; // unused
			
			List<RegexContainer> regexList = new ArrayList<RegexContainer>();
			regexList.add(new RegexContainer("Beverage", null, "Table", null));
			regexList.add(new RegexContainer("Analyte", "Qualifier", "Report Date", "Reported By"));			
	    	
			new BatchSelectionTest(inputPath, outputPath, regexList);
        }
        catch(Exception e) {
        	System.out.println(e);
        	System.exit(0);
        }
        
        System.exit(0);
    }

// reference for current page iterator
    
	/*private PageIterator getPageIterator(PDDocument pdfDocument) throws IOException {
        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);
        PageIterator pageIterator = (pages == null) ?
          extractor.extract() :
          extractor.extract(pages);
        return pageIterator;
    }*/


// may want to use this for different output extensions

/*
private String getOutputFilename(File pdfFile) {
    String extension = ".csv";
    switch (outputFormat) {
    case CSV:
        extension = ".csv";
        break;
    case JSON:
        extension = ".json";
        break;
    case TSV:
        extension = ".tsv";
        break;
    }
    return pdfFile.getPath().replaceFirst("(\\.pdf|)$", extension);
}
*/
    
}