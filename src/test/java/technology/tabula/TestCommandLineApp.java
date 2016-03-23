package technology.tabula;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class TestCommandLineApp {

	private String csvFromCommandLineArgs(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);

		StringBuilder stringBuilder = new StringBuilder();
		new CommandLineApp(stringBuilder).extractTables(cmd);

		return stringBuilder.toString();
	}

	@Test
	public void testExtractSpreadsheetWithArea() throws ParseException, IOException {

		String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
		
		assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[] {
				"src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
				"-p", "1", "-a",
				"150.56,58.9,654.7,536.12", "-f",
				"CSV"
		}));
	}

	@Test
	public void testGuessOption() throws ParseException, IOException {
		String expectedCsvNoGuessing = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/TestCommandLineApp_testGuessOption_no_guessing.csv");
		assertEquals(expectedCsvNoGuessing, this.csvFromCommandLineArgs(new String[] {
				"src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-eu/eu-001.pdf",
				"-p", "1",
				"-f", "CSV"
		}));

		String expectedCsvWithGuessing = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/TestCommandLineApp_testGuessOption_with_guessing.csv");
		assertEquals(expectedCsvWithGuessing, this.csvFromCommandLineArgs(new String[] {
				"src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-eu/eu-001.pdf",
				"-p", "1",
				"-f", "CSV",
				"-g"
		}));
	}

}
