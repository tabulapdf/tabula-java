package technology.tabula;
import static org.junit.Assert.*;

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
		_REGEXSEARCH = new RegexSearch();
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
	public void testPatternMatching() {
			
		try {
			//Upload 'simple' PDF
			Page data = UtilsForTesting.getPage("src/test/resources/technology/tabula/eu-002.pdf", 1);
			
			_REGEXSEARCH.detect(data, new String[] {"Table [0-9]","Table [0-9]"}); //should the user be able to specify the same regex for the beginning and end?? <--forces serial behavior I do believe
			
			//Extract text
			String output="";
			for( TextElement text : data.getTexts()) {
			   output+=text.getText();
			}
			System.out.println(output);
			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		fail("Not yet implemented");
	}

}
