package technology.tabula.detectors;

import java.awt.geom.Point2D;
import java.util.*;
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
/*
	private static void determineFooterResizeImpact(Integer previousHeight,
													Integer currentHeight,
													PDDocument file,
													Page pageOfFooterResize,
													HashMap<Integer,FilteredArea> areasToFilter,
													RegexSearch[] currentRegexSearches,
													ArrayList<UpdatesOnResize> updatedSearches) {

		Integer pageNumOfFooterResize = pageOfFooterResize.getPageNumber();

		System.out.println("Previous Height of Footer:"+previousHeight);
		System.out.println("Current Height of Footer:"+currentHeight);


		if ( currentHeight > previousHeight) {

			System.out.println("Footer has been expanded...");

			for (RegexSearch regexSearch : currentRegexSearches) {
				ArrayList<MatchingArea> areasToRemove = new ArrayList<>();
				ArrayList<MatchingArea> areasToAdd;

				for (MatchingArea matchingArea : regexSearch._matchingAreas) {

					FilteredArea areaToFilter = areasToFilter.get(pageNumOfFooterResize);

					TableArea overlappedSection = matchingArea.sectionOverlappingFooter(pageNumOfFooterResize, areaToFilter);
					if (overlappedSection != null) {
						Integer indexOfOverlappingMatch = regexSearch._matchingAreas.indexOf(matchingArea);

						Integer indexOfMatchBeforeOverlap = indexOfOverlappingMatch;
						MatchingArea matchBeforeOverlap = null;
						if(indexOfOverlappingMatch>0) {
							while ((matchBeforeOverlap == null) && (--indexOfMatchBeforeOverlap > 0)) {
								matchBeforeOverlap = regexSearch._matchingAreas.get(indexOfMatchBeforeOverlap);
								if (matchBeforeOverlap.sectionOverlappingHeader(pageNumOfFooterResize, areaToFilter) != null) {
									matchBeforeOverlap = null;
								}
							}
						}

						Integer indexOfMatchAfterOverlap = indexOfOverlappingMatch;
						MatchingArea matchAfterOverlap = null;
						while ((matchAfterOverlap == null) && (++indexOfMatchAfterOverlap < regexSearch._matchingAreas.size())) {
							matchAfterOverlap = regexSearch._matchingAreas.get(indexOfMatchAfterOverlap);
							if (matchAfterOverlap.sectionOverlappingHeader(pageNumOfFooterResize, areaToFilter) != null) {
								matchAfterOverlap = null;
							}
						}


						for (int indexOfAreaToRemove = (matchBeforeOverlap == null) ? 0 : indexOfMatchBeforeOverlap + 1;
							 indexOfAreaToRemove < indexOfMatchAfterOverlap; indexOfAreaToRemove++) {
							areasToRemove.add(regexSearch._matchingAreas.get(indexOfAreaToRemove));
						}
						regexSearch._matchingAreas.removeAll(areasToRemove);

						Integer pageOfDetectStart = (matchBeforeOverlap == null) ? 1 : matchBeforeOverlap.getStartPageNum(); //Page-numbering is one-based
						TableArea lastSectionBeforeOverlap = (matchBeforeOverlap == null) ? null : matchBeforeOverlap.get(matchBeforeOverlap._endPageNum).getLast();
						Integer yOfDetectStart = (matchBeforeOverlap == null) ? null : lastSectionBeforeOverlap.getTop() + lastSectionBeforeOverlap.getHeight();

						System.out.println("Page of Detect Start:" + pageOfDetectStart);
						System.out.println("y of Detect Start:" + yOfDetectStart);


						Integer pageOfDetectEnd = (matchAfterOverlap == null) ? file.getNumberOfPages() : matchAfterOverlap.getStartPageNum();
						TableArea firstSectionAfterOverlap = (matchAfterOverlap == null) ? null : matchAfterOverlap.get(matchAfterOverlap._startPageNum).getFirst();
						Integer yOfDetectEnd = (matchAfterOverlap == null) ? null : firstSectionAfterOverlap.getTop();

						System.out.println("Page of Detect End:" + pageOfDetectEnd);
						System.out.println("y of Detect End:" + yOfDetectEnd);

						areasToAdd = regexSearch.detectMatchingAreas(file, areaToFilter, pageOfDetectStart, yOfDetectStart, pageOfDetectEnd, yOfDetectEnd);


						System.out.println("Areas To Add Length:" + areasToAdd.size());
						System.out.println("Areas to Remove Length:" + areasToRemove.size());
						regexSearch._matchingAreas.addAll(indexOfMatchBeforeOverlap, areasToAdd);

						updatedSearches.add(new UpdatesOnResize(regexSearch, areasToAdd, areasToRemove, false));

						break;
					}
				}
			}
		}
		else{ //Footer has been shrunk <-- check content in area between old header and new header

			System.out.println("Footer has been shrunk...");

			ArrayList<MatchingArea> areasToRemove = new ArrayList<>();
			ArrayList<MatchingArea> areasToAdd;

			MatchingArea matchBeforeFooter = null;
			MatchingArea matchAfterFooter = null;

			for (RegexSearch regexSearch : currentRegexSearches) {
				for (MatchingArea matchingArea : regexSearch._matchingAreas) {
					if ((matchingArea.getStartPageNum() <= pageNumOfFooterResize) &&
							(matchingArea.getEndPageNum() > pageNumOfFooterResize)) {
						matchBeforeFooter = matchingArea;
					}
					else{
						TableArea sectionWithOverlap = matchingArea.sectionOverlappingFooter(pageNumOfFooterResize,
								areasToFilter.get(pageNumOfFooterResize));
						if(matchingArea.getStartPageNum()> pageNumOfFooterResize && sectionWithOverlap==null){
							if(matchAfterFooter==null || (matchAfterFooter.getStartPageNum() > matchingArea.getStartPageNum())) {
								matchAfterFooter = matchingArea;
							}
						}
					}
				}



				int minIndex = (matchBeforeFooter == null) ? 0 : regexSearch._matchingAreas.indexOf(matchBeforeFooter);
				int maxIndex = (matchAfterFooter == null) ? regexSearch._matchingAreas.size() :
						regexSearch._matchingAreas.indexOf(matchAfterFooter);

				for (int indexOfAreaToRemove = minIndex; indexOfAreaToRemove < maxIndex; indexOfAreaToRemove++) {
					areasToRemove.add(regexSearch._matchingAreas.get(indexOfAreaToRemove));
				}
				regexSearch._matchingAreas.removeAll(areasToRemove);

				Integer pageOfDetectStart = (matchBeforeFooter == null) ? 1 : matchBeforeFooter.getStartPageNum(); //Page-numbering is one-based
				TableArea lastSectionBeforeOverlap = (matchBeforeFooter == null) ? null : matchBeforeFooter.get(matchBeforeFooter._endPageNum).getLast();
				Integer yOfDetectStart = (matchBeforeFooter == null) ? null : lastSectionBeforeOverlap.getTop() + lastSectionBeforeOverlap.getHeight();

				Integer pageOfDetectEnd = (matchAfterFooter == null) ? file.getNumberOfPages() : matchAfterFooter.getStartPageNum();
				TableArea firstSectionAfterOverlap = (matchAfterFooter == null) ? null : matchAfterFooter.get(matchAfterFooter._startPageNum).getFirst();
				Integer yOfDetectEnd = (matchAfterFooter == null) ? null : firstSectionAfterOverlap.getTop();


				System.out.println("Page of Detect Start:" + pageOfDetectStart);
				System.out.println("y of Detect Start:" + yOfDetectStart);

				System.out.println("Page of Detect End:" + pageOfDetectEnd);
				System.out.println("y of Detect End:" + yOfDetectEnd);



				//TODO-time permitting, clean this up a bit...too much redundancy here.

				Float pageHeight = (float)pageOfFooterResize.getHeight();

				Boolean contentEndCloseToFilter = (Math.abs(Math.round(currentHeight -
						(pageHeight-pageOfFooterResize.getTextBounds().getBottom()))) < TEXT_BUFFER);

				Boolean noMatchesOutsideFooter = (matchBeforeFooter==null) && (matchAfterFooter==null);

				FilteredArea filteredArea = areasToFilter.get(pageNumOfFooterResize);
				Integer top = (filteredArea==null) ? 0 : filteredArea.getScaledHeaderHeight();

				Boolean potentialMatchInFooter = regexSearch.containsMatchIn(new PageTextMetaData(pageOfFooterResize,
						new Rectangle(pageHeight-previousHeight, 0, pageOfFooterResize.width,
								previousHeight-currentHeight)).pageAsText);

				if (contentEndCloseToFilter) {
					areasToAdd = regexSearch.detectMatchingAreas(file, areasToFilter, pageOfDetectStart, yOfDetectStart,
							pageOfDetectEnd, yOfDetectEnd);
					System.out.println("Areas to Add Length:" + areasToAdd.size());
					System.out.println("Areas to Remove Length:" + areasToRemove.size());
					regexSearch._matchingAreas.addAll(minIndex, areasToAdd);
					updatedSearches.add(new UpdatesOnResize(regexSearch, areasToAdd, areasToRemove, false));
				} else if ((noMatchesOutsideFooter) && (!potentialMatchInFooter) ){
					continue;
				}
				else {
					areasToAdd = regexSearch.detectMatchingAreas(file, areasToFilter, pageOfDetectStart, yOfDetectStart, pageOfDetectEnd, yOfDetectEnd);
					System.out.println("Areas to Add Length:" + areasToAdd.size());
					System.out.println("Areas to Remove Length:" + areasToRemove.size());
					regexSearch._matchingAreas.addAll(minIndex, areasToAdd);
					updatedSearches.add(new UpdatesOnResize(regexSearch, areasToAdd, areasToRemove, false));
				}
			}
		}
	}
*/
/*
	private static void determineHeaderResizeImpact(Integer previousHeight,
													Integer currentHeight,
													PDDocument file,
													Page pageOfHeaderResize,
													HashMap<Integer,FilteredArea> areasToFilter,
													RegexSearch[] currentRegexSearches,
													ArrayList<UpdatesOnResize> updatedSearches) {

		Integer pageNumOfHeaderResize = pageOfHeaderResize.getPageNumber();

		System.out.println("Previous Height:"+previousHeight);
		System.out.println("Current Height:"+currentHeight);


		if ( currentHeight > previousHeight) {

			System.out.println("Header has been expanded...");

			for (RegexSearch regexSearch : currentRegexSearches) {
				ArrayList<MatchingArea> areasToRemove = new ArrayList<>();
				ArrayList<MatchingArea> areasToAdd;

				for (MatchingArea matchingArea : regexSearch._matchingAreas) {

					FilteredArea areaToFilter = areasToFilter.get(pageNumOfHeaderResize);

					TableArea overlappedSection = matchingArea.sectionOverlappingHeader(pageNumOfHeaderResize, areaToFilter);
					if (overlappedSection != null) {
						Integer indexOfOverlappingMatch = regexSearch._matchingAreas.indexOf(matchingArea);
						Integer indexOfMatchBeforeOverlap = indexOfOverlappingMatch;
						MatchingArea matchBeforeOverlap = null;
						if (indexOfMatchBeforeOverlap > 0) {
							while ((matchBeforeOverlap == null) && (--indexOfMatchBeforeOverlap > 0)) {
								matchBeforeOverlap = regexSearch._matchingAreas.get(indexOfMatchBeforeOverlap);
								if (matchBeforeOverlap.sectionOverlappingHeader(pageNumOfHeaderResize, areaToFilter) != null) {
									matchBeforeOverlap = null;
								}
							}
						}

						MatchingArea matchAfterOverlap = null;
						Integer indexOfMatchAfterOverlap = indexOfOverlappingMatch;
						while ((matchAfterOverlap == null) && (++indexOfMatchAfterOverlap < regexSearch._matchingAreas.size())) {
							matchAfterOverlap = regexSearch._matchingAreas.get(indexOfMatchAfterOverlap);
							if (matchAfterOverlap.sectionOverlappingHeader(pageNumOfHeaderResize, areaToFilter) != null) {
								matchAfterOverlap = null;
							}
						}


						for (int indexOfAreaToRemove = (matchBeforeOverlap == null) ? 0 : indexOfMatchBeforeOverlap + 1;
							 indexOfAreaToRemove < indexOfMatchAfterOverlap; indexOfAreaToRemove++) {
							areasToRemove.add(regexSearch._matchingAreas.get(indexOfAreaToRemove));
						}
						//regexSearch._matchingAreas.removeAll(areasToRemove);

						Integer pageOfDetectStart = (matchBeforeOverlap == null) ? 1 : matchBeforeOverlap.getStartPageNum(); //Page-numbering is one-based
						TableArea lastSectionBeforeOverlap = (matchBeforeOverlap == null) ? null : matchBeforeOverlap.get(matchBeforeOverlap._endPageNum).getLast();
						Integer yOfDetectStart = (matchBeforeOverlap == null) ? null : lastSectionBeforeOverlap.getTop() + lastSectionBeforeOverlap.getHeight();

						System.out.println("Page of Detect Start:" + pageOfDetectStart);
						System.out.println("y of Detect Start:" + yOfDetectStart);


						Integer pageOfDetectEnd = (matchAfterOverlap == null) ? file.getNumberOfPages() : matchAfterOverlap.getStartPageNum();
						TableArea firstSectionAfterOverlap = (matchAfterOverlap == null) ? null : matchAfterOverlap.get(matchAfterOverlap._startPageNum).getFirst();
						Integer yOfDetectEnd = (matchAfterOverlap == null) ? null : firstSectionAfterOverlap.getTop();

						System.out.println("Page of Detect End:" + pageOfDetectEnd);
						System.out.println("y of Detect End:" + yOfDetectEnd);

						areasToAdd = regexSearch.detectMatchingAreas(file, areasToFilter, pageOfDetectStart, yOfDetectStart, pageOfDetectEnd, yOfDetectEnd);


						System.out.println("Areas To Add Length:" + areasToAdd.size());
						System.out.println("Areas to Remove Length:" + areasToRemove.size());
						regexSearch._matchingAreas.addAll(indexOfMatchBeforeOverlap, areasToAdd);

						updatedSearches.add(new UpdatesOnResize(regexSearch, areasToAdd, areasToRemove, false));

						break;
					}
				}
			}
		}
		else{ //Header has been shrunk <-- check content in area between old header and new header

			System.out.println("Header has been shrunk...");

			ArrayList<MatchingArea> areasToRemove = new ArrayList<>();
			ArrayList<MatchingArea> areasToAdd;

			MatchingArea areaBeforeHeader = null;
			MatchingArea areaAfterHeader = null;

			for (RegexSearch regexSearch : currentRegexSearches) {
				for (MatchingArea matchingArea : regexSearch._matchingAreas) {
					if ((matchingArea.getStartPageNum() < pageNumOfHeaderResize) &&
							(matchingArea.getEndPageNum() >= pageNumOfHeaderResize)) {
						areaBeforeHeader = matchingArea;
					} else if ((matchingArea.getStartPageNum() >= pageNumOfHeaderResize) &&
							(matchingArea.sectionOverlappingHeader(pageNumOfHeaderResize,
									areasToFilter.get(pageNumOfHeaderResize))) == null) {
						if (areaAfterHeader == null || areaAfterHeader.getStartPageNum() > matchingArea.getStartPageNum()) {
							areaAfterHeader = matchingArea;
						}
					}
				}

				int minIndex = (areaBeforeHeader == null) ? 0 : regexSearch._matchingAreas.indexOf(areaBeforeHeader);
				int maxIndex = (areaAfterHeader == null) ? regexSearch._matchingAreas.size() :
						regexSearch._matchingAreas.indexOf(areaAfterHeader);

				for (int indexOfAreaToRemove = minIndex; indexOfAreaToRemove < maxIndex; indexOfAreaToRemove++) {
					areasToRemove.add(regexSearch._matchingAreas.get(indexOfAreaToRemove));
				}
				regexSearch._matchingAreas.removeAll(areasToRemove);

				Integer pageOfDetectStart = (areaBeforeHeader == null) ? 1 : areaBeforeHeader.getStartPageNum(); //Page-numbering is one-based
				TableArea lastSectionBeforeOverlap = (areaBeforeHeader == null) ? null : areaBeforeHeader.get(areaBeforeHeader._endPageNum).getLast();
				Integer yOfDetectStart = (areaBeforeHeader == null) ? null : lastSectionBeforeOverlap.getTop() + lastSectionBeforeOverlap.getHeight();

				Integer pageOfDetectEnd = (areaAfterHeader == null) ? file.getNumberOfPages() : areaAfterHeader.getStartPageNum();
				TableArea firstSectionAfterOverlap = (areaAfterHeader == null) ? null : areaAfterHeader.get(areaAfterHeader._startPageNum).getFirst();
				Integer yOfDetectEnd = (areaAfterHeader == null) ? null : firstSectionAfterOverlap.getTop();


				System.out.println("Page of Detect Start:" + pageOfDetectStart);
				System.out.println("y of Detect Start:" + yOfDetectStart);

				System.out.println("Page of Detect End:" + pageOfDetectEnd);
				System.out.println("y of Detect End:" + yOfDetectEnd);



				//TODO-time permitting, clean this up a bit...to much redundancy here.
				if (Math.abs(Math.round(currentHeight - pageOfHeaderResize.getTextBounds().y)) < TEXT_BUFFER) {
					areasToAdd = regexSearch.detectMatchingAreas(file, areasToFilter, pageOfDetectStart, yOfDetectStart, pageOfDetectEnd, yOfDetectEnd);
					System.out.println("Areas to Add Length:" + areasToAdd.size());
					System.out.println("Areas to Remove Length:" + areasToRemove.size());
					regexSearch._matchingAreas.addAll(minIndex, areasToAdd);
					updatedSearches.add(new UpdatesOnResize(regexSearch, areasToAdd, areasToRemove, false));
				} else if ((areaBeforeHeader == null) && (areaAfterHeader == null) && !(regexSearch.containsMatchIn(new PageTextMetaData(pageOfHeaderResize,
						new Rectangle(currentHeight, 0, pageOfHeaderResize.width,
								previousHeight - currentHeight)).pageAsText))) {
					continue;
				} else {
					areasToAdd = regexSearch.detectMatchingAreas(file, areasToFilter, pageOfDetectStart, yOfDetectStart, pageOfDetectEnd, yOfDetectEnd);
					System.out.println("Areas to Add Length:" + areasToAdd.size());
					System.out.println("Areas to Remove Length:" + areasToRemove.size());
					regexSearch._matchingAreas.addAll(minIndex, areasToAdd);
					updatedSearches.add(new UpdatesOnResize(regexSearch, areasToAdd, areasToRemove, false));
				}
			}
		}
	}
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
	public static ArrayList<UpdatesOnResize> checkSearchesOnFilterResize( PDDocument file,
													         FilteredArea filterArea,
															 RegexSearch[] currentRegexSearches) {

		System.out.println("In checkSearchesOnFilterResize:");

		System.out.println("FilterArea height scale:" + filterArea.getHeaderHeightScale());



		ArrayList<UpdatesOnResize> updatedSearches = new ArrayList<>();
		for (RegexSearch regexSearch : currentRegexSearches){
			ArrayList<MatchingArea> areasToRemove = new ArrayList<MatchingArea>(regexSearch._matchingAreas);
			regexSearch._matchingAreas.clear();
			ArrayList<MatchingArea> areasToAdd = regexSearch._matchingAreas = regexSearch.detectMatchingAreas(file,filterArea);
			updatedSearches.add(new UpdatesOnResize(regexSearch,areasToRemove,areasToAdd,false));
		}
		return updatedSearches;


		/*
		ArrayList<UpdatesOnResize> updatedSearches = new ArrayList<UpdatesOnResize>();

		ObjectExtractor oe = new ObjectExtractor(file);
		Page pageOfResize = oe.extract(pageNumOfResizedFilter);

		ArrayList<RegexSearch> searchesToReRun = new ArrayList<RegexSearch>();

		Integer previousHeaderHeight = previousFilterArea.getScaledHeaderHeight();
		Integer currentHeaderHeight = areasToFilter.get(pageNumOfResizedFilter).getScaledHeaderHeight();

		Integer previousFooterHeight = previousFilterArea.getScaledFooterHeight();
		Integer currentFooterHeight = areasToFilter.get(pageNumOfResizedFilter).getScaledFooterHeight();

		if (previousHeaderHeight != currentHeaderHeight) {
			determineHeaderResizeImpact(previousHeaderHeight,currentHeaderHeight,file,
					pageOfResize,areasToFilter,currentRegexSearches,updatedSearches);
		}
        else if(previousFooterHeight!=currentFooterHeight){
			determineFooterResizeImpact(previousFooterHeight,currentFooterHeight,file,
					pageOfResize,areasToFilter,currentRegexSearches,updatedSearches);
		}

		return updatedSearches;
		*/
	}

	private static class UpdatesOnResize{
		RegexSearch updatedRegexSearch;         //The RegexSearch object that is being updated
		ArrayList<MatchingArea> areasAdded;     //New Areas discovered or areas modified upon resize event
		ArrayList<MatchingArea> areasRemoved;   //Previous Area dimensions for areas changed by resize event
		Boolean overlapsAnotherSearch; //Flag for when resize causes areas to be found overlapping a present query <--TODO: this variable is always false for now...need to update this once an overlap algorithm is written

		public UpdatesOnResize(RegexSearch regexSearch, ArrayList<MatchingArea> newAreasToAdd, ArrayList<MatchingArea> oldAreasToRemove,
							   Boolean resizeEventCausedAnOverlap){
			updatedRegexSearch = regexSearch;
			areasAdded = newAreasToAdd;
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
					   String includeRegexAfterTable, PDDocument document, FilteredArea areaToFilter) {
		
		this(regexBeforeTable,Boolean.valueOf(includeRegexBeforeTable),regexAfterTable,
			Boolean.valueOf(includeRegexAfterTable),document,areaToFilter);
		
	}

	public RegexSearch(String regexBeforeTable,Boolean includeRegexBeforeTable, String regexAfterTable,
					   Boolean includeRegexAfterTable, PDDocument document, FilteredArea filterArea) {
		_regexBeforeTable = Pattern.compile(regexBeforeTable);
		_regexAfterTable = Pattern.compile(regexAfterTable);

		_includeRegexBeforeTable = includeRegexBeforeTable;
		_includeRegexAfterTable = includeRegexAfterTable;

		_matchingAreas = detectMatchingAreas(document,filterArea);

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

	//TODO-Write up blurb for TableArea...
	private class TableArea {
		private Integer _pageNum; //Number of the page that the TableArea is drawn on
		private Rectangle _area; //Rectangular coordinates defining the boundaries of TableArea

		public TableArea(Integer pageNum, Rectangle area){
			_pageNum = pageNum;
			_area = area;
		}

		public Integer getPageNum(){return _pageNum;}
		public Rectangle getArea() {return _area;}
		public Integer getHeight() { return Math.round(_area.height);}
		public Integer getTop()    { return Math.round(_area.getTop());}
		public Integer getLeft()   { return Math.round(_area.getLeft());}
		public Integer getWidth()  { return Math.round(_area.width);}
	}



    /*
     * This class maps on a per-page basis the areas (plural) of the PDF document that fall between text matching the
     * user-provided regex (this allows for tables that span multiple pages to be considered a single entity).
     * The key is the page number that the areas first begin. The LinkedList of Rectangles allows for multiple
     * areas to be associated with a given match (as in the case of multiple pages)
     */
	private static class MatchingArea extends HashMap<Integer,LinkedList<TableArea>> {

		private Integer _startPageNum;
		private Integer _endPageNum;


		public MatchingArea(Integer startPageNum, Integer endPageNum){
			_startPageNum = startPageNum;
			_endPageNum = endPageNum;
		}
	}

	/*
	 * @param pageNumber The one-based index into the document
	 * @return ArrayList<Rectangle> The values stored in _matchingAreas for a given page	
	 */

	public ArrayList<Rectangle> getMatchingAreasForPage(Integer pageNumber){
		
        ArrayList<Rectangle> allMatchingAreas = new ArrayList<>();
		
		for( MatchingArea matchingArea : _matchingAreas) {
			for( int currentPageNumber : matchingArea.keySet()){
				if(currentPageNumber == pageNumber){
					for(TableArea tableArea : matchingArea.get(currentPageNumber)){
						allMatchingAreas.add(tableArea.getArea());
					}
				}
			}
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
		private Integer guiPageHeight;   //The height of the page AS IT APPEARS IN THE GUI
		private Float  scaleOfHeaderHeight;
		private Float  scaleOfFooterHeight;

		public FilteredArea(Integer heightOfHeader, Integer heightOfFooter, Integer guiHeightOfPage){
			guiPageHeight = guiHeightOfPage;

			scaleOfFooterHeight = ((float)heightOfFooter)/guiPageHeight;
			scaleOfHeaderHeight = ((float)heightOfHeader)/guiPageHeight;
	//		System.out.println("Height of header:" + heightOfHeader);
	//		System.out.println("Height of page in gui:" + guiPageHeight);
	//		System.out.println("Height of page in back-end:" + absolutePageHeight);
		}

		public Float getHeaderHeightScale(){ return scaleOfHeaderHeight;}
		public Float getFooterHeightScale(){ return scaleOfFooterHeight;}

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
	 * @param areasToFilter The header and footer sections of the document that are to be ignored.
	 * @return ArrayList<MatchingArea> A list of the sections of the document that occur between text 
	 * that matches the user-provided regex
	 */
	private ArrayList<MatchingArea> detectMatchingAreas(PDDocument document, FilteredArea areaToFilter) {
	  return detectMatchingAreas(document,areaToFilter,1,null, document.getNumberOfPages(),null);
	};

	//TODO: Update documentation to reflect the new inputs...
	/*
	 * @param pageNumStart The first page of the document to begin detection
	 * @param pageStartY The y position on pageNumStart to begin detection
	 * @param pageNumEnd The last page of the document to perform detecion
	 * @param pageEndY The ending y position on pageNumEnd where detection should stop
	 */
	private ArrayList<MatchingArea> detectMatchingAreas(PDDocument document, FilteredArea areaToFilter, Integer pageNumStart, Integer pageStartY,
	                                                    Integer pageNumEnd, Integer pageEndY) {

	ObjectExtractor oe = new ObjectExtractor(document);

	LinkedList<DetectionData> potentialMatches = new LinkedList<>();
	potentialMatches.add(new DetectionData());

	for(Integer currentPage=pageNumStart;currentPage<=pageNumEnd;currentPage++) {
		/*
		 * Convert PDF page to text
		 */
		Page page = oe.extract(currentPage);

		//FilteredArea filterArea  = ( (areasToFilter!=null) && areasToFilter.containsKey(page.getPageNumber())) ?
		//		              areasToFilter.get(page.getPageNumber()): new FilteredArea(0,0, (int) page.getHeight(), (int) page.getHeight());



		Integer top = INIT;
		if((currentPage==pageNumStart) && (pageStartY!=null)){
			top = pageStartY;
		}
		else if(areaToFilter!=null){
			top = Math.round(areaToFilter.getHeaderHeightScale()* page.height);
		}

		Integer height = Math.round(page.height);
		if((currentPage==pageNumEnd) && (pageEndY!=null)) {
			height = pageEndY;
		}
		else if(areaToFilter!=null){
			height = Math.round(page.height-areaToFilter.getHeaderHeightScale()*page.height
					-areaToFilter.getFooterHeightScale()* page.height);
		}
		height -= top;

		ArrayList<TextElement> pageTextElements = (ArrayList<TextElement>) page.getText(
				new Rectangle(top,0, page.width, height));

		StringBuilder pageAsText = new StringBuilder();

		for(TextElement element : pageTextElements ) {
			pageAsText.append(element.getText());
		}

		System.out.println("Area to parse as string:");
		System.out.println(pageAsText.toString());

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

	return calculateMatchingAreas(potentialMatches,document,areaToFilter);

}

	/*
	 * calculateMatchingAreas: Determines the rectangular coordinates of the document sections
	 *                         matching the user-specified regex(_regexBeforeTable,_regexAfterTable)
	 * 
	 * @param foundMatches A list of DetectionData values
	 * @return ArrayList<MatchingArea> A Hashmap 
	 */
	private ArrayList<MatchingArea> calculateMatchingAreas(LinkedList<DetectionData> foundMatches, PDDocument document,
														   FilteredArea areaToFilter) {
		
		ArrayList<MatchingArea> matchingAreas = new ArrayList<>();
		
		ObjectExtractor oe = new ObjectExtractor(document);


		while(!foundMatches.isEmpty()) {

			DetectionData foundTable = foundMatches.pop();

            if(foundTable._pageBeginMatch.get() == foundTable._pageEndMatch.get()) {
            
            	float width = oe.extract(foundTable._pageBeginMatch.get()).width;
            	float height = foundTable._pageEndCoord.y-foundTable._pageBeginCoord.y;
            	
            	LinkedList<TableArea> tableArea = new LinkedList<>();
            	tableArea.add(new TableArea(foundTable._pageBeginMatch.get(),new Rectangle(foundTable._pageBeginCoord.y,0,width,height)));
            	
            	MatchingArea matchingArea = new MatchingArea(foundTable._pageBeginMatch.get(), foundTable._pageEndMatch.get());
            	matchingArea.put(foundTable._pageBeginMatch.get(), tableArea);
            
            	matchingAreas.add(matchingArea);
            
			}
            else {
            	
            	MatchingArea matchingArea = new MatchingArea(foundTable._pageBeginMatch.get(),foundTable._pageEndMatch.get());
            	/*
            	 * Create sub-area for table from directly below the pattern-before-table content to the end of the page
            	 */
            	Page currentPage =  oe.extract(foundTable._pageBeginMatch.get());
            	LinkedList<TableArea> tableSubArea = new LinkedList<>();

            	Float footer_height = (areaToFilter==null) ? (float)0:
						areaToFilter.getFooterHeightScale()*currentPage.height;

            	Float height = currentPage.height-foundTable._pageBeginCoord.y-footer_height;

            	tableSubArea.add( new TableArea(currentPage.getPageNumber(), new Rectangle(foundTable._pageBeginCoord.y,0,currentPage.width,
            			                        height))); //Note: limitation of this approach is that the entire width of the page is used...could be problematic for multi-column data
            	matchingArea.put(currentPage.getPageNumber(), tableSubArea);
            	
            	/*
            	 * Create sub-areas for table that span the entire page
            	 */
            	for (Integer iter=currentPage.getPageNumber()+1; iter<foundTable._pageEndMatch.get(); iter++) {
            		currentPage = oe.extract(iter);
            		Integer subAreaTop = (areaToFilter!=null) ? Math.round(areaToFilter.getHeaderHeightScale()*currentPage.height) :
							(int) Math.round(0.5*(currentPage.getTextBounds().getMinY()));

            		Float subAreaHeight = currentPage.height-subAreaTop;

            		if(areaToFilter!=null){
						subAreaHeight -=areaToFilter.getFooterHeightScale()*currentPage.height;
					}
					else{
            			subAreaHeight -= (float)((0.5) * currentPage.getTextBounds().getBottom());
					}


            		tableSubArea = new LinkedList<>();
            		tableSubArea.add(new TableArea(currentPage.getPageNumber(),
							new Rectangle(subAreaTop,0,currentPage.width,
									currentPage.height-(subAreaTop+areaToFilter.getFooterHeightScale()))));
            		matchingArea.put(currentPage.getPageNumber(), tableSubArea);
            	}
                
            	/*
            	 * Create sub-areas for table from the top of the page to directly before the pattern-after-table content 
            	 */
            	
            	currentPage = oe.extract(foundTable._pageEndMatch.get());
                tableSubArea = new LinkedList<>();

				Integer top = (areaToFilter!=null) ? Math.round(areaToFilter.getHeaderHeightScale()*currentPage.height) :
						(int) Math.round(0.5*(currentPage.getTextBounds().getMinY()));

				System.out.println("Current Page #:"+currentPage.getPageNumber());
				System.out.println("Top:"+top);
                tableSubArea.add(new TableArea(currentPage.getPageNumber(), new Rectangle(top,0,currentPage.width,foundTable._pageEndCoord.y-top)));

                matchingArea.put(currentPage.getPageNumber(), tableSubArea);
                matchingAreas.add(matchingArea);
            }
		}

		return matchingAreas;
	}
}
