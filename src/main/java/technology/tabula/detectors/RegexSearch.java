package technology.tabula.detectors;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;

import technology.tabula.Rectangle;
import technology.tabula.TextElement;

/*
 * RegexSearch
 * 
 *    TODO: Small blurb about this class
 *    
 *    TODO: Large blurb about this class
 *    10/29/2017 REM; created.
 */


public class RegexSearch {

	
	Pattern _regexBeforeTable;
	Pattern _regexAfterTable;
	
	ArrayList<MatchingArea> _matchingAreas;
	
	Boolean _includeRegexBeforeTable;
	Boolean _includeRegexAfterTable;
	
	/*
	 * @param regexBeforeTable The text pattern that occurs in the document directly before the table that is to be extracted
	 * @param regexAfterTable The text pattern that occurs in the document directly after the table that is to be extracted
	 * @param PDDocument The PDFBox model of the PDF document uploaded by the user.
	 */
	public RegexSearch(String regexBeforeTable, String includeRegexBeforeTable, String regexAfterTable, 
			           String includeRegexAfterTable, PDDocument document) {
		
		_regexBeforeTable = Pattern.compile(regexBeforeTable);
		_regexAfterTable = Pattern.compile(regexAfterTable);
		
	   _includeRegexBeforeTable = Boolean.valueOf(includeRegexBeforeTable);
	   _includeRegexAfterTable = Boolean.valueOf(includeRegexAfterTable);
		
		_matchingAreas = detectMatchingAreas(document);
		
	}
	
    /*
     * This class maps on a per-page basis the areas (plural) of the PDF document that fall between text matching the
     * user-provided regex (this allows for tables that span multiple pages to be considered a single entity).
     */
	private class MatchingArea extends HashMap<Integer,LinkedList<Rectangle>> {}

	/*
	 * @param pageNumber The one-based index into the document
	 * @return ArrayList<Rectangle> The values stored in _matchingAreas for a given page	
	 */
	public ArrayList<Rectangle> getMatchingAreasForPage(Integer pageNumber){
		
        ArrayList<Rectangle> allMatchingAreas = new ArrayList<Rectangle>();
		
		for( MatchingArea matchingArea : _matchingAreas) {
			allMatchingAreas.addAll(matchingArea.get(pageNumber));
		}
		
		 return allMatchingAreas;	
	}
	
	
	  /*
     * Inner class to retain information about a potential matching area while
     * iterating over the document and performing calculations to determine the rectangular 
     * area coordinates for matching areas. This may be overkill...
     */
	private final class DetectionData{
		
		DetectionData(){
			_pageBeginMatch = _pageEndMatch = null;
			_pageBeginCoord = _pageEndCoord = null;
		}
		
		Integer       _pageBeginMatch;
		Integer       _pageEndMatch;
		Point2D.Float _pageBeginCoord;
		Point2D.Float _pageEndCoord;
	}	
	
	/*
	 * detectMatchingAreas: Detects the subsections of the document occurring 
	 *                      between the user-specified regexes. 
	 * 
	 * @param document The name of the document for which regex has been applied
	 * @return ArrayList<MatchingArea> A list of the sections of the document that occur between text 
	 * that matches the user-provided regex
	 */
	
	private ArrayList<MatchingArea> detectMatchingAreas(PDDocument document) {
	  
	ObjectExtractor oe = new ObjectExtractor(document);
	Integer totalPages = document.getNumberOfPages();
	
	LinkedList<DetectionData> potentialMatches = new LinkedList<DetectionData>();
	potentialMatches.add(new DetectionData());

	for(Integer currentPage=1;currentPage<=totalPages;currentPage++) {
		
		/*
		 * Convert PDF page to text
		 */
		Page page = oe.extract(currentPage);
		ArrayList<TextElement> pageTextElements = (ArrayList<TextElement>) page.getText();
		String pageAsText ="";
		
		for(TextElement element : pageTextElements ) {
			pageAsText += element.getText();
		}

		/*
		 * Find each table on each page + tables which span multiple pages
		 */
		
		Integer startMatchingAt = 0;
		Matcher beforeTableMatches = _regexBeforeTable.matcher(pageAsText);
		Matcher afterTableMatches  = _regexAfterTable.matcher(pageAsText);
		
		while( beforeTableMatches.find(startMatchingAt) || afterTableMatches.find(startMatchingAt)) {
			
		   if(potentialMatches.getLast()._pageBeginMatch==null && beforeTableMatches.find(startMatchingAt)) {

		       Float xCoordinate = pageTextElements.get(beforeTableMatches.start()).x;
		       Float yCoordinate = pageTextElements.get(beforeTableMatches.start()).y;
		       yCoordinate += (_includeRegexBeforeTable) ? 0 : pageTextElements.get(beforeTableMatches.start()).height;
			   Point2D.Float coordinates = new Point2D.Float(xCoordinate,yCoordinate);
				   	                                    
			   
			   potentialMatches.getLast()._pageBeginCoord=coordinates;
			   potentialMatches.getLast()._pageBeginMatch=currentPage;
			
			   startMatchingAt = beforeTableMatches.end();
		   }
		   else if(potentialMatches.getLast()._pageEndMatch==null && afterTableMatches.find(startMatchingAt)) {
			

		       Float xCoordinate = pageTextElements.get(afterTableMatches.start()).x;
		       Float yCoordinate = pageTextElements.get(afterTableMatches.start()).y;
		       yCoordinate += (_includeRegexAfterTable) ? pageTextElements.get(afterTableMatches.start()).height : 0;

			   Point2D.Float coordinates = new Point2D.Float(xCoordinate,yCoordinate);
			
			   potentialMatches.getLast()._pageEndCoord = coordinates;
			   potentialMatches.getLast()._pageEndMatch = currentPage;
	
			   startMatchingAt = afterTableMatches.end();
			 
			   potentialMatches.add(new DetectionData()); //To reset algorithm for detection of another table
			  
		   }		
		}
	}	
	
	/*
	 * Remove the last potential match if its data is incomplete
	 */
	DetectionData lastPotMatch = potentialMatches.getLast();
	
	if(lastPotMatch._pageBeginMatch==null || lastPotMatch._pageEndMatch==null) {
		potentialMatches.removeLast();
	}
	
	return calculateMatchingAreas(potentialMatches,document);
	
	}
	

	
	/*
	 * calculateMatchingAreas: Determines the rectangular coordinates of the document sections
	 *                         matching the user-specified regex(_regexBeforeTable,_regexAfterTable)
	 * 
	 * @param foundMatches A list of DetectionData values
	 * @return ArrayList<MatchingArea> A Hashmap 
	 */
	private ArrayList<MatchingArea> calculateMatchingAreas(LinkedList<DetectionData> foundMatches, PDDocument document) {
		
		ArrayList<MatchingArea> matchingAreas = new ArrayList<MatchingArea>();
		
		ObjectExtractor oe = new ObjectExtractor(document);
	
		
		while(foundMatches.isEmpty() == false) {
			DetectionData foundTable = foundMatches.pop();
			
            if(foundTable._pageBeginMatch == foundTable._pageEndMatch) {
            
            	float width = oe.extract(foundTable._pageBeginMatch).width;
            	float height = foundTable._pageEndCoord.y-foundTable._pageBeginCoord.y;
            	
            	LinkedList<Rectangle> tableArea = new LinkedList<Rectangle>();
            	tableArea.add( new Rectangle(foundTable._pageBeginCoord.y,0,width,height)); //TODO:Figure out how/what must be done to support multi-column texts (4 corners??)
            	
            	MatchingArea matchingArea = new MatchingArea();
            	matchingArea.put(foundTable._pageBeginMatch, tableArea);
            
            	matchingAreas.add(matchingArea);
            
			}
            else {
            	
            	MatchingArea matchingArea = new MatchingArea();
            	
            	/*
            	 * Create sub-area for table from directly below the pattern-before-table content to the end of the page
            	 */
            	Page currentPage =  oe.extract(foundTable._pageBeginMatch);
            	LinkedList<Rectangle> tableSubArea = new LinkedList<Rectangle>();
            	tableSubArea.add( new Rectangle(foundTable._pageBeginCoord.y,0,currentPage.width,
            			                        currentPage.height-foundTable._pageBeginCoord.y)); //TODO:Figure out how/what must be done to support multi-column texts (4 corners??)
            	
            	matchingArea.put(foundTable._pageBeginMatch, tableSubArea);
            	
            	/*
            	 * Create sub-areas for table that span the entire page
            	 */
            	for (Integer iter=currentPage.getPageNumber()+1; iter<foundTable._pageEndMatch; iter++) {
            		currentPage = oe.extract(iter);
            		
            		tableSubArea = new LinkedList<Rectangle>();
            		tableSubArea.add(new Rectangle(0,0,currentPage.width,currentPage.height));
            		
            		matchingArea.put(currentPage.getPageNumber(), tableSubArea);
            		
            	}
                
            	/*
            	 * Create sub-areas for table from the top of the page to directly before the pattern-after-table content 
            	 */
            	
            	currentPage = oe.extract(foundTable._pageEndMatch);
                tableSubArea = new LinkedList<Rectangle>();
                tableSubArea.add(new Rectangle(0,0,currentPage.width,foundTable._pageEndCoord.y));
                   

                matchingArea.put(currentPage.getPageNumber(), tableSubArea);
                matchingAreas.add(matchingArea);
            	
            }
		}
		return matchingAreas;
	}
}
