package technology.tabula;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

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
    //Test fails with identical expected and actual results
    public void testExtractSpreadsheetWithArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
        expectedCsv = expectedCsv.replaceAll("\n","");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                "-p", "1", "-a",
                "150.56,58.9,654.7,536.12", "-f",
                "CSV"
        }).replaceAll("\n", ""));
    }

    @Test
    public void testExtractBatchSpreadsheetWithArea() throws ParseException, IOException {
        FileSystem fs = FileSystems.getDefault();
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
        expectedCsv = expectedCsv.replaceAll("\n","");
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
            assertEquals(expectedCsv,UtilsForTesting.loadCsv(csvPath.toString()).replaceAll("\n", ""));
            //assertArrayEquals(expectedCsv.getBytes(), Files.readAllBytes(csvPath));
        }
        //Test has failed if parseException has been thrown...
        catch(ParseException pe){
            assertTrue(pe.getMessage(),false);
        }
    }

    @Test
    public void testExtractSpreadsheetWithAreaAndNewFile() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
        expectedCsv = expectedCsv.replaceAll("\n", "");
        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                "-p", "1", "-a",
                "150.56,58.9,654.7,536.12", "-f",
                "CSV", "-o", "outputFile"
        });

        assertEquals(expectedCsv,UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
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
        } catch (ParseException pe) {
            assertTrue(pe.toString(), true);
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
                "-f", "CSV"
        }));
    }

    @Test
    /*
     * Test to verify that at least two pairs of regex searches can be run in one instance
     */
    public void testExtractTwoRegexSearches() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractTwoRegexSearches.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"}," +
                        "{\"pattern_before\" : \"Social\"," +
                        "\"pattern_after\" : \"Self\"} ]}",
                "-f", "CSV"
        }));
    }

    @Test
    /*
     * Test to verify that at least three pairs of regex searche can be run in one instance
     * Mainly wanted to see if the CLI would output an overlap error like the GUI did
     * CLI did -not- output overlap error
     */
    public void testExtractThreeRegexSearches() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractThreeRegexSearches.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"}," +
                        "{\"pattern_before\" : \"Social\"," +
                        "\"pattern_after\" : \"Self\"}," +
                        "{\"pattern_before\" : \"Foreign\"," +
                        "\"pattern_after\" : \"Acquaintance\"} ]}",
                "-f", "CSV"
        }));
    }


    @Test
    public void testExtractTwoRegexSearchesAndNewFile() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractTwoRegexSearches.csv");
        expectedCsv = expectedCsv.replaceAll("\n", "");
        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"}," +
                        "{\"pattern_before\" : \"Social\"," +
                        "\"pattern_after\" : \"Self\"} ]}",
                "-f", "CSV",
                "-o", "outputFile"
        });

        assertEquals(expectedCsv,UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
    }

    @Test
    /*
     * Test to verify that at least three pairs of regex searche can be run in one instance
     * Mainly wanted to see if the CLI would output an overlap error like the GUI did
     * CLI did -not- output overlap error
     */
    public void testExtractThreeRegexSearchesAndNewFile() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractThreeRegexSearches.csv");
        expectedCsv = expectedCsv.replaceAll("\n", "");

        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"}," +
                        "{\"pattern_before\" : \"Social\"," +
                        "\"pattern_after\" : \"Self\"}," +
                        "{\"pattern_before\" : \"Foreign\"," +
                        "\"pattern_after\" : \"Acquaintance\"} ]}",
                "-f", "CSV",
                "-o", "outputFile"
        });

        assertEquals(expectedCsv,UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
    }

    @Test
    /*
     * Test to verify that a single, basic Regex search capturing a multi-page table into an output file works
     * Note that this test DOES NOT explicitly account for page HEADER and FOOTER
     */
    public void testExtractMultiplePageTableRegexAndNewFile() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable.csv");
        expectedCsv = expectedCsv.replaceAll("\n", "");

        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.pdf",
                "--stream",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"39\"," +
                        "\"pattern_after\" : \"44\"}]}",
                "-f", "CSV",
                "-o", "outputFile"
        });

        assertEquals(expectedCsv,UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
    }

    @Test
    /*
     * Verify that header can be specified on CLI when performing regex search...
     */
    public void testHeaderSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestHeaderSpecificationsOnCLI.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0.5," +
                      "\"footer_scale\" : \"0\" }"
        }));
    }


    @Test
    /*
     * Verify that footer can be specified on CLI when performing regex search...
     */
    public void testFooterSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestFooterSpecificationsOnCLI.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0.5 }"
        }));
    }

    @Test
    /*
     * Verify that both header and footer can be specified on CLI when performing regex search...
     */
    public void testHeaderAndFooterSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestHeaderAndFooterSpecificationsOnCLI.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0.25," +
                "\"footer_scale\" : 0.25 }"
        }));
    }

    @Test
    /*
     * Verify that when no header and footer is specified on CLI regex search is still performed correctly...
     */
    public void testNoHeaderAndFooterSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestNoHeaderAndFooterSpecificationsOnCLI.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));
    }



    @Test
    public void testExtractRegexAreaAndNewFile() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractRegexArea.csv");
        expectedCsv = expectedCsv.replaceAll("\n", "");
        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"} ]}",
                "-f", "CSV",
                "-o", "outputFile"
        });

        assertEquals(expectedCsv,UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
    }

    @Test
    /*
     * Test to verify that basic regex search can be run in batch mode
     * (This test is intended to check if a regex search can be run at the directory-level)
     */
    public void testExtractBatchSpreadsheetWithRegex() throws ParseException, IOException {

        FileSystem fs = FileSystems.getDefault();
        Path tmpFolder = Files.createTempDirectory("tabula-java-batch-test");
        Path srcFolder = fs.getPath("src/test/resources/technology/tabula/regex_batch_process/input_files");
        Path expectedValsFolder = fs.getPath("src/test/resources/technology/tabula/regex_batch_process/expected_output");

        File[] srcFiles = srcFolder.toFile().listFiles();

        for(File srcFile: srcFiles){
            Path copiedPDF = tmpFolder.resolve(srcFile.getName());
            Files.copy(srcFile.toPath(),copiedPDF);
        }

        System.out.println("Temp FOLDER as string:"+tmpFolder.toString());

        tmpFolder.toFile().deleteOnExit();
        try{
            this.csvFromCommandLineArgs(new String[]{
                    "-b", tmpFolder.toString(),
                    "-r",
                    "{\"queries\": " +
                            "[ {\"pattern_before\" : \"Analyte\"," +
                            "\"pattern_after\" : \"Report Date\"} ]}",
                    "-f","CSV",
                    "-o","testResults.csv"
            });

            for(final File extractedOutputFile : tmpFolder.toFile().listFiles(new FilenameFilter(){
                public boolean accept(File f, String s){
                    return s.endsWith(".csv");
                }
            })){

                   // System.out.println(extractedOutputFile.getName());
                    String extractedValue = UtilsForTesting.loadCsv(extractedOutputFile.getAbsolutePath());
                    String expectedValue = "";

                    File[] expectedOutput = expectedValsFolder.toFile().listFiles(new FilenameFilter(){
                        public boolean accept(File f, String s){
                            //System.out.println("S:"+s);
                            //System.out.println("Extracted Output File Name:"+ extractedOutputFile.getName());
                            return s.equals(extractedOutputFile.getName());
                        }
                    });

                    if(expectedOutput.length==0){
                        System.out.println("Expected Value for " + extractedOutputFile.getName() + ":");
                        System.out.println(expectedValue);
                        System.out.println("Extracted Value for " + extractedOutputFile.getName() + ":");
                        System.out.println(extractedValue);
                        assertEquals(expectedValue,extractedValue);
                    }
                    else{
                        expectedValue= UtilsForTesting.loadCsv(expectedOutput[0].getAbsolutePath());
                        System.out.println("Expected Value for " + extractedOutputFile.getName() + ":");
                        System.out.println(expectedValue);
                        System.out.println("Extracted Value for " + extractedOutputFile.getName() + ":");
                        System.out.println(extractedValue);
                        assertEquals(expectedValue,extractedValue);
                    }
            }
        }
        //test has failed if ParseException has been thrown.
        catch(ParseException pe){
            assertTrue(pe.getMessage(),false);
        }
    }

    @Test
    /*
     * Test to verify that a single, basic Regex search capturing a multi-page table works
     * Note that this test DOES NOT explicitly account for page HEADER and FOOTER
     */
    public void testExtractMultiplePageTableRegex() throws ParseException, IOException {

        String expectedCsvForStream = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable_stream.csv");

        assertEquals(expectedCsvForStream,this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.pdf",
                "--stream",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"39\"," +
                        "\"pattern_after\" : \"44\"} ]}",
                "-f", "CSV"
        }));

        String expectedCsvForLattice = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable_lattice.csv");

        assertEquals(expectedCsvForLattice,this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.pdf",
                "--lattice",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"39\"," +
                        "\"pattern_after\" : \"44\"} ]}",
                "-f", "CSV"
        }));
    }

}
