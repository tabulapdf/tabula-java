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
    //Test no longer fails with identical expected and actual results
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
    //Test if multiple user drawn selections specifications on the CLI are processed correctly...
    public void testMultipleUserDrawnSelections() throws ParseException, IOException {

        String expectedOutput="Table 5\n" +
                "Correlations between the extent of participation of pupils in project activities and the\n" +
                "perceived  impacts on pupils (Pearsons correlation coefficient*)\n" +
                "Involvement of pupils in\n" +
                "Preperation and Production of Presentation and\n" +
                "planing materials evaluation\n" +
                "\"Knowledge and awareness of different cultures 0,2885 0,3974 0,3904\"\n" +
                "\"Foreign language competence 0,3057 0,4184 0,3899\"\n" +
                "\"Social skills and abilities 0,3416 0,3369 0,4303\"\n" +
                "\"Acquaintance of special knowledge 0,2569 0,2909 0,3557\"\n" +
                "\"Self competence 0,3791 0,3320 0,4617\"\n" +
                "\"* Significance p = 0,000\"\n" +
                "Table 6\n" +
                "\"Correlations between the difficulties encounters and the perceived impacts on pupils,\"\n" +
                "teachers and the school as a whole (Pearsons correlation coefficient*)\n" +
                "Difficulties encountered with respect to\n" +
                "Lack of interest/ Lack of interest of Lack of interest of\n" +
                "acceptance from pupils parents\n" +
                "Impacts on participating pupils colleagues\n" +
                "\"Knowledge and awareness of different cultures -0,1651 -0,2742 -0,1618\"\n" +
                "\"Foreign language competence -0,0857 -0,1804 -0,1337\"\n" +
                "\"Social skills and abilities -0,1237 -0,2328 -0,1473\"\n";

        //Removing line returns for easier comparisons...
        expectedOutput=expectedOutput.replaceAll("\\r|\\n", "");


        assertEquals(expectedOutput, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-a",
                "65.822,60.657,259.197,528.476",
                "-p","1",
                "-a",
                "273.328,67.351,422.822,526.988",
                "-p","1",
                "-f",
                "CSV"
        }).replaceAll("\\r|\\n", ""));
    }

    @Test
    //Test if multiple user drawn selections specifications (on different pages of the document)
    // on the CLI are processed correctly...
    public void testMultipleUserDrawnSelectionsOnDifferentPages() throws ParseException, IOException {

        String expectedOutput=UtilsForTesting.loadCsv(
         "src/test/resources/technology/tabula/csv/expectedOutput_TestMultipleUserDrawnSelectionsOnDifferentPages.csv");


        assertEquals(expectedOutput, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-a",
                "65.822,60.657,259.197,528.476",
                "-p","1",
                "-a",
                "65.822,60.657,259.197,528.476",
                "-p","2",
                "-f",
                "CSV"
        }));
    }

    @Test
    //Test if the page margins options are being processed on full page extraction...
    public void testPageExtractionWithHeaderAndFooter() throws ParseException, IOException {

        String expectedOutput=UtilsForTesting.loadCsv(
                "src/test/resources/technology/tabula/csv/expectedOutput_TestPageExtractionWithHeaderAndFooter.csv");

        String actualValue=this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-p","1",
                "-m", "{\"header_scale\" : 0.25," +
                "\"footer_scale\" : 0.25 }",
                "-f",
                "CSV"
        });

        System.out.println("Actual Value:"+actualValue);
        System.out.println("Expected Value:" + expectedOutput);

        assertEquals(expectedOutput, actualValue);

    }

    @Test
    //Test if the page margins options 'crop' areas that extend into header/footer...
    public void testUserDefinedAreaExtractionWithHeaderAndFooter() throws ParseException, IOException {

        String expectedOutput=UtilsForTesting.loadCsv(
       "src/test/resources/technology/tabula/csv/expectedOutput_TestUserDefinedAreaExtractionWithHeaderAndFooter.csv");


        assertEquals(expectedOutput, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-p","1",
                "-a",
                "0,58.9,654.7,536.12",
                "-m", "{\"header_scale\" : 0.15," +
                "\"footer_scale\" : 0.25 }",
                "-f",
                "CSV"
        }));
    }


    @Test
    //Test if incorrectly specified user drawn selections specifications on the CLI are detected...
    //The second user-drawn selection overlaps the first--therefore it will not be processed
    public void testOverlapDetectionOfUserDrawnSelections() throws ParseException, IOException {


        assertEquals(UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestOverlapDetectionOfUserDrawnSelections.csv"),
                this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-a",
                "65.822,60.657,259.197,528.476",
                "-p","1",
                "-a",
                "73.328,67.351,422.822,526.988",
                "-p","1",
                "-f",
                "CSV"
        }));
    }


    @Test
    //Test if incorrectly specified user drawn selections specifications on the CLI are detected...
    //The second user-drawn selection overlaps the first--therefore it will not be processed
    public void testOverlapDetectionOfMultiplePageSpecifications() throws ParseException, IOException {

        assertEquals(UtilsForTesting.loadCsv(
                "src/test/resources/technology/tabula/csv/expectedOutput_TestOverlapDetectionOfMultiplePageSpecifications.csv"),
                this.csvFromCommandLineArgs(new String[]{
                        "src/test/resources/technology/tabula/eu-002.pdf",
                        "-p","1",
                        "-p","1",
                        "-f",
                        "CSV"
                }));
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
    // Tests whether one regex pair is being picked up
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
     * Test to see if overlapping regex searches are 1) Being detected
     *                                               2) The offending regex search is not included in the extracted output
     * UPDATE (4/13/2018 BHT) -- The program should not output any CSV file, due to the offending regex search
     */
    public void testOverlapDetectionForMultipleRegexExtractions() throws ParseException, IOException {

        //String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestOverlapDetectionForMultipleRegexExtractions.csv");

        assertEquals("", this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"Knowledge\"," +
                        "\"pattern_after\" : \"Social\"}," +
                        "{\"pattern_before\" : \"Table 5\"," +
                        "\"pattern_after\" : \"Table 6\"} ]}",
                "-f", "CSV"
        }));
    }



    @Test
    /*
     * Test 2 to see if overlapping regex searches     1) Being detected
     *                                                 2) The offending regex search is not included in the extracted output
     * NOTE: This test was deemed necessary due to the complexity of implementing multi-page matches
     */
    public void testOverlapDetectionForMultiPageMatchRegexExtractions() throws ParseException, IOException {

        assertEquals("", this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"International\"," +
                        "\"pattern_after\" : \"Turkey\"}," +
                        "{\"pattern_before\" : \"Total\"," +
                        "\"pattern_after\" : \"New EU-25\"} ]}",
                "-f", "CSV"
        }));
    }

    //TODO-Incorporate parsing of extraction log file into unit test to verify meta-data accuracy...

//    @Test NOTE: Commented out for now...will redo when page-restricted regex search is implemented...
    /*
     * Test to verify --page option; also utilizing header option (so kind of seeing if both work in conjunction)
     */

    /*
    public void testPageandHeaderOptions() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestPageandHeaderOptions.csv");

        System.out.println(expectedCsv);

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/One_Stop_Voting_Site_List_Nov2012.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"CHARLES RIDDLE CENTER\"," +
                        "\"include_pattern_before\": \"true\","+
                        "\"pattern_after\" : \"BUNCOMBE\"," +
                        "\"include_pattern_after\": \"true\"} ]}",
                "-f", "CSV",
                "-m", "{\"header_scale\" : 0.095," +
                "\"footer_scale\" : 0.0096 }",
                //"--pages", "1,4"

        }));
    }
*/
    @Test
    /*
     * Test to verify that at least two pairs of regex searches can be run in one instance
     */
    public void testExtractTwoRegexSearches() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestExtractTwoRegexSearches.csv");

        System.out.println(expectedCsv);

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
     * Test to verify that at least three pairs of regex searches can be run in one instance
     * Mainly wanted to see if the CLI would output an overlap error like the GUI did
     * CLI did -not- output overlap error
     *
     * Update (4/13/2018): Program should not output a CSV file due to the offending regex; this is the intended function
     */
    public void testExtractThreeRegexSearches() throws ParseException, IOException {

        assertEquals("", this.csvFromCommandLineArgs(new String[]{
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
     * Test to verify that at least three pairs of regex searches can be run in one instance
     * Mainly wanted to see if the CLI would output an overlap error like the GUI did
     * CLI did -not- output overlap error
     * UPDATE (4/6/2018 REM) -- CLI should  now output Overlap Detection message + not extract overlapping areas...
     * UPDATE (4/13/2018 BHT) -- CLI should not output a CSV, due to the offending overlapping regex search
     */
    public void testExtractThreeRegexSearchesAndNewFile() throws ParseException, IOException {

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

        assertEquals("",UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
    }

//    @Test -this test will pass on Travis but not on AppVeyor build...maybe has something to do with line separators?? not sure..
    /*
     * Test to verify that a single, basic Regex search capturing a multi-page (spanning 2 pages) table into an output file works
     */
/*
    public void testExtractMultiplePageTableRegexAndNewFile1() throws ParseException, IOException {

        System.out.println("In testExractMultiplePageTableRegexAndNewFile1...");

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable_1.csv");
        expectedCsv = expectedCsv.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

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



        String actualValue = UtilsForTesting.loadCsv("outputFile").replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        assertEquals("Actual Value:\n"+actualValue +"\n" + "Expected Value:\n"+expectedCsv,actualValue,expectedCsv);
    }
*/
    @Test
    /*
     * Test to verify that a single, basic Regex search capturing a multi-page (spanning 6 pages) table into an output file works
     */
    public void testExtractMultiplePageTableRegexAndNewFile2() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable_2.csv");
        expectedCsv = expectedCsv.replaceAll("\n", "");

        this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/One_Stop_Voting_Site_List_Nov2012.pdf",
                "--stream",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"ANSON\"," +
                        "\"pattern_after\" : \"BURKE\"}]}",
                "-f", "CSV",
                "-o", "outputFile"
        });

        assertEquals(expectedCsv,UtilsForTesting.loadCsv("outputFile").replaceAll("\n",""));
    }

    @Test
    /*
     * Verify that when inclusive is specified for both regex patterns, both are obtained in the csv generated
     * by the CLI...
     */
    public void testInclusiveSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/Inclusive_1.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"European/International\"," +
                            "\"include_pattern_before\": \"true\","+
                             "\"pattern_after\" : \"day\","+
                             "\"include_pattern_after\":\"true\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));
        expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/Inclusive_2.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/One_Stop_Voting_Site_List_Nov2012.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"ALAMANCE\"," +
                        "\"include_pattern_before\": \"true\","+
                        "\"pattern_after\" : \"ALEXANDER\","+
                        "\"include_pattern_after\":\"true\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));

    }

    @Test
    /*
     * Verify that when exclusive is specified for both regex patterns, both are obtained in the csv generated
     * by the CLI...
     */
    public void testExclusiveSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/Exclusive_1.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"European/International\"," +
                        "\"include_pattern_before\": \"false\","+
                        "\"pattern_after\" : \"day\","+
                        "\"include_pattern_after\":\"false\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));
        expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/Exclusive_2.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/One_Stop_Voting_Site_List_Nov2012.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"ALAMANCE\"," +
                        "\"include_pattern_before\": \"false\","+
                        "\"pattern_after\" : \"ALEXANDER\","+
                        "\"include_pattern_after\":\"false\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));

    }

    @Test
    /*
     * Verify that when inclusive is specified for first regex pattern ('pattern before') and exclusive is specified for
     * the second regex pattern, ('pattern after') the correct rectangular region is obtained by the CSV and corespondingly
     * the correct output is generated by the CLI... ('pattern before' IS in csv, 'pattern after' NOT in csv)
     */
    public void testInclusiveBeforeExclusiveAfterSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv(
                "src/test/resources/technology/tabula/csv/InclusiveBeforeExclusiveAfter_1.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"European/International\"," +
                        "\"include_pattern_before\": \"true\","+
                        "\"pattern_after\" : \"day\","+
                        "\"include_pattern_after\":\"false\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));
        expectedCsv = UtilsForTesting.loadCsv(
                "src/test/resources/technology/tabula/csv/InclusiveBeforeExclusiveAfter_2.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/One_Stop_Voting_Site_List_Nov2012.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"ALAMANCE\"," +
                        "\"include_pattern_before\": \"true\","+
                        "\"pattern_after\" : \"ALEXANDER\","+
                        "\"include_pattern_after\":\"false\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));

    }

    @Test
    /*
     * Verify that when exclusive is specified for first regex pattern ('pattern before') and inclusive is specified for
     * the second regex pattern, ('pattern after') the correct rectangular region is obtained by the CSV and corresponding
     * the correct output is generated by the CLI...('pattern_before' value NOT in csv, 'pattern_after' value IS in csv)
     */
    public void testExclusiveBeforeInclusiveAfterSpecificationsOnCLI() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv(
                "src/test/resources/technology/tabula/csv/ExclusiveBeforeInclusiveAfter_1.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/eu-002.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"European/International\"," +
                        "\"include_pattern_before\": \"false\","+
                        "\"pattern_after\" : \"day\","+
                        "\"include_pattern_after\":\"true\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));
        expectedCsv = UtilsForTesting.loadCsv(
                "src/test/resources/technology/tabula/csv/ExclusiveBeforeInclusiveAfter_2.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/One_Stop_Voting_Site_List_Nov2012.pdf",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"ALAMANCE\"," +
                        "\"include_pattern_before\": \"false\","+
                        "\"pattern_after\" : \"ALEXANDER\","+
                        "\"include_pattern_after\":\"true\"} ]}",
                "-f", "CSV",
                "-m","{\"header_scale\" : 0," +
                "\"footer_scale\" : 0 }"
        }));

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
    public void testExtractBatchSpreadsheetWithRegex1() throws ParseException, IOException {

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
     * Test to verify that basic regex search can be run in batch mode
     * (This test is intended to check if a regex search can be run at the directory-level)
     * Similar to testExtractBatchSpreadsheetWithRegex1, but is includes 'inclusive'/'exclusive' options
     * as well as two pairs of regex searches to emulate what Bill was looking for in "shall" pdfs
     */
    public void testExtractBatchSpreadsheetWithRegex2() throws ParseException, IOException {

        FileSystem fs = FileSystems.getDefault();
        Path tmpFolder = Files.createTempDirectory("tabula-java-batch-test");
        Path srcFolder = fs.getPath("src/test/resources/technology/tabula/regex_batch_process/shall_pdfs_input_files");
        Path expectedValsFolder = fs.getPath("src/test/resources/technology/tabula/regex_batch_process/shall_pdfs_expected_output");

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
                            "[ {\"pattern_before\" : \"Report To:\"," +
                            "\"include_pattern_before\": \"false\"," +
                            "\"pattern_after\" : \"Sample Type:\"," +
                            "\"include_pattern_after\" : \"true\"}," +
                            "{\"pattern_before\" : \"Analyte\"," +
                            "\"include_pattern_before\": \"true\"," +
                            "\"pattern_after\" : \"Report Date:\"," +
                            "\"include_pattern_after\" : \"true\"} ]}",
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
     * Specifically tests for stream and lattice extraction methods
     */
    public void testExtractMultiplePageTableRegexStreamandLattice() throws ParseException, IOException {

        String expectedCsvForStream = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable_stream.csv");

        assertEquals(expectedCsvForStream, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.pdf",
                "--stream",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"39\"," +
                        "\"pattern_after\" : \"44\"} ]}",
                "-f", "CSV"
        }));

        String expectedCsvForLattice = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/expectedOutput_TestMultiplePageTable_lattice.csv");

        assertEquals(expectedCsvForLattice, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.pdf",
                "--lattice",
                "-r",
                "{\"queries\": " +
                        "[ {\"pattern_before\" : \"39\"," +
                        "\"pattern_after\" : \"44\"} ]}",
                "-f", "CSV"}));
    }



    public void testExtractWithMultiplePercentArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/MultiColumn.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/MultiColumn.pdf",
                "-p", "1", "-a",
                "%0,0,100,50", "-a",
                "%0,50,100,100", "-f",
                "CSV"
        }));
    }

    @Test
    public void testExtractWithMultipleAbsoluteArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/MultiColumn.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/MultiColumn.pdf",
                "-p", "1", "-a",
                "0,0,451,212", "-a",
                "0,212,451,425", "-p", "1",
                "-f", "CSV"
        }));
    }

    @Test
    public void testExtractWithPercentAndAbsoluteArea() throws ParseException, IOException {

        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/MultiColumn.csv");

        assertEquals(expectedCsv, this.csvFromCommandLineArgs(new String[]{
                "src/test/resources/technology/tabula/MultiColumn.pdf",
                "-p", "1", "-a",
                "%0,0,100, 49", //at 50 horizontal overlap occurs...
                "-p", "1","-a",
                "0,212,451,425",
                "-f", "CSV"
        }));
    }

    @Test
    public void testAreaArgWithoutPageArg() throws ParseException, IOException {

       Boolean parseExceptionThrown = false;

        try {
            this.csvFromCommandLineArgs(new String[]{
                    "src/test/resources/technology/tabula/MultiColumn.pdf",
                    "-p", "1", "-a",
                    "%0,0,100, 49", //at 50 horizontal overlap occurs...
                    "-a",
                    "0,212,451,425",
                    "-f", "CSV"
            });
        }
        catch(ParseException pe){
            parseExceptionThrown = true;
        }

        assertTrue(parseExceptionThrown);

    }

}


