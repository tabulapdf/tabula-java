package technology.tabula;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.junit.Test;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.JSONWriter;
import technology.tabula.writers.TSVWriter;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestWriters {

    private static final String EXPECTED_CSV_WRITER_OUTPUT = "\"ABDALA de MATARAZZO, Norma Amanda\",Frente Cívico por Santiago,Santiago del Estero,AFIRMATIVO";

    private Table getTable() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/argentina_diputados_voting_record.pdf", 269.875f, 12.75f, 790.5f, 561f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        return table;
    }

    private List<Table> getTables() throws IOException {

        Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/twotables.pdf", 1);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        return sea.extract(page);
    }

    @Test
    public void testCSVWriter() throws IOException {
    	String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/argentina_diputados_voting_record.csv");
        Table table = this.getTable();
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        String s = sb.toString();
        String[] lines = s.split("\\r?\\n");
        assertEquals(lines[0], EXPECTED_CSV_WRITER_OUTPUT);
        assertEquals(expectedCsv, s);
    }

    // TODO Add assertions
    @Test
    public void testTSVWriter() throws IOException {
        Table table = this.getTable();
        StringBuilder sb = new StringBuilder();
        (new TSVWriter()).write(sb, table);
        String s = sb.toString();
        //System.out.println(s);
        //String[] lines = s.split("\\r?\\n");
        //assertEquals(lines[0], EXPECTED_CSV_WRITER_OUTPUT);
    }

    @Test
    public void testJSONWriter() throws IOException {
        String expectedJson = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/argentina_diputados_voting_record.json");
        Table table = this.getTable();
        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, table);
        String s = sb.toString();
        assertEquals(expectedJson, s);
    }

    @Test
    public void testJSONSerializeInfinity() throws IOException {
        String expectedJson = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/schools.json");
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/schools.pdf", 53.74f, 16.97f, 548.74f, 762.3f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, table);
        String s = sb.toString();
        assertEquals(expectedJson, s);
    }

    @Test
    public void testCSVSerializeInfinity() throws IOException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/schools.csv");
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/schools.pdf", 53.74f, 16.97f, 548.74f, 762.3f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        String s = sb.toString();
        assertEquals(expectedCsv, s);
    }

    @Test
    public void testJSONSerializeTwoTables() throws IOException {
        String expectedJson = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/twotables.json");
        List<Table> tables = this.getTables();
        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, tables);

        String s = sb.toString();
        assertEquals(expectedJson, s);

        Gson gson = new Gson();
        JsonArray json = gson.fromJson(s, JsonArray.class);
        assertEquals(2, json.size());
    }

    @Test
    public void testCSVSerializeTwoTables() throws IOException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/twotables.csv");
        List<Table> tables = this.getTables();
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables);

        String s = sb.toString();
        assertEquals(expectedCsv, s);
    }

    @Test
    public void testCSVMultilineRow() throws IOException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/frx_2012_disclosure.csv");
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/frx_2012_disclosure.pdf", 53.0f, 49.0f, 735.0f, 550.0f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        String s = sb.toString();
        assertEquals(expectedCsv, s);
    }

    private Table getTableWithColAndRowSpans() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/table_with_col_and_row_spans.pdf", 68.08515F, 69.2013F, 152.16846F, 526.07874F);
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        return bea.extract(page).get(0);
    }

    @Test
    public void testCSVSpanCells() throws IOException {
        String expectedCsvFilled = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/table_with_col_and_row_spans_filled.csv");
        String expectedCsvDefault = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/table_with_col_and_row_spans_default.csv");
        Table table = getTableWithColAndRowSpans();

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        String s = sb.toString();

        assertEquals(expectedCsvDefault, s);

        table.setFillSpanCells(true);
        sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        s = sb.toString();

        assertEquals(expectedCsvFilled, s);
    }

    @Test
    public void testJSONFillSpanCells() throws IOException {
        String expectedJsonFilled = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/table_with_col_and_row_spans_filled.json");
        String expectedJsonDefault = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/table_with_col_and_row_spans_default.json");
        Table table = getTableWithColAndRowSpans();

        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, table);
        String s = sb.toString();

        assertEquals(expectedJsonDefault, s);

        table.setFillSpanCells(true);
        sb = new StringBuilder();
        (new JSONWriter()).write(sb, table);
        s = sb.toString();

        assertEquals(expectedJsonFilled, s);
    }

}
