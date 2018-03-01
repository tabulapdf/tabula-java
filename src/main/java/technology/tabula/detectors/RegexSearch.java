package technology.tabula.detectors;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	private static final Integer INIT=0;

	private Pattern _regexBeforeTable;
	private Pattern _regexAfterTable;
	
	private ArrayList<MatchingArea> _matchingAreas;
	
	private Boolean _includeRegexBeforeTable;
	private Boolean _includeRegexAfterTable;
	
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

	public String getPatternBefore() { return new String(_regexBeforeTable.toString());}
	public String getPatternAfter() { return new String(_regexAfterTable.toString());}

	public ArrayList<Rectangle> getMatchingAreasForPage(Integer pageNumber){
		
        ArrayList<Rectangle> allMatchingAreas = new ArrayList<>();
		
		for( MatchingArea matchingArea : _matchingAreas) {
			allMatchingAreas.addAll(matchingArea.get(pageNumber));
		}

		 return allMatchingAreas;	
	}

	// TODO: New code added to accommodate for for CLI Regex, see if it works
    public ArrayList<Rectangle> getAllMatchingAreas(){

        ArrayList<Rectangle> allPagesMatchingAreas = new ArrayList<>();

        for(MatchingArea matchingArea : _matchingAreas){
            for( int i : matchingArea.keySet())
            allPagesMatchingAreas.addAll(matchingArea.get(i));
        }
        return allPagesMatchingAreas;
    }
	
	  /*
     * Inner class to retain information about a potential matching area while
     * iterating over the document and performing calculations to determine the rectangular 
     * area coordinates for matching areas. This may be overkill...
     */
	private final class DetectionData{
		DetectionData(){
			_pageBeginMatch = new AtomicInteger(INIT);
			_pageEndMatch = new AtomicInteger(INIT);
			_pageBeginCoord = new Point2D.Float();
			_pageEndCoord= new Point2D.Float();
		}
		
		AtomicInteger       _pageBeginMatch;
		AtomicInteger       _pageEndMatch;
		Point2D.Float       _pageBeginCoord;
		Point2D.Float       _pageEndCoord;

	}

	static final class SignOfOffset{
		public static final double POSITIVE_NO_BUFFER = 1;
        public static final double POSITIVE_WITH_BUFFER = 1.5;
        public static final double NEGATIVE_BUFFER = -.5;
    //    public static final double NEGATIVE_NO_BUFFER = -1;
    //    public static final double NEGATIVE_WITH_BUFFER = -1.5;
        public static final int NONE = 0;
	};



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
	
	LinkedList<DetectionData> potentialMatches = new LinkedList<>();
	potentialMatches.add(new DetectionData());

	for(Integer currentPage=1;currentPage<=totalPages;currentPage++) {
		
		/*
		 * Convert PDF page to text
		 */
		Page page = oe.extract(currentPage);
		ArrayList<TextElement> pageTextElements = (ArrayList<TextElement>) page.getText();
		StringBuilder pageAsText = new StringBuilder();


		for(TextElement element : pageTextElements ) {
			pageAsText.append(element.getText());
		}

		/*
		 * Find each table on each page + tables which span multiple pages
		 */

		Integer startMatchingAt = 0;
		Matcher beforeTableMatches = _regexBeforeTable.matcher(pageAsText);
		Matcher afterTableMatches  = _regexAfterTable.matcher(pageAsText);
		
		while( beforeTableMatches.find(startMatchingAt) || afterTableMatches.find(startMatchingAt)) {

			DetectionData tableUnderDetection;
			DetectionData lastTableUnderDetection=potentialMatches.getLast();

			if((lastTableUnderDetection._pageBeginMatch.get()==INIT) || (lastTableUnderDetection._pageEndMatch.get()==INIT)){
			   tableUnderDetection = lastTableUnderDetection;
			}
			else if(lastTableUnderDetection._pageEndMatch.get()<lastTableUnderDetection._pageBeginMatch.get()){
				tableUnderDetection = lastTableUnderDetection;
			}
			else if(lastTableUnderDetection._pageEndCoord.getY()<lastTableUnderDetection._pageBeginCoord.getY() &&
					(lastTableUnderDetection._pageEndMatch.get()==lastTableUnderDetection._pageBeginMatch.get())){
				tableUnderDetection = lastTableUnderDetection;
			}

            else{
				tableUnderDetection = new DetectionData();
				potentialMatches.add(tableUnderDetection);
			}

			Integer beforeTableMatchLoc = (beforeTableMatches.find(startMatchingAt)) ? beforeTableMatches.start() : null;
			Integer afterTableMatchLoc = (afterTableMatches.find(startMatchingAt))? afterTableMatches.start() : null;

			Matcher firstMatchEncountered;
			double offsetScale;
			AtomicInteger pageToFind;
			Point2D.Float coordsToFind;

			Boolean bothMatchesEncountered = (beforeTableMatchLoc!=null) && (afterTableMatchLoc!=null);
			if(bothMatchesEncountered){
				//
				// In the instance the Table Beginning Pattern and Table End Pattern both match a given text element,
				// the element chosen is dependent on what is currently in the tableUnderDetection
				//
				if(beforeTableMatchLoc.intValue() == afterTableMatchLoc.intValue()){
					Boolean beginNotFoundYet = tableUnderDetection._pageBeginMatch.get()==INIT;
					firstMatchEncountered = (beginNotFoundYet) ? beforeTableMatches : afterTableMatches;

					//    --------------------------------
					//    Table Beginning  <------ |Offset
					//      Content                          (To include beginning, negative offset added: coords on top-left but buffer is needed)
					//      Content
 					//      Content                         (To include end, positive offset added)
					//    Table End        <------ |Offset
					//    --------------------------------

                    offsetScale = (beginNotFoundYet) ?
							                               //Negative offset for inclusion     Positive offset for exclusion
							 ((_includeRegexBeforeTable) ? SignOfOffset.NEGATIVE_BUFFER : SignOfOffset.POSITIVE_NO_BUFFER ):
							                              //Positive offset for inclusion    No offset for exclusion
							 ((_includeRegexAfterTable) ? SignOfOffset.POSITIVE_WITH_BUFFER: SignOfOffset.NONE);
					pageToFind = (beginNotFoundYet) ? tableUnderDetection._pageBeginMatch : tableUnderDetection._pageEndMatch;
					coordsToFind = (beginNotFoundYet) ? tableUnderDetection._pageBeginCoord : tableUnderDetection._pageEndCoord;

				}
				else{

					Boolean beginLocFoundFirst = beforeTableMatchLoc<afterTableMatchLoc;
					firstMatchEncountered = (beginLocFoundFirst)? beforeTableMatches : afterTableMatches;
					offsetScale = (beginLocFoundFirst) ?
							((_includeRegexBeforeTable) ? SignOfOffset.NEGATIVE_BUFFER : SignOfOffset.POSITIVE_NO_BUFFER ):
							((_includeRegexAfterTable) ? SignOfOffset.POSITIVE_WITH_BUFFER: SignOfOffset.NONE);
					pageToFind = (beginLocFoundFirst) ? tableUnderDetection._pageBeginMatch : tableUnderDetection._pageEndMatch;
					coordsToFind = (beginLocFoundFirst) ? tableUnderDetection._pageBeginCoord : tableUnderDetection._pageEndCoord;
				}
			}
			else{
				Boolean beginLocNotFound = (beforeTableMatchLoc==null);
				firstMatchEncountered = (beginLocNotFound) ? afterTableMatches : beforeTableMatches;
				offsetScale = (beginLocNotFound) ?
						((_includeRegexAfterTable) ? SignOfOffset.POSITIVE_WITH_BUFFER: SignOfOffset.NONE):
				        ((_includeRegexBeforeTable) ? SignOfOffset.NEGATIVE_BUFFER : SignOfOffset.POSITIVE_NO_BUFFER);
				pageToFind = (beginLocNotFound) ? tableUnderDetection._pageEndMatch : tableUnderDetection._pageBeginMatch;
				coordsToFind = (beginLocNotFound) ? tableUnderDetection._pageEndCoord : tableUnderDetection._pageBeginCoord;
			}

			Integer firstMatchIndex = firstMatchEncountered.start();

			Float xCoordinate = pageTextElements.get(firstMatchIndex).x;
			Float yCoordinate = pageTextElements.get(firstMatchIndex).y;
			Float offset = pageTextElements.get(firstMatchIndex).height;
			yCoordinate += (float)(offset*offsetScale);

			coordsToFind.setLocation(xCoordinate,yCoordinate);

			pageToFind.set(currentPage);

            startMatchingAt = firstMatchEncountered.end();
		}
	}	

	/*
	 * Remove the last potential match if its data is incomplete
	 */
	DetectionData lastPotMatch = potentialMatches.getLast();
	
	if((lastPotMatch._pageBeginMatch.get()==INIT) || (lastPotMatch._pageEndMatch.get()==INIT)) {
		potentialMatches.removeLast();
	}
	else if((lastPotMatch._pageEndMatch.get()<lastPotMatch._pageBeginMatch.get())){
		potentialMatches.removeLast();
	}
	else if((lastPotMatch._pageEndMatch.get()==lastPotMatch._pageBeginMatch.get())&&
			(lastPotMatch._pageEndCoord.getY()<lastPotMatch._pageBeginCoord.getY())){
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
		
		ArrayList<MatchingArea> matchingAreas = new ArrayList<>();
		
		ObjectExtractor oe = new ObjectExtractor(document);

		
		while(!foundMatches.isEmpty()) {

			DetectionData foundTable = foundMatches.pop();

            if(foundTable._pageBeginMatch.get() == foundTable._pageEndMatch.get()) {
            
            	float width = oe.extract(foundTable._pageBeginMatch.get()).width;
            	float height = foundTable._pageEndCoord.y-foundTable._pageBeginCoord.y;
            	
            	LinkedList<Rectangle> tableArea = new LinkedList<>();
            	tableArea.add( new Rectangle(foundTable._pageBeginCoord.y,0,width,height)); //TODO:Figure out how/what must be done to support multi-column texts (4 corners??)
            	
            	MatchingArea matchingArea = new MatchingArea();
            	matchingArea.put(foundTable._pageBeginMatch.get(), tableArea);
            
            	matchingAreas.add(matchingArea);
            
			}
            else {
            	
            	MatchingArea matchingArea = new MatchingArea();
            	/*
            	 * Create sub-area for table from directly below the pattern-before-table content to the end of the page
            	 */
            	Page currentPage =  oe.extract(foundTable._pageBeginMatch.get());
            	LinkedList<Rectangle> tableSubArea = new LinkedList<>();

            	tableSubArea.add( new Rectangle(foundTable._pageBeginCoord.y,0,currentPage.width,
            			                        currentPage.height-foundTable._pageBeginCoord.y)); //TODO:Figure out how/what must be done to support multi-column texts (4 corners??)
            	
            	matchingArea.put(foundTable._pageBeginMatch.get(), tableSubArea);
            	
            	/*
            	 * Create sub-areas for table that span the entire page
            	 */
            	for (Integer iter=currentPage.getPageNumber()+1; iter<foundTable._pageEndMatch.get(); iter++) {
            		currentPage = oe.extract(iter);
            		tableSubArea = new LinkedList<>();
            		tableSubArea.add(new Rectangle(0,0,currentPage.width,currentPage.height));
            		
            		matchingArea.put(currentPage.getPageNumber(), tableSubArea);
            		
            	}
                
            	/*
            	 * Create sub-areas for table from the top of the page to directly before the pattern-after-table content 
            	 */
            	
            	currentPage = oe.extract(foundTable._pageEndMatch.get());
                tableSubArea = new LinkedList<>();
                tableSubArea.add(new Rectangle(0,0,currentPage.width,foundTable._pageEndCoord.y));

                matchingArea.put(currentPage.getPageNumber(), tableSubArea);
                matchingAreas.add(matchingArea);
            }
		}

		return matchingAreas;
	}
}
