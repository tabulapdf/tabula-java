package technology.tabula;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
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
 *    TODO: Small blurb about this
 *    
 *    TODO: Large blurb about this
 *    
 *    10/27/2017 REM; created.
 */

public class TestRegexSearch {

	
	
	
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

//	@Test
	/**
	 * Test if RegexSearch class will NOT find a single instance of a table on a single page document when given incorrect regex
	 */
/*
	public void testSimpleTableDetectNonMatchingRegex() {
		try {
			//Upload 1 page of PDF containing a single table
			
			
			
			File singleTable = new File("src/test/resources/technology/tabula/eu-002.pdf");
			
	        Page data = UtilsForTesting.getPage("src/test/resources/technology/tabula/eu-002.pdf", 1);
			
			RegexSearch regexSearch = new RegexSearch("WRONG","WRONG",PDDocument.load(singleTable));
	
			
			String expectedTableContent = "";
			
			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
				extractedTableContent += data.getText(tableArea);
			}
			
			System.out.println(extractedTableContent);
			
//			assertTrue(expectedTableContent.equals(extractedTableContent));
			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
		
		fail("Test not fully implemented yet");
		
		
	}
*/	
	/**
	 * Test if RegexSearch class will find a single instance of a table on a single page document when given the correct regex
	 */
	@Test
	public void testSimpleTableDetectMatchingRegex() {
			
		try {
			//Upload 1 page of PDF containing a single table
			
			File singleTable = new File("src/test/resources/technology/tabula/eu-002.pdf");
			
	        Page data = UtilsForTesting.getPage("src/test/resources/technology/tabula/eu-002.pdf", 1);
			
			RegexSearch regexSearch = new RegexSearch("Table [0-9]","Table [0-9]",PDDocument.load(singleTable));
	
			
			String expectedTableContent = "Correlations between the extent of participation of pupils in project activities and the" + 
					" perceived  impacts on pupils (Pearsons correlation coefficient*)   Involvement of pupils in  Preperation" + 
					" and Production of Presentation and planing materials evaluation Knowledge and awareness of different" + 
					" cultures 0,2885 0,3974 0,3904 Foreign language competence 0,3057 0,4184 0,3899 Social skills and" + 
					" abilities 0,3416 0,3369 0,4303 Acquaintance of special knowledge 0,2569 0,2909 0,3557 Self competence" + 
					" 0,3791 0,3320 0,4617 * Significance p = 0,000 ";
			
			String extractedTableContent = "";
			for(Rectangle tableArea : regexSearch.getMatchingAreasForPage(1)) {
				
				System.out.println(tableArea.toString());
				
				for(TextElement element : data.getText(tableArea)) {
					extractedTableContent += element.getText();
				}
			}
			
			System.out.println(extractedTableContent);
			
//			assertTrue(expectedTableContent.equals(extractedTableContent));
			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
		
		fail("Test not fully implemented yet");
		
		
	}

}
