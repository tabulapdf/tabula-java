package technology.tabula;

import org.junit.Test;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import static org.junit.Assert.*;

import java.io.IOException;

/**
 * File created on 11.10.17.
 *
 * @author moritzhabegger
 */
public class TestMarkingSpanningCells {
    @Test
    public void testCorrectCellSpanMarking() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/table_with_col_and_row_spans.pdf", 68.08515F, 69.2013F, 152.16846F, 526.07874F);
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        String[][] result = UtilsForTesting.tableToArrayOfRows(table, true);

        assertTrue(result[0][4].startsWith("[1]"));
        assertTrue(result[1][4].startsWith("[1]"));
        assertTrue(result[2][1].startsWith("[2]"));
        assertTrue(result[3][1].startsWith("[2]"));
        assertTrue(result[3][2].startsWith("[3]"));
        assertTrue(result[3][3].startsWith("[3]"));
        assertEquals("[0]", result[4][0]);
        assertEquals("[0]", result[4][1]);
    }
}
