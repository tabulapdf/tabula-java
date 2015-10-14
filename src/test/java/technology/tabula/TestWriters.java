package technology.tabula;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.JSONWriter;
import technology.tabula.writers.TSVWriter;

public class TestWriters {
    
    private static final String EXPECTED_CSV_WRITER_OUTPUT = "\"ABDALA de MATARAZZO, Norma Amanda \",\"Frente Cívico por Santiago \",\"Santiago del Estero \",AFIRMATIVO";
    private Table getTable() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/argentina_diputados_voting_record.pdf", 269.875f, 12.75f, 790.5f, 561f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        return table;
    }
    private List<Table> getTables() throws IOException {
        
    	Page page = UtilsForTesting.getPage("src/test/resources/technology/tabula/twotables.pdf", 1);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        return (List<Table>) sea.extract(page);    	
    }

    @Test
    public void testCSVWriter() throws IOException {
        Table table = this.getTable();
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        String s = sb.toString();
        String[] lines = s.split("\\r?\\n");
        assertEquals(lines[0], EXPECTED_CSV_WRITER_OUTPUT);
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
    public void testJSONSerializeTwoTables() throws IOException {
    	String expectedJson = UtilsForTesting.loadJson("src/test/resources/technology/tabula/json/twotables.json");
        List<Table> tables = this.getTables();
        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, tables);
        String s = sb.toString();
        assertEquals(expectedJson, s);
    }
    

}
