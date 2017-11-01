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
 *    TODO: Small blurb about this
 *    
 *    TODO: Large blurb about this
 *    
 *    10/29/2017 REM; created.
 */


public class RegexSearch {

	
	Pattern _regexBeforeTable;
	Pattern _regexAfterTable;
	
	
	ArrayList<MatchingArea> _matchingAreas;
	
	/*
	 * @param regexBeforeTable The text pattern that occurs in the document directly before the table that is to be extracted
	 * @param regexAfterTable The text pattern that occurs in the document directly after the table that is to be extracted
	 * @param PDDocument The PDFBox model of the PDF document uploaded by the user.
	 */
	public RegexSearch(String regexBeforeTable, String regexAfterTable, PDDocument document) {
		
		_regexBeforeTable = Pattern.compile(regexBeforeTable);
		_regexAfterTable = Pattern.compile(regexAfterTable);
		
		
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
			
			   
			   Point2D.Float coords = new Point2D.Float(pageTextElements.get(beforeTableMatches.start()).x,
				   	                                    pageTextElements.get(beforeTableMatches.start()).y + 
				   	                                    pageTextElements.get(beforeTableMatches.start()).height);
			
			   potentialMatches.getLast()._pageBeginCoord=coords;
			   potentialMatches.getLast()._pageBeginMatch=currentPage;
			
			   startMatchingAt = beforeTableMatches.end();
		   }
		   else if(potentialMatches.getLast()._pageEndMatch==null && afterTableMatches.find(startMatchingAt)) {
			
			   
			   Point2D.Float coords = new Point2D.Float(pageTextElements.get(afterTableMatches.start()).x,
                                                        pageTextElements.get(afterTableMatches.start()).y);
			
			   potentialMatches.getLast()._pageEndCoord = coords;
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


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	public List<Rectangle> detect(Page page, String[] regexList) throws ParseException {
		// check if page object is null
		if(page == null) return new ArrayList<Rectangle>(); // return empty arraylist
		
		// only 3 and 4 string arrays accepted
		if(regexList.length < 3 || regexList.length > 4) return new ArrayList<Rectangle>(); // return empty arraylist;
		
		// this is so dumb
		String upperLeft = null;
		String upperRight = null;
		String lowerLeft = null;
		String lowerRight = null;
		
		// parse out the input string array
		for(int i = 0; i < regexList.length; i++) {
			switch(i) {
				case 0:
					upperLeft = regexList[i];
					break;
				case 1:
					upperRight = regexList[i];
					break;
				case 2:
					lowerLeft = regexList[i];
					break;
				case 3:
					lowerRight = regexList[i];
					break;
				default:
					break;
			}
		}		
		
		// replace null strings with empty fields
		if(upperLeft == null) upperLeft = "";
		if(upperRight == null) upperRight = "";
		if(lowerLeft == null) lowerLeft = "";
		if(lowerRight == null) lowerRight = "";
		
		// count number of empty fields
		int emptyCount = 0;
		if(upperLeft.equals("")) emptyCount++;
		if(upperRight.equals("")) emptyCount++;
		if(lowerLeft.equals("")) emptyCount++;
		if(lowerRight.equals("")) emptyCount++;
		
		// if 0 or 1 empty fields, perform 4 corner search
		if(emptyCount <= 1) {
			return detectFour(page, upperLeft, upperRight, lowerLeft, lowerRight);
		}
		
		// if 2 fields are null, determine which then perform 2 corner search
		if(emptyCount == 2 
				&& (upperLeft.equals("") ^ upperRight.equals("")) 
				&& (lowerLeft.equals("") ^ lowerRight.equals(""))) {
			String upperBound;
			String lowerBound;
			if((upperRight.equals("")) && !(upperLeft.equals(""))) upperBound = upperLeft;
			else upperBound = upperRight;
			if((lowerRight.equals("")) && !(lowerLeft.equals(""))) lowerBound = lowerLeft;
			else lowerBound = lowerRight;
			return detectTwo(page, upperBound, lowerBound);
		}
		
		return new ArrayList<Rectangle>(); // return empty arraylist
		
	}
	
	// test without expanding to page?
	private List<Rectangle> detectTwo(Page page, String upperBound, String lowerBound) throws ParseException {
		List<Rectangle> regexList = new ArrayList<Rectangle>();
		char currChar = 0;
		Boolean upperExists = false;
		int upperCount = 0;
		int lowerCount = 0;
		TextElement currElement = null;
		TextElement firstElement = null;
		TextElement lastElement = null;
		int state = 0;

		// check for empty upper and lower here instead of later
		// Dont really need to check these if they are private and only called by detect function				
		if(page == null){
			return new ArrayList<Rectangle>(); // empty list
		}
		if(upperBound == null || lowerBound == null){
			return new ArrayList<Rectangle>(); // empty list
		}

		// have page, can check text elements
		List<TextElement> textElements = page.getText();
		int size = textElements.size();

		// should scan through text elements so that we can use coords
		for (int i = 0; i < size; i++) {
			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);
			if (!upperBound.isEmpty() && upperExists == false) {
				switch (state) {
				case 0:
					if (currChar == upperBound.charAt(0)) { // valid first upper
						firstElement = currElement;
						if (upperBound.length() == 1) { // single character case
							state = 0;
							upperCount = 0;
							if (lowerBound.isEmpty()) {
								// single character upper, no lower
								regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
										page.height - firstElement.y));
							} else {
								// single character upper, yes lower
								upperExists = true;
							}
						} else { // multiple character strings continue scanning
							upperCount = 1;
							state = 1;
						}
					}
					break;
				case 1:
					if (currChar == upperBound.charAt(upperCount)) {
						upperCount++;
					} else { // invalid char detected
						upperCount = 0;
						state = 0;
					}
					if (upperCount == upperBound.length()) {
						state = 0;
						upperCount = 0;
						if (lowerBound.isEmpty()) {
							// Multiple character upper, no lower
							regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
									page.height - firstElement.y));
						} else {
							// Multiple character upper, yes lower
							upperExists = true;
						}
					}
					break;
				}
			} else {
				switch (state) {
				case 0:
					if (currChar == lowerBound.charAt(0)) { // valid first lower
						if (lowerBound.length() == 1) {
							lastElement = currElement;
							state = 0;
							if (upperExists) { // upper and lower single char
								regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
										(lastElement.y - firstElement.y) + lastElement.height));
							} else {
								regexList.add(new Rectangle(0, 0, page.width, lastElement.y + lastElement.height));
							}
							upperExists = false;
							continue;
						} else {// multiple character strings continue scanning
							lowerCount = 1;
							state = 1;
						}
					}
					break;
				case 1:
					// next valid char detected
					if (currChar == lowerBound.charAt(lowerCount)) {
						lowerCount++;
					} else { // invalid char detected
						lowerCount = 0;
						state = 0;
					}
					if (lowerCount == lowerBound.length()) {
						lastElement = currElement;
						state = 0;
						lowerCount = 0;
						if (upperExists) {
							regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
									(lastElement.y - firstElement.y) + lastElement.height));
						} else {
							regexList.add(new Rectangle(0, 0, page.width, lastElement.y + lastElement.height));
						}
						upperExists = false;
					}
					break;
				}
			}
		}
		/*for (Rectangle walk : regexList) {
			System.out.println(
					"Box Found at x=" + walk.x + " y=" + walk.y + " width=" + walk.width + " height=" + walk.height);
		}*/
		return regexList;
	}

	// possible issues:
	// multiple strings share the same name
	// right string to the left of left string?
	// need first char of left strings, last char of right strings <-- if left
	// and right reversed???
	// recover loop value if string detect fails
	// two strings on same line with same first char may fail to correctly reset
	// todo:
	// implement empty field handling
	//
	// check for both strings at once in first state, then set flag
	private List<Rectangle> detectFour(Page page, String upperLeft, String upperRight, String lowerLeft, String lowerRight)
			throws ParseException {

		// Dont really need to check these if they are private and only called by detect function				
		if(page == null){
			return new ArrayList<Rectangle>(); // empty list
		}
		// if both top strings are null, return null
		if (upperLeft == null && upperRight == null) {
			return new ArrayList<Rectangle>(); // empty list
		}
		
		if (lowerLeft == null && lowerRight == null) {
			return new ArrayList<Rectangle>(); // empty list
		}

		List<Rectangle> regexList = new ArrayList<Rectangle>();
		char currChar = 0;

		// could check elements for null instead
		Boolean upperLeftExists = false;
		Boolean upperRightExists = false;
		Boolean lowerLeftExists = false;
		Boolean lowerRightExists = false;

		TextElement upperLeftElement = null;
		TextElement upperRightElement = null;
		TextElement lowerLeftElement = null;
		TextElement lowerRightElement = null;

		int backup = 0;
		int lastGoodIndex = 0;
		int resetCount = 0;
		
		int foundCount = 0; // use to count number of detected strings

		int upperLeftCount = 0;
		int upperRightCount = 0;
		int lowerLeftCount = 0;
		int lowerRightCount = 0;

		TextElement currElement = null;
		TextElement firstElement = null;
		// TextElement lastElement = null;

		int searchArea = 0;
		int upperState = 0;
		int lowerState = 0;

		// have page, can check text elements
		List<TextElement> textElements = page.getText();
		int size = textElements.size();

		// should scan through text elements so that we can use coords
		for (int i = 0; i < size; i++) {

			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);

			if (upperLeftExists && upperRightExists) { // found both top, temp solution
				searchArea = 1;
			}
			
			try{
			switch(searchArea) {
			case 0:
			// search for top elements
			//if (!(upperLeftExists && upperRightExists)) {
				switch (upperState) {
				case 0: // search for first char of either top string
				{
					// valid first upper left
					if (upperLeft.startsWith(Character.toString(currChar)) && !(upperLeftExists))
					{
						if (upperLeft.length() == 1) // single character case
						{
							upperLeftElement = currElement;
							upperLeftExists = true;
							lastGoodIndex = i;
							foundCount++;
						}

						else {
							firstElement = currElement;
							backup = i;
							upperLeftCount = 1;
							upperState = 1;
						}
					}

					// valid first upper right
					else if (upperRight.startsWith(Character.toString(currChar)) && !(upperRightExists))
					{
						if (upperRight.length() == 1) // single character case
						{
							upperRightElement = currElement;
							upperRightExists = true;
							lastGoodIndex = i;
							foundCount++;
						} else {
							firstElement = currElement;
							backup = i;
							upperRightCount = 1;
							upperState = 2;
						}
					}

					break;
				}

				case 1: // evaluate chars for top left string
				{
					if (currChar == upperLeft.charAt(upperLeftCount)) {
						upperLeftCount++;
					} else { // invalid char detected
						upperLeftCount = 0;

						//if (upperLeft.charAt(0) == upperRight.charAt(0)) {
						if (upperRight.startsWith(Character.toString(upperLeft.charAt(0)))) {
							upperRightCount = 1;
							upperState = 2;
							i = backup; // go back to first character
						} else {
							upperState = 0;
							i = backup; // go to second character
						}
					}

					if (upperLeftCount == upperLeft.length()) {
						upperLeftElement = firstElement;
						upperLeftExists = true;
						firstElement = null;
						
						lastGoodIndex = i;
						foundCount++;

						if (upperRightExists)
							upperState = 3;
						else
							upperState = 0;
					}

					break;
				}

				case 2: // evaluate chars for top right string
				{
					if (currChar == upperRight.charAt(upperRightCount)) {
						upperRightCount++;
					} else { // invalid char detected
						upperRightCount = 0;
						upperState = 0;
						i = backup;
					}

					if (upperRightCount == upperRight.length()) {
						upperRightElement = currElement;
						upperRightExists = true;
						firstElement = null;
						
						lastGoodIndex = i;
						foundCount++;

						if (upperLeftExists)
							upperState = 3; // replace
						else
							upperState = 0;
					}

					break;
				}

				case 3: // found both upper strings, probably not needed because
						// of if statement
				{
					break;
				}
			}
				
				break;
			
			case 1: // lower strings
			//else {
				switch (lowerState) {
				case 0: // search for first char of either top string
				{
					// valid first lower left
					if (lowerLeft.startsWith(Character.toString(currChar)) && !(lowerLeftExists))
					{
						if (lowerLeft.length() == 1) // single character case
						{
							lowerLeftElement = currElement;
							lowerLeftExists = true;
							lastGoodIndex = i;
							foundCount++;
						}

						else {
							firstElement = currElement;
							backup = i;
							lowerLeftCount = 1;
							lowerState = 1;
						}
					}

					// valid first lower right
					else if (lowerRight.startsWith(Character.toString(currChar)) && !(lowerRightExists))
					{
						if (lowerRight.length() == 1) // single character case
						{
							lowerRightElement = currElement;
							lowerRightExists = true;
							lastGoodIndex = i;
							foundCount++;
						} else {
							firstElement = currElement;
							backup = i;
							lowerRightCount = 1;
							lowerState = 2;
						}
					}

					break;
				}

				case 1: // evaluate chars for top left string
				{
					if (currChar == lowerLeft.charAt(lowerLeftCount)) {
						lowerLeftCount++;
					} else { // invalid char detected
						lowerLeftCount = 0;

						//if (lowerLeft.charAt(0) == lowerRight.charAt(0)) {
						if (lowerRight.startsWith(Character.toString(lowerLeft.charAt(0)))) {
							lowerRightCount = 1;
							lowerState = 2;
							i = backup; // go back to first character
						} else {
							lowerState = 0;
							i = backup; // go to second character
						}
					}

					if (lowerLeftCount == lowerLeft.length()) {
						lowerLeftElement = firstElement;
						lowerLeftExists = true;
						firstElement = null;
						
						lastGoodIndex = i;
						foundCount++;

						if (lowerRightExists)
							lowerState = 3;
						else
							lowerState = 0;
					}

					break;
				}

				case 2: // evaluate chars for top right string
				{
					if (currChar == lowerRight.charAt(lowerRightCount)) {
						lowerRightCount++;
					} else { // invalid char detected
						lowerRightCount = 0;
						lowerState = 0;
						i = backup;
					}

					if (lowerRightCount == lowerRight.length()) {
						lowerRightElement = currElement;
						lowerRightExists = true;
						firstElement = null;
						
						lastGoodIndex = i;
						foundCount++;

						if (lowerLeftExists)
							lowerState = 3;
						else
							lowerState = 0;
					}

					break;
				}

				case 3: 
				{
					// zzz
					break;
				}
			}
				
				break;
			}
			}
			// this is so dumb
			catch(NullPointerException npe){
				//System.out.println(npe);
			}
			
			// DONT LET THIS BECOME AN INF LOOP!
			// if at end of page and only one upper string detected, need to roll back to last good index
			// then begin searching for the bottom strings
			if((i == (size - 1)) && !(upperRightExists && upperLeftExists) && (foundCount == 1) && (resetCount == 0)){
				resetCount = 1;
				i = lastGoodIndex;
				searchArea = 1;
			}
		} // end of for loop

		// need at least 3 matched strings to form box
		if (foundCount < 3)	{
			return new ArrayList<Rectangle>(); // empty arraylist
		}

		// this is sooooo dumb
		float leftBound;
		float topBound;
		float width;
		float height;
		
		if(!upperLeftExists && lowerLeftExists) leftBound = lowerLeftElement.x - lowerLeftElement.width;
		else if(upperLeftExists && !lowerLeftExists) leftBound = upperLeftElement.x - upperLeftElement.width;
		else leftBound = Math.min(upperLeftElement.x - upperLeftElement.width,
				lowerLeftElement.x - lowerLeftElement.width);
		
		if(!upperLeftExists && upperRightExists) topBound = upperRightElement.x - upperRightElement.width;
		else if(upperLeftExists && !upperRightExists) topBound = upperLeftElement.x - upperLeftElement.width;
		else topBound = Math.min(upperLeftElement.y - upperLeftElement.height,
				upperRightElement.y - upperRightElement.height);
		
		if(!upperRightExists && lowerRightExists) width = lowerRightElement.x + lowerRightElement.width - leftBound;
		else if(upperRightExists && !lowerRightExists) width = upperRightElement.x + upperRightElement.width - leftBound;
		else width = Math.max((upperRightElement.x + upperRightElement.width - leftBound),
				(lowerRightElement.x + lowerRightElement.width - leftBound));
		
		if(!lowerRightExists && lowerLeftExists) height = lowerLeftElement.y + lowerLeftElement.height - topBound;
		else if(lowerRightExists && !lowerLeftExists) height = lowerRightElement.y + lowerRightElement.height - topBound;
		else height = Math.max((lowerLeftElement.y + lowerLeftElement.height - topBound),
				(lowerRightElement.y + lowerRightElement.height - topBound));
		
		
		/*
		// may want to check if parameters match left/right schema first
		float leftBound = Math.min(upperLeftElement.x - upperLeftElement.width,
				lowerLeftElement.x - lowerLeftElement.width);
		float topBound = Math.min(upperLeftElement.y - upperLeftElement.height,
				upperRightElement.y - upperRightElement.height);
		float width = Math.max((upperRightElement.x + upperRightElement.width - leftBound),
				(lowerRightElement.x + lowerRightElement.width - leftBound));
		float height = Math.max((lowerLeftElement.y + lowerLeftElement.height - topBound),
				(lowerRightElement.y + lowerRightElement.height - topBound));
		*/
		
		Rectangle foundRectangle = new Rectangle(topBound, leftBound, width, height);
		regexList.add(foundRectangle);

		/*for (Rectangle walk : regexList) {
			System.out.println(
					"Box found at x=" + walk.x + " y=" + walk.y + " width=" + walk.width + " height=" + walk.height);
		}*/
		return regexList;
	}
}
