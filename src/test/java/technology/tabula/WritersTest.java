package technology.tabula;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.junit.Test;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.JSONWriter;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static technology.tabula.UtilsForTesting.*;

public class WritersTest {

    private static final String EXPECTED_CSV_WRITER_OUTPUT = "\"ABDALA de MATARAZZO, Norma Amanda\",Frente Cívico por Santiago,Santiago del Estero,AFIRMATIVO";

    private Table getTable() throws IOException {
        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/argentina_diputados_voting_record.pdf", 269.875f, 12.75f, 790.5f, 561f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        return bea.extract(page).get(0);
    }

    private List<? extends Table> getTables() throws IOException {

        Page page = getPage("src/test/resources/technology/tabula/twotables.pdf", 1);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        return sea.extract(page);
    }

    @Test
    public void testCSVWriter() throws IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/argentina_diputados_voting_record.csv");
        Table table = getTable();

        String s = CSVWriter.writeString(table);
        String[] lines = s.split("\\r?\\n");

        assertThat(lines[0]).isEqualTo(EXPECTED_CSV_WRITER_OUTPUT);
        assertThat(s).isEqualTo(expectedCsv);
    }

    // TODO Add assertions
    @Test
    public void testTSVWriter() throws IOException {
        Table table = getTable();
        CSVWriter.writeString(table);

        //System.out.println(s);
        //String[] lines = s.split("\\r?\\n");
        //assertThat(EXPECTED_CSV_WRITER_OUTPUT).isEqualTo(lines[0]);
    }

    @Test
    public void testJSONWriter() throws IOException {
        String expectedJson = loadTextFile("src/test/resources/technology/tabula/json/argentina_diputados_voting_record.json");
        Table table = getTable();

        assertThat(CSVWriter.writeString(table)).isEqualTo(expectedJson);
    }

    @Test
    public void testJSONSerializeInfinity() throws IOException {
        String expectedJson = loadTextFile("src/test/resources/technology/tabula/json/schools.json");
        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/schools.pdf", 53.74f, 16.97f, 548.74f, 762.3f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        assertThat(CSVWriter.writeString(table)).isEqualTo(expectedJson);
    }

    @Test
    public void testCSVSerializeInfinity() throws IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/schools.csv");
        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/schools.pdf", 53.74f, 16.97f, 548.74f, 762.3f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        assertThat(CSVWriter.writeString(table)).isEqualTo(expectedCsv);
    }

    @Test
    public void testJSONSerializeTwoTables() throws IOException {
        String expectedJson = loadTextFile("src/test/resources/technology/tabula/json/twotables.json");
        List<? extends Table> tables = getTables();
        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, tables);

        String s = sb.toString();
        assertThat(s).isEqualTo(expectedJson);

        Gson gson = new Gson();
        JsonArray json = gson.fromJson(s, JsonArray.class);

        assertThat(json).hasSize(2);
    }

    @Test
    public void testCSVSerializeTwoTables() throws IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/twotables.csv");
        List<? extends Table> tables = getTables();

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables);
        String s = sb.toString();

        assertThat(s).isEqualTo(expectedCsv);
    }

    @Test
    public void testCSVMultilineRow() throws IOException {
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/frx_2012_disclosure.csv");
        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/frx_2012_disclosure.pdf", 53.0f, 49.0f, 735.0f, 550.0f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        assertThat(CSVWriter.writeString(table)).isEqualTo(expectedCsv);
    }
}
