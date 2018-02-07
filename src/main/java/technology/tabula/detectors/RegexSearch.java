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
 *    This class supports regex-based content extraction from PDF Documents
 *    
 *    TODO: Large blurb about this class
 *    10/29/2017 REM; created.
 *    1/13/2018  REM; updated detectMatchingAreas to resolve pattern-detection bug
 *    1/27/2018  REM; added constructors to facilitate header/footer functionality as well as CLI work
 *    1/30/2018  REM; added static method skeleton for proof-of-concept header work, also added documentation
 *    2/4/2018   REM; added UpdatesOnResize so that the appropriate data can be passed back to the front-end
 *
 */


public class RegexSearch {

	/*
	 * This class is leveraged by convertPageSectionToString so that the TextElement ArrayList and the string
	 * associated with a given PDF page can both be returned to the caller
	 */

	/* checkSearchesFilterResize
	 *
	 * Determines which RegexSearch objects should be modified as a result of the user changing/defining a header filter
	 * for a page in the document
	 *
	 * @param file The PDDocument representation of the file
	 * @param currentRegexSearches The array of RegexSearch objects used on this document
	 * @param pageNumOfHeaderResize The number of the page upon which the header filter was resized
	 * @param pageHeight The height of the page containing the header filter resize event AS IT APPEARED IN THE GUI
	 * @param newHeaderHeight The new height of the header filter AS IT APPEARED IN THE GUI
	 * @param previousHeaderHeight The previous height of the header filter AS IT APPEARED IN THE GUI
	 * @return
	 */
	public static ArrayList<UpdatesOnResize> checkSearchesOnFilterResize( PDDocument file, Integer pageNumOfResizedFilter,
													         FilteredArea previousFilterArea,
													         HashMap<Integer,FilteredArea> areasToFilter,
															 RegexSearch[] currentRegexSearches){

		System.out.println("In checkSearchesOnFilterResize:");

		ObjectExtractor oe = new ObjectExtractor(file);
		Page pageOfHeaderResize = oe.extract(pageNumOfResizedFilter);

		ArrayList<RegexSearch> searchesToReRun = new ArrayList<RegexSearch>();

		Integer previousHeaderHeight = previousFilterArea.getScaledHeaderHeight();
		Integer currentHeaderHeight = areasToFilter.get(pageNumOfResizedFilter).getScaledHeaderHeight();

		PageTextMetaData contentCheckedOnResize;

        if(currentHeaderHeight>previousHeaderHeight) { //Header has been expanded <-- this content must be checked
			contentCheckedOnResize = new PageTextMetaData(pageOfHeaderResize,
					new Rectangle(0, 0, pageOfHeaderResize.width, currentHeaderHeight));
		}

		else{ //Header has been shrunk <-- check content in area between old header and new header
        	contentCheckedOnResize = new PageTextMetaData(pageOfHeaderResize,
					                                      new Rectangle(currentHeaderHeight,0, pageOfHeaderResize.width,
																        previousHeaderHeight-currentHeaderHeight));
		}

		ArrayList<UpdatesOnResize> updatedSearches = new ArrayList<UpdatesOnResize>();

		for(RegexSearch regexSearch : currentRegexSearches){
			if(regexSearch.containsMatchIn(contentCheckedOnResize.pageAsText)){
				System.out.println("Regex Search contains match:");
				searchesToReRun.add(regexSearch);
			}
		}
		//For now, skirting the check-overlaps issue...but it will need to be handled soon and will require user-input...
		//Most likely the cuba framework will facilitate a re-run of the parameters following a removal of a given query...
		for(RegexSearch regexSearch : searchesToReRun){

			ArrayList<MatchingArea> areasRemoved;
			ArrayList<MatchingArea> areasAddedOrModified = new ArrayList<MatchingArea>();

			ArrayList<MatchingArea> matchingAreasBeforeResize = (ArrayList<MatchingArea>) regexSearch._matchingAreas.clone();

        	regexSearch.detectMatchingAreas(file,areasToFilter);

        	for(MatchingArea matchingArea : regexSearch._matchingAreas){
				if (matchingAreasBeforeResize.contains(matchingArea)) {
					matchingAreasBeforeResize.remove(matchingArea);
				}
				else{
					areasAddedOrModified.add(matchingArea);
				}
			}

			areasRemoved = matchingAreasBeforeResize;

			updatedSearches.add(new UpdatesOnResize(regexSearch,areasAddedOrModified,areasRemoved,false));
		}

		return updatedSearches;
	}

	private static class UpdatesOnResize{
		RegexSearch updatedRegexSearch; //The RegexSearch object that is being updated
		ArrayList<MatchingArea> areasChangedOrAdded;  //New Areas discovered or areas modified upon resize event
		ArrayList<MatchingArea> areasRemoved;        //Previous Area dimensions for areas changed by resize event
		Boolean overlapsAnotherSearch; //Flag for when resize causes areas to be found overlapping a present query <--TODO: this variable is always false for now...need to update this once an overlap algorithm is written

		public UpdatesOnResize(RegexSearch regexSearch, ArrayList<MatchingArea> newAreasToAdd, ArrayList<MatchingArea> oldAreasToRemove,
							   Boolean resizeEventCausedAnOverlap){
			updatedRegexSearch = regexSearch;
			areasChangedOrAdded = newAreasToAdd;
			areasRemoved = oldAreasToRemove;
			overlapsAnotherSearch = resizeEventCausedAnOverlap;
		}
	}

	private static final Integer INIT=0;

	private Pattern _regexBeforeTable;
	private Pattern _regexAfterTable;
	
	private ArrayList<MatchingArea> _matchingAreas;
	
	private Boolean _includeRegexBeforeTable;
	private Boolean _includeRegexAfterTable;



	/*
	 * This constructor is designed to be used for parameters originating in JSON and where no header areas are defined
	 * NOTE: This constructor will soon be deprecated!!
	 * @param regexBeforeTable The text pattern that occurs in the document directly before the table that is to be extracted
	 * @param regexAfterTable The text pattern that occurs in the document directly after the table that is to be extracted
	 * @param PDDocument The PDFBox model of the PDF document uploaded by the user.
	 */
	public RegexSearch(String regexBeforeTable, String includeRegexBeforeTable, String regexAfterTable, 
			           String includeRegexAfterTable, PDDocument document) {

		this(regexBeforeTable, includeRegexBeforeTable, regexAfterTable, includeRegexAfterTable, document,null);
	}

	public RegexSearch(String regexBeforeTable, String includeRegexBeforeTable, String regexAfterTable,
					   String includeRegexAfterTable, PDDocument document, HashMap<Integer,FilteredArea> AreasToFilter) {
		
		this(regexBeforeTable,Boolean.valueOf(includeRegexBeforeTable),regexAfterTable,
			Boolean.valueOf(includeRegexAfterTable),document,AreasToFilter);
		
	}

	public RegexSearch(String regexBeforeTable,Boolean includeRegexBeforeTable, String regexAfterTable,
					   Boolean includeRegexAfterTable, PDDocument document, HashMap<Integer,FilteredArea> areasToFilter) {
		_regexBeforeTable = Pattern.compile(regexBeforeTable);
		_regexAfterTable = Pattern.compile(regexAfterTable);

		_includeRegexBeforeTable = includeRegexBeforeTable;
		_includeRegexAfterTable = includeRegexAfterTable;

		detectMatchingAreas(document,areasToFilter);

	}

	/* getRegexBeforeTable: basic getter function
	 * @return The regex pattern used to delimit the beginning of the table
	 */
	public String getRegexBeforeTable(){
		return _regexBeforeTable.toString();
	}
	/* getRegexAfterTable: basic getter function
	 * @return The regex pattern used to delimit the end of the table
	 */
	public String getRegexAfterTable(){
		return _regexAfterTable.toString();
	}

    /*
     * This class maps on a per-page basis the areas (plural) of the PDF document that fall between text matching the
     * user-provided regex (this allows for tables that span multiple pages to be considered a single entity).
     * The key is the page number that the areas first begin. The LinkedList of Rectangles allows for multiple
     * areas to be associated with a given match (as in the case of multiple pages)
     */
	private class MatchingArea extends HashMap<Integer,LinkedList<Rectangle>> {}

	/*
	 * @param pageNumber The one-based index into the document
	 * @return ArrayList<Rectangle> The values stored in _matchingAreas for a given page	
	 */
	public ArrayList<Rectangle> getMatchingAreasForPage(Integer pageNumber){
		
        ArrayList<Rectangle> allMatchingAreas = new ArrayList<>();
		
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
		private static final double POSITIVE_NO_BUFFER = 1;
        private static final double POSITIVE_WITH_BUFFER = 1.5;
        private static final double NEGATIVE_BUFFER = -.5;
    //    public static final double NEGATIVE_NO_BUFFER = -1;
    //    public static final double NEGATIVE_WITH_BUFFER = -1.5;
        private static final int NONE = 0;
	};


	public static class PageTextMetaData{

		private String pageAsText;
		private ArrayList<TextElement> pageMetaData;

		/*
         * @param page The page of the document containing the section of text desired
         * @param sectionToConvert The rectangular coordinates of the desired section
         */
		public PageTextMetaData(Page page, Rectangle sectionToConvert){

			pageMetaData = (ArrayList<TextElement>) page.getText(sectionToConvert);

			StringBuilder headerTextAsString = new StringBuilder();

			for(TextElement element : pageMetaData ) {
				headerTextAsString.append(element.getText());
			}

			pageAsText = headerTextAsString.toString();
		}

		public String getPageAsText(){
			return pageAsText;
		}

		public ArrayList<TextElement> getPageMetaData(){
			return pageMetaData;
		}
	}


	public static class FilteredArea{
		private Integer headerHeight; //The height of the header AS IT APPEARS IN THE GUI
		private Integer footerHeight; //The height of the footer AS IT APPEARS IN THE GUI
		private Integer guiPageHeight;   //The height of the page AS IT APPEARS IN THE GUI
		private Integer absolutePageHeight; //The height of the page AS IT APPEARS IN THE BACK-END
        private Integer scaledHeaderHeight; //The height of the header AS IT APPEARS IN THE BACK-END
		private Integer scaledFooterHeight; //The height of the footer AS IT APPEARS IN THE BACK-END

		public FilteredArea(Integer heightOfHeader, Integer heightOfFooter, Integer guiHeightOfPage, Integer absPageHeight){
			headerHeight = heightOfHeader;
			footerHeight = heightOfFooter;
			guiPageHeight = guiHeightOfPage;
			absolutePageHeight = absPageHeight;

			scaledHeaderHeight = (guiPageHeight==0) ? 0 : (int)((((double) headerHeight)/guiPageHeight)*absolutePageHeight);
			scaledFooterHeight = (guiPageHeight==0) ? 0 : (int)((((double) footerHeight)/guiPageHeight)*absolutePageHeight);

			System.out.println("Height of header:" + heightOfHeader);
			System.out.println("Height of page in gui:" + guiPageHeight);
			System.out.println("Height of page in back-end:" + absolutePageHeight);
		}

		private Integer getScaledHeaderHeight(){ return scaledHeaderHeight; }

		private Integer getScaledFooterHeight(){ return scaledFooterHeight;}

	}

	/*
	 * containsMatchIn: Checks to see if patternBefore or patternAfter matches a string of text
	 *
	 * @param text The string of data we are looking at
	 * @return Boolean indicating the matching status of text
	 * that matches the user-provided regex
	 */

	private Boolean containsMatchIn(String text){

		Matcher beforeTableMatch = _regexBeforeTable.matcher(text);
		Matcher afterTableMatch = _regexAfterTable.matcher(text);

		return ((beforeTableMatch.find()) || (afterTableMatch.find()));
	}


	/*
	 * detectMatchingAreas: Detects the subsections of the document occurring 
	 *                      between the user-specified regexes. 
	 * 
	 * @param document The name of the document for which regex has been applied
	 * @param headerAreas The header sections of the document that are to be ignored.
	 * @return ArrayList<MatchingArea> A list of the sections of the document that occur between text 
	 * that matches the user-provided regex
	 */
	
	private void detectMatchingAreas(PDDocument document, HashMap<Integer,FilteredArea> AreasToFilter) {


	ObjectExtractor oe = new ObjectExtractor(document);
	Integer totalPages = document.getNumberOfPages();
	
	LinkedList<DetectionData> potentialMatches = new LinkedList<>();
	potentialMatches.add(new DetectionData());

	for(Integer currentPage=1;currentPage<=totalPages;currentPage++) {
		/*
		 * Convert PDF page to text
		 */
		Page page = oe.extract(currentPage);

		FilteredArea filterArea  = ( (AreasToFilter!=null) && AreasToFilter.containsKey(page.getPageNumber())) ?
				              AreasToFilter.get(page.getPageNumber()): new FilteredArea(0,0, (int) page.getHeight(), (int) page.getHeight());

		ArrayList<TextElement> pageTextElements = (ArrayList<TextElement>) page.getText(
				new Rectangle(filterArea.getScaledHeaderHeight(),0, (float)page.getWidth(),
				Math.round(page.getHeight()-filterArea.getScaledHeaderHeight()-filterArea.getScaledFooterHeight())));

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

	_matchingAreas = calculateMatchingAreas(potentialMatches,document);



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
