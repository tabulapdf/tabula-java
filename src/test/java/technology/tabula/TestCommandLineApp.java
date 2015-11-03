package technology.tabula;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class TestCommandLineApp {

	@Test
	public void testExtractSpreadsheetWithArea() throws ParseException {

        String expectedCsv = "HARVEST,VARIATION,,,\r\n\"11/12\r(a)\",12/13,Percentage,Absolute,\r\n\"\",\"May/2013     \r(b)\",\"Jun/2013      \r(c)\",(c/a),\r\n\"1.393,4\",\"886,7\",\"894,9\",\"(35,8)\",\"( 498,5)\"\r\n\"93,9\",\"100,6\",\"100,2\",\"6,7\",\"6,3\"\r\n\"82,1\",\"86,3\",\"86,2\",\"5,0\",\"4,1\"\r\n\"11,8\",\"14,3\",\"14,0\",\"18,6\",\"2,2\"\r\n\"2.426,7\",\"2.389,7\",\"2.396,0\",\"(1,3)\",\"( 30,7)\"\r\n\"3.262,1\",\"2.952,7\",\"3.026,9\",\"(7,2)\",\"( 235,2)\"\r\n\"1.241,4\",\"1.122,6\",\"1.122,9\",\"(9,5)\",\"( 118,5)\"\r\n\"1.394,6\",\"1.275,4\",\"1.271,7\",\"(8,8)\",\"( 122,9)\"\r\n\"626,1\",\"554,7\",\"632,3\",\"1,0\",\"6,3\",\"49,5\"\r\n\"74,5\",\"60,4\",\"68,9\",\"(7,5)\",\"( 5,6)\"\r\n\"128,2\",\"87,5\",\"87,4\",\"(31,8)\",\"( 40,8)\"\r\n\"15.178,1\",\"15.686,2\",\"15.817,4\",\"4,2\",\"639,3\"\r\n\"7.558,5\",\"6.879,2\",\"6.864,7\",\"(9,2)\",\"( 693,8)\"\r\n\"7.619,6\",\"8.807,0\",\"8.952,7\",\"17,5\",\"1.333,1\"\r\n\"25.042,2\",\"27.715,2\",\"27.715,5\",\"10,7\",\"2.673,3\"\r\n\"786,9\",\"836,4\",\"836,4\",\"6,3\",\r\n\"48.386,0\",\"50.715,4\",\"50.943,6\",\"5,3\",\r\n\"153,0\",\"168,7\",\"168,7\",\"10,3\",\"15,7\"\r\n\"42,4\",\"43,8\",\"43,8\",\"3,3\",\"1,4\"\r\n\"2,3\",\"2,3\",\"2,3\",-,-\r\n\"88,4\",\"102,8\",\"102,8\",\"16,3\",\"14,4\"\r\n\"2.166,2\",\"1.895,4\",\"1.895,4\",\"(12,5)\",\"( 270,8)\"\r\n\"46,9\",\"48,0\",\"48,0\",\"2,3\",\r\n\"2.499,2\",\"2.261,0\",\"2.261,0\",\"(9,5)\",\r\n\"50.885,2\",\"5 2.976,4\",\"5 3.204,6\",\"4,6\",\r\n";

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser
				.parse(CommandLineApp.buildOptions(),
						new String[] {
								"src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
								"-p", "1", "-a",
								"150.56,58.9,654.7,536.12", "-f",
								"CSV" });
		
		StringBuilder stringBuilder = new StringBuilder();
		new CommandLineApp(stringBuilder).extractTables(cmd);
		
		System.out.println("StringBuilder:");
		System.out.println(stringBuilder);
		//assertEquals(expectedCsv, stringBuilder);

	}
	
	//@Test
	public void testExtractSpreadsheet() throws ParseException {

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser
				.parse(CommandLineApp.buildOptions(),
						new String[] {
								"src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
								"-p", "1", "-f",
								"CSV" });
		
		StringBuilder stringBuilder = new StringBuilder();
		new CommandLineApp(stringBuilder).extractTables(cmd);

	}

}
