package technology.tabula;

import static org.junit.Assert.*;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class TestCommandLineApp {

	@Test
	public void testExtractSpreadsheetWithArea() throws ParseException, IOException {

		String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");

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
		
		assertEquals(expectedCsv, stringBuilder.toString());

	}
	

}
