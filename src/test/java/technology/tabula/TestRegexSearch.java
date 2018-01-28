package technology.tabula;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import technology.tabula.detectors.RegexSearch;

/*
 * TestRegexSearch
 * 
 *    This class makes up all of the back-end unit tests performed on the RegexSearch class.
 *    
 *    TODO: Large blurb about this
 *    
 *    10/27/2017 REM; created.
 */

public class TestRegexSearch {


	private static final int CLIENT_CODE_STACK_INDEX;

	//Ripped this  out of StackOverflow:
	// https://stackoverflow.com/questions/442747/getting-the-name-of-the-currently-executing-method/8592871#8592871
	// with small change (moved increment to after the if statement) to facilitate an easier use of status_report
	static {
		// Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
		int i = 0;
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			if (ste.getClassName().equals(TestRegexSearch.class.getName())) {
				break;
			}
			i++;
		}
		CLIENT_CODE_STACK_INDEX = i;
	}


	public static void statusReport(String caller,String expectedContent, String extractedContent,String messageOnFail){
		System.out.println("----------------------------");
		System.out.println("Status Report for "+ caller);
		System.out.println("Expected Table Content:");
		System.out.println(expectedContent);
		System.out.println("Extracted Table Content:");
		System.out.println(extractedContent);
		assertTrue(messageOnFail,expectedContent.equals(extractedContent));
		System.out.println();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	/*
	 * Test if table spanning multiple pages is correctly detected
	 */
	public void testMultiPageTableDetectMatchingRegex() {
		
		String docName = "src/test/resources/technology/tabula/sydney_disclosure_contract.pdf";
		File multiPageTable = new File(docName);
		
		
		try {
				
			Integer numDataPages = 2;
			
			ArrayList<Page> dataPages = new ArrayList<Page>();
			
			for(Integer iter=1; iter<=numDataPages; iter++) {
				dataPages.add(UtilsForTesting.getPage(docName, iter));
			}
			
			RegexSearch regexSearch = new RegexSearch("9\\.","false","10\\.","false",PDDocument.load(multiPageTable));
			
			//TODO: The current multi-page regex capabilities WILL NOT FILTER OUT THE FOOTER--this needs to be corrected!! This test simply verifies the current program behavior
			//to facilitate future regression testing
			String expectedTableContent = "tendering and a summary of the criteria against which the various "+
					                      "tenders were assessed: Open Tender  Tender evaluation criteria "+
					                      "included: - The schedule of prices - Compliance with technical "+
					                      "specifications/Technical assessment - Operational Plan including "+
					                      "maintenance procedures  1  - Transition in/out plans - Demonstrated "+
					                      "experience in works and services of a similar nature and quality - "+
					                      "Adequate resources and personnel to fulfil requirements of the contract "+
					                      "including subcontractors - Data Management Procedures and reporting capabilities - "+
					                      "Proposed installation plan including Pedestrian & Traffic Management - "+
					                      "Environmental Management  - Work Health & Safety - Financial and commercial trading integrity/insurances  ";
			
			String extractedTableContent = "";
			for(Integer iter=0; iter<numDataPages; iter++) {
				for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(iter+1)) {
					for(TextElement element : dataPages.get(iter).getText(tableArea)) {
						extractedTableContent += element.getText();
					}
				}
			}
            statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
					expectedTableContent,extractedTableContent,"Failure in multi-page detection");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
	}
	
	@Test
	/**
	 * Test if RegexSearch class will NOT generate false positives 
	 * (find a table area when one does not exist for the regex supplied)
	 */

	public void testSimpleTableDetectNonMatchingRegex() {

		
		try {
			//Upload 1 page of PDF containing a single table	
			
			String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";
			
			File singleTable = new File(basicDocName);


	        Page data = UtilsForTesting.getPage(basicDocName, 1);
			
	        PDDocument docInQuestion = PDDocument.load(singleTable);
			RegexSearch regexSearch = new RegexSearch("WRONG","false","WRONG","false",docInQuestion);

			String expectedTableContent = "";
			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
				extractedTableContent += data.getText(tableArea);
			}

			statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
					expectedTableContent,extractedTableContent,"Content falsely detected");
		    //assertTrue(extractedTableContent.isEmpty() );
			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
	}
	
	/**
	 * Test if RegexSearch class will find a single instance of a table on a single page document when given the correct regex
	 */
	@Test
	public void testSimpleTableDetectMatchingRegex() {

		try {
			
			//Upload 1 page of PDF containing a single table
			
			String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";
			
			File singleTable = new File(basicDocName);

	        Page data = UtilsForTesting.getPage(basicDocName, 1);

	        PDDocument docInQuestion = PDDocument.load(singleTable);

			RegexSearch regexSearch = new RegexSearch("Table [0-9]","false","Table [0-9]","false",docInQuestion);
			
			String expectedTableContent = "Correlations between the extent of participation of pupils in project activities and the" + 
					" perceived  impacts on pupils (Pearsons correlation coefficient*)   Involvement of pupils in  Preperation" + 
					" and Production of Presentation and planing materials evaluation Knowledge and awareness of different" + 
					" cultures 0,2885 0,3974 0,3904 Foreign language competence 0,3057 0,4184 0,3899 Social skills and" + 
					" abilities 0,3416 0,3369 0,4303 Acquaintance of special knowledge 0,2569 0,2909 0,3557 Self competence" + 
					" 0,3791 0,3320 0,4617 * Significance p = 0,000  ";
			
			expectedTableContent.trim();
			
			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
				for(TextElement element : data.getText(tableArea)) {
					extractedTableContent += element.getText();
				}
				extractedTableContent.trim();
			}

			statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
					expectedTableContent,extractedTableContent,"Error in simple table detection");

			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
	}

/**
 * Test if RegexSearch class will include the line containing patternBefore when requested 
 */
@Test
public void testIncludePatternAfterOption() {
	
	PDDocument docInQuestion = new PDDocument();
	
	try {
		
		//Upload 1 page of PDF containing a single table
		
		String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";
		
		File singleTable = new File(basicDocName);
		
        Page data = UtilsForTesting.getPage(basicDocName, 1);
        

		RegexSearch regexSearch = new RegexSearch("Table [0-9]","false","Table [0-9]","true",PDDocument.load(singleTable));
		
		String expectedTableContent = "Correlations between the extent of participation of pupils in project activities and the" + 
				" perceived  impacts on pupils (Pearsons correlation coefficient*)   Involvement of pupils in  Preperation" + 
				" and Production of Presentation and planing materials evaluation Knowledge and awareness of different" + 
				" cultures 0,2885 0,3974 0,3904 Foreign language competence 0,3057 0,4184 0,3899 Social skills and" + 
				" abilities 0,3416 0,3369 0,4303 Acquaintance of special knowledge 0,2569 0,2909 0,3557 Self competence" + 
				" 0,3791 0,3320 0,4617 * Significance p = 0,000  Table 6";
		
		expectedTableContent = expectedTableContent.trim();
		
		String extractedTableContent = "";
		for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
			
			for(TextElement element : data.getText(tableArea)) {
				extractedTableContent += element.getText();
			}
			extractedTableContent = extractedTableContent.trim();
		}

		statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
				expectedTableContent,extractedTableContent,"Error in the inclusive test");

		
	} 
	catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		fail("Error in test case");
	}
	finally {
		if(docInQuestion!=null) {
			try {
				docInQuestion.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

/**
 * Test if RegexSearch class will include the line containing patternAfter when requested 
 */
@Test
public void testIncludePatternBeforeOption() {
	
	PDDocument docInQuestion = new PDDocument();
	
	try {
		
		//Upload 1 page of PDF containing a single table
		
		String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";
		
		File singleTable = new File(basicDocName);
		
        Page data = UtilsForTesting.getPage(basicDocName, 1);
        

		RegexSearch regexSearch = new RegexSearch("Table [0-9]","true","Table [0-9]","false",PDDocument.load(singleTable));
		
		String expectedTableContent = "Table 5 Correlations between the extent of participation of pupils in project activities and the" + 
				" perceived  impacts on pupils (Pearsons correlation coefficient*)   Involvement of pupils in  Preperation" + 
				" and Production of Presentation and planing materials evaluation Knowledge and awareness of different" + 
				" cultures 0,2885 0,3974 0,3904 Foreign language competence 0,3057 0,4184 0,3899 Social skills and" + 
				" abilities 0,3416 0,3369 0,4303 Acquaintance of special knowledge 0,2569 0,2909 0,3557 Self competence" + 
				" 0,3791 0,3320 0,4617 * Significance p = 0,000  ";
		
		expectedTableContent = expectedTableContent.trim();
		
		
		String extractedTableContent = "";
		for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
			
			
			for(TextElement element : data.getText(tableArea)) {
				extractedTableContent += element.getText();
			}
			extractedTableContent = extractedTableContent.trim();
		}

		statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
				expectedTableContent,extractedTableContent,"PatternBefore was not included in the extracted content");


		
	} 
	catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		fail("Error in test case");
	}
	finally {
		if(docInQuestion!=null) {
			try {
				docInQuestion.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

/**
 * Test if RegexSearch class will include the line containing patternBefore
 * and patternAfter when requested for a simple match (1 match area detected)
 */
@Test
public void testIncludePatternBeforeAndPatternAfterOption() {
	
	PDDocument docInQuestion = new PDDocument();
	
	try {
		
		//Upload 1 page of PDF containing a single table
		
		String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";
		
		File singleTable = new File(basicDocName);
		
        Page data = UtilsForTesting.getPage(basicDocName, 1);
        

		RegexSearch regexSearch = new RegexSearch("Table [0-9]","true","Table [0-9]","true",PDDocument.load(singleTable));
		
		String expectedTableContent = "Table 5 Correlations between the extent of participation of pupils in project activities and the" + 
				" perceived  impacts on pupils (Pearsons correlation coefficient*)   Involvement of pupils in  Preperation" + 
				" and Production of Presentation and planing materials evaluation Knowledge and awareness of different" + 
				" cultures 0,2885 0,3974 0,3904 Foreign language competence 0,3057 0,4184 0,3899 Social skills and" + 
				" abilities 0,3416 0,3369 0,4303 Acquaintance of special knowledge 0,2569 0,2909 0,3557 Self competence" + 
				" 0,3791 0,3320 0,4617 * Significance p = 0,000  Table 6";
		
		expectedTableContent = expectedTableContent.trim();
		
		
		String extractedTableContent = "";
		for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
			
			
			for(TextElement element : data.getText(tableArea)) {
				extractedTableContent += element.getText();
			}
			extractedTableContent = extractedTableContent.trim();
		}

		statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
				     expectedTableContent,extractedTableContent,"Error with inclusion of patterns");
	} 
	catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		fail("Error in test case");
	}
	finally {
		if(docInQuestion!=null) {
			try {
				docInQuestion.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

	/**
	 * Test if RegexSearch class will include the line containing patternBefore
	 * and patternAfter when requested
	 */
	@Test
	public void testIncludePatternBeforeAndPatternAfterForMultipleMatches() {

		PDDocument docInQuestion = new PDDocument();

		try {

			//Upload 1 page of PDF containing a single table

			String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";

			File singleTable = new File(basicDocName);

			Page data = UtilsForTesting.getPage(basicDocName, 1);


			RegexSearch regexSearch = new RegexSearch("Knowledge","true","Social","true",PDDocument.load(singleTable));

			String expectedTableContent = "Knowledge and awareness of different " +
					"cultures 0,2885 0,3974 0,3904 Foreign language competence 0,3057 0,4184 0,3899 Social skills and " +
					"abilities 0,3416 0,3369 0,4303"+
			        "Knowledge and awareness of different cultures -0,1651 -0,2742 -0,1618 Foreign language competence "+
			        "-0,0857 -0,1804 -0,1337 Social skills and abilities -0,1237 -0,2328 -0,1473Knowledge/appreciation of "+
			        "school system and -0,1505 -0,1636 -0,1349 education in the partner countries Foreign language competence "+
			        "-0,0545 -0,0997 -0,0519 Social skills and personal commitment -0,2558 -0,2235 -0,1302";

			expectedTableContent = expectedTableContent.trim();


			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {

				for(TextElement element : data.getText(tableArea)) {
					extractedTableContent += element.getText();
				}
				extractedTableContent = extractedTableContent.trim();
			}

			statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
					expectedTableContent,extractedTableContent,"Error with pattern inclusion");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
		finally {
			if(docInQuestion!=null) {
				try {
					docInQuestion.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}




	/**
	 * Test if RegexSearch class will ignore tables which do not have a defined beginning.
	 *   Example:
	 *     Pattern Before: x
	 *     Pattern After: y
	 *     Content: x
	 *                1
	 *                2
	 *              y
	 *              b
	 *                3
	 *                4
	 *              y
	 *
	 *      (Only 1 table area should be detected)
	 */
	@Test
	public void testMatchWithNoBeginPatternFound() {

		PDDocument docInQuestion = new PDDocument();

		try {

			//Upload 1 page of PDF containing a single table

			String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";

			File singleTable = new File(basicDocName);

			Page data = UtilsForTesting.getPage(basicDocName, 1);


			RegexSearch regexSearch = new RegexSearch("Correlations","true","Knowledge","true",PDDocument.load(singleTable));


			String expectedTableContent = "Correlations between the extent of participation of pupils in project activities " +
					                      "and the perceived  impacts on pupils (Pearsons correlation coefficient*)   "+
			                              "Involvement of pupils in  Preperation and Production of Presentation and planing " +
					                      "materials evaluation Knowledge and awareness of different cultures 0,2885 0,3974 0,3904"+
					                      "Correlations between the difficulties encounters and the perceived impacts on pupils, "+
			                              "teachers and the school as a whole (Pearsons correlation coefficient*)   " +
					                      "Difficulties encountered with respect to  Lack of interest/ Lack of interest "+
					                      "of Lack of interest of  acceptance from pupils parents Impacts on participating "+
					                      "pupils colleagues Knowledge and awareness of different cultures -0,1651 -0,2742 -0,1618";

			expectedTableContent = expectedTableContent.trim();


			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {


				for(TextElement element : data.getText(tableArea)) {
					extractedTableContent += element.getText();
				}
				extractedTableContent = extractedTableContent.trim();
			}

			statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
					expectedTableContent,extractedTableContent,"Error in table detection");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
		finally {
			if(docInQuestion!=null) {
				try {
					docInQuestion.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Test if RegexSearch detection method will ignore the text located in the header-specified area.
	 */
	@Test
	public void testHeaderFilteringCapability() {

		PDDocument docInQuestion = new PDDocument();

		try {

			//Upload 1 page of PDF containing a single table

			String basicDocName = "src/test/resources/technology/tabula/eu-002.pdf";

			File singleTable = new File(basicDocName);

			Page data = UtilsForTesting.getPage(basicDocName, 1);


			RegexSearch regexSearch = new RegexSearch("Table 5","false","Table 6","false",
					PDDocument.load(singleTable),new HashMap<Integer, Integer>(){{put(1,83);
			                                                                      put(2,0);}});


			String expectedTableContent = "";

			expectedTableContent = expectedTableContent.trim();


			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {


				for(TextElement element : data.getText(tableArea)) {
					extractedTableContent += element.getText();
				}
				extractedTableContent = extractedTableContent.trim();
			}

			statusReport(Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName(),
					expectedTableContent,extractedTableContent,"Error in header filtering capability");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
		finally {
			if(docInQuestion!=null) {
				try {
					docInQuestion.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}