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

	
	private static RegexSearch _REGEXSEARCH;
	
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

	/**
	 * Test if RegexSearch class will find a single instance of a table on a single page document
	 */
	@Test
	public void testPatternMatching() {
			
		try {
			//Upload 1 page of PDF containing a single table
			
	//		PDDocument document = PDDocument.load(new File("src/test/resources/technology/tabula/eu-002.pdf"));
			
			Page data = UtilsForTesting.getPage("src/test/resources/technology/tabula/eu-002.pdf", 1);
			
			RegexSearch regexSearch = new RegexSearch("Table [0-9]","Table [0-9]");

			
			//Extract text
			String output="";
			for( TextElement text : data.getText()) {
			   output+=text.getText();
			}
			System.out.println(output);
			
			
			String expectedTableContent = "";
			
			assertTrue(expectedTableContent.equals(data.getText(s)));
			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Error in test case");
		}
		
		fail("Test not fully implemented yet");
		
		
	}

}
