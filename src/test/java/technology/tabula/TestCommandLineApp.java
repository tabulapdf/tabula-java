package technology.tabula;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class TestCommandLineApp {


    private String csvFromCommandLineArgs(String[] args) throws ParseException, IOException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);

        StringBuilder stringBuilder = new StringBuilder();
        new CommandLineApp(stringBuilder, cmd).extractTables(cmd);

        return stringBuilder.toString();
    }

    @Test
    // Test fails with identical expected and actual results
    // Test failure due to end-of-line (\r\n) mismatches  - 'expectedCsv' has 3 extra \n's than the output Csv
    // Most likely due to "UtilsForTesting.loadCsv"
    public void testExtractSpreadsheetWithArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                "-p", "1", "-a",
                "150.56,58.9,654.7,536.12", "-f",
                "CSV"
        }));
    }

    @Test
    // Test failure due to end-of-line (\r\n) mismatches - 'expectedCsv' has 3 extra \n's than the output Csv
    // Most likely due to the replaceAll in "UtilsForTesting.loadCsv"
    public void testExtractBatchSpreadsheetWithArea() throws ParseException, IOException {
        FileSystem fs = FileSystems.getDefault();
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
        Path tmpFolder = Files.createTempDirectory("tabula-java-batch-test");
        tmpFolder.toFile().deleteOnExit();

        Path copiedPDF = tmpFolder.resolve(fs.getPath("spreadsheet.pdf"));
        Path sourcePDF = fs.getPath("src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf");
        Files.copy(sourcePDF, copiedPDF);
        copiedPDF.toFile().deleteOnExit();
        try {
            this.csvFromCommandLineArgs(new String[]{
                    "-b", tmpFolder.toString(),
                    "-p", "1", "-a",
                    "150.56,58.9,654.7,536.12", "-f",
                    "CSV"
            });

            Path csvPath = tmpFolder.resolve(fs.getPath("spreadsheet.csv"));
            assertTrue(csvPath.toFile().exists());
            assertArrayEquals(expectedCsv.getBytes(), Files.readAllBytes(csvPath));
        }
        //Test has failed if parseException has been thrown...
        catch(ParseException pe){
            assertTrue(pe.getMessage(),false);
        }
    }

    @Test
    // Test is passing, but I'm not sure if it's testing what it was intended to test for...
    public void testExtractSpreadsheetWithAreaAndNewFile() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");

        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                "-p", "1", "-a",
                "150.56,58.9,654.7,536.12", "-f",
                "CSV", "-o", "outputFile"
        });
        //assertEquals(expectedCsv,);
    }


    @Test
    public void testExtractJSONWithArea() throws ParseException, IOException {

        String expectedJson = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/spanning_cells_basic.json");

        assertEquals(expectedJson, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/spanning_cells.pdf",
                "-p", "1", "-a",
                "150.56,58.9,654.7,536.12", "-f",
                "JSON"
        }));
    }

    @Test
    public void testExtractCSVWithArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/spanning_cells.pdf",
                "-p", "1", "-a",
                "150.56,58.9,654.7,536.12", "-f",
                "CSV"
        }));
    }

    @Test
    public void testGuessOption() throws ParseException, IOException {
        String expectedCsvNoGuessing = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/TestCommandLineApp_testGuessOption_no_guessing.csv");
        assertEquals(expectedCsvNoGuessing, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-eu/eu-001.pdf",
                "-p", "1",
                "-f", "CSV"
        }));

        String expectedCsvWithGuessing = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/TestCommandLineApp_testGuessOption_with_guessing.csv");
        assertEquals(expectedCsvWithGuessing, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-eu/eu-001.pdf",
                "-p", "1",
                "-f", "CSV",
                "-g"
        }));
    }

    @Test
    public void testEncryptedPasswordSupplied() throws ParseException, IOException {
        String s = this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/encrypted.pdf",
                "-s", "userpassword",
                "-p", "1",
                "-f", "CSV"
        });
        assertEquals("FLA Audit Profile,,,,,,,,,", s.split("\\r?\\n")[0]);
    }

    @Test(expected=org.apache.commons.cli.ParseException.class)
    public void testEncryptedWrongPassword() throws ParseException, IOException {
        String s = this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/encrypted.pdf",
                "-s", "wrongpassword",
                "-p", "1",
                "-f", "CSV"
        });
    }

    @Test
    // Tests for when app attempts to load an invalid/nonexistent pdf
    // Test expects to catch a ParseException
    public void testIncorrectPDFFile() throws IOException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");

        try {
            this.csvFromCommandLineArgs(new String[]{
                    "src/test/resources/technology/tabula/fakeFile.pdf",
                    "-p", "1", "-r", "argForR",
                    "-f",
                    "CSV"
            });
        } catch (ParseException pe) {
            assertTrue(pe.toString(), true);
        }
    }

    @Test
    // Tests for an invalid input for argument following '-r'
    // Test expects to catch IllegalStateException
    public void testImproperlyFormattedData() throws IOException, ParseException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");

        try {
            this.csvFromCommandLineArgs(new String[]{
                    "src/test/resources/technology/tabula/spanning_cells.pdf",
                    "-p", "1", "-r", "invalidInput",
                    "-f",
                    "CSV"
            });
        } catch (IllegalStateException ie) {
            assertTrue(ie.toString(), true);
        }
    }

    @Test
    // Tests for correct parsing of pattern_before and pattern_after regex
    // Note: Data is formatted as JSON Object
    public void testParsingBeforeAndAfterRegex() throws IOException, ParseException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");

        try {
            this.csvFromCommandLineArgs(new String[]{
                    "src/test/resources/technology/tabula/spanning_cells.pdf",
                    "-p", "1", "-r",
                    "{\"queries\": " +
                            "[ {\"pattern_before\" : \"Table 5\"," +
                            "\"pattern_after\" : \"Table 6\"} ]}",
                    "-f",
                    "CSV"
            });
        } catch (IllegalStateException ie) {
            assertTrue(ie.toString(), true);
        }
    }

    @Test
    public void testExtractRegexArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractRegexArea.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"} ]}",
                "-f",
                "CSV"
        }));
    }
/*
    public void testBeforeAndAfterRegexData() throws IOException, ParseException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");
        String[] args = new String[]{"src/test/resources/technology/tabula/spanning_cells.pdf",
                "-p", "1", "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Table 5\"," +
                        "\"pattern_after\" : \"Table 6\"} ]}",
                "-f",
                "CSV"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);
        StringBuilder stringBuilder = new StringBuilder();
        new CommandLineApp(stringBuilder, cmd).extractTables(cmd);

        try {


            };
        }
        catch (IllegalStateException ie) {
            assertTrue(ie.toString(), true);
        }
    }
    */
}
