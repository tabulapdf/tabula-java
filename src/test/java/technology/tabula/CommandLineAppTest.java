package technology.tabula;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static technology.tabula.UtilsForTesting.loadNormalizedCsv;
import static technology.tabula.UtilsForTesting.loadTextFile;

public class CommandLineAppTest {

    private String csvFromCommandLineArgs(String... args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);

        StringBuilder stringBuilder = new StringBuilder();
        new CommandLineApp(stringBuilder, cmd).extractTables(cmd);

        return stringBuilder.toString();
    }

    @Test
    public void testExtractSpreadsheetWithArea() throws ParseException, IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");

        assertThat(csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                "-p", "1",
                "-a", "150.56,58.9,654.7,536.12",
                "-f", "CSV")
                // TODO
        ).isEqualToNormalizingWhitespace(expectedCsv);
    }

    @Test
    public void testExtractBatchSpreadsheetWithArea() throws ParseException, IOException {
        FileSystem fs = FileSystems.getDefault();
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");
        Path tmpFolder = Files.createTempDirectory("tabula-java-batch-test");
        tmpFolder.toFile().deleteOnExit();

        Path copiedPDF = tmpFolder.resolve(fs.getPath("spreadsheet.pdf"));
        Path sourcePDF = fs.getPath("src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf");
        Files.copy(sourcePDF, copiedPDF);
        copiedPDF.toFile().deleteOnExit();

        csvFromCommandLineArgs(
                "-b", tmpFolder.toString(),
                "-p", "1",
                "-a", "150.56,58.9,654.7,536.12",
                "-f", "CSV");

        Path csvPath = tmpFolder.resolve(fs.getPath("spreadsheet.csv"));
        assertThat(csvPath.toFile()).exists().hasContent(expectedCsv);
    }

    @Test
    public void testExtractSpreadsheetWithAreaAndNewFile() throws ParseException, IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");

        assertThat(csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                "-p", "1",
                "-a", "150.56,58.9,654.7,536.12",
                "-f", "CSV",
                "-o", "target/outputFile")
        );//.isEqualTo(expectedCsv); // TODO
    }

    @Test
    public void testExtractJSONWithArea() throws ParseException, IOException {
        String expectedJson = loadTextFile("src/test/resources/technology/tabula/json/spanning_cells_basic.json");

        assertThat(csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/spanning_cells.pdf",
                "-p", "1",
                "-a", "150.56,58.9,654.7,536.12",
                "-f", "JSON")
        ).isEqualTo(expectedJson);
    }

    @Test
    public void testExtractCSVWithArea() throws ParseException, IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");

        assertThat(csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/spanning_cells.pdf",
                "-p", "1",
                "-a", "150.56,58.9,654.7,536.12",
                "-f", "CSV")
        ).isEqualTo(expectedCsv);
    }

    @Test
    public void testGuessOption() throws ParseException, IOException {
        String expectedCsvNoGuessing = loadNormalizedCsv(
                "src/test/resources/technology/tabula/csv/CommandLineAppTest_testGuessOption_no_guessing.csv");
        assertThat(csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-eu/eu-001.pdf",
                "-p", "1",
                "-f", "CSV")
        ).isEqualTo(expectedCsvNoGuessing);

        String expectedCsvWithGuessing = loadNormalizedCsv(
                "src/test/resources/technology/tabula/csv/CommandLineAppTest_testGuessOption_with_guessing.csv");
        assertThat(csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-eu/eu-001.pdf",
                "-p", "1",
                "-f", "CSV",
                "-g")
        ).isEqualTo(expectedCsvWithGuessing);
    }

    @Test
    public void testEncryptedPasswordSupplied() throws ParseException {
        String s = csvFromCommandLineArgs(
                "src/test/resources/technology/tabula/encrypted.pdf",
                "-s", "userpassword",
                "-p", "1",
                "-f", "CSV");
        assertThat(s.split("\\r?\\n")[0]).isEqualTo("FLA Audit Profile,,,,,,,,,");
    }

    @Test(expected=org.apache.commons.cli.ParseException.class)
    public void testEncryptedWrongPassword() throws ParseException {
        csvFromCommandLineArgs("src/test/resources/technology/tabula/encrypted.pdf",
                "-s", "wrongpassword",
                "-p", "1",
                "-f", "CSV");
        fail("Should fail with wrong password");
    }
}
