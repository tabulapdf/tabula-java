package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.nerdpower.tabula.Cell;
import org.nerdpower.tabula.Page;
import org.nerdpower.tabula.Rectangle;
import org.nerdpower.tabula.Ruling;
import org.nerdpower.tabula.TextChunk;
import org.nerdpower.tabula.TextElement;
import org.nerdpower.tabula.extractors.SpreadsheetExtractionAlgorithm;
import org.nerdpower.tabula.writers.CSVWriter;
import org.nerdpower.tabula.UtilsForTesting;

public class TestSpreadsheetExtractor {

    private static final Cell[] CELLS = new Cell[] {
            new Cell(40.0f, 18.0f, 208.0f, 4.0f),
            new Cell(44.0f, 18.0f, 52.0f, 6.0f),
            new Cell(50.0f, 18.0f, 52.0f, 4.0f),
            new Cell(54.0f, 18.0f, 52.0f, 6.0f),
            new Cell(60.0f, 18.0f, 52.0f, 4.0f),
            new Cell(64.0f, 18.0f, 52.0f, 6.0f),
            new Cell(70.0f, 18.0f, 52.0f, 4.0f),
            new Cell(74.0f, 18.0f, 52.0f, 6.0f),
            new Cell(90.0f, 18.0f, 52.0f, 4.0f),
            new Cell(94.0f, 18.0f, 52.0f, 6.0f),
            new Cell(100.0f, 18.0f, 52.0f, 28.0f),
            new Cell(128.0f, 18.0f, 52.0f, 4.0f),
            new Cell(132.0f, 18.0f, 52.0f, 64.0f),
            new Cell(196.0f, 18.0f, 52.0f, 66.0f),
            new Cell(262.0f, 18.0f, 52.0f, 4.0f),
            new Cell(266.0f, 18.0f, 52.0f, 84.0f),
            new Cell(350.0f, 18.0f, 52.0f, 4.0f),
            new Cell(354.0f, 18.0f, 52.0f, 32.0f),
            new Cell(386.0f, 18.0f, 52.0f, 38.0f),
            new Cell(424.0f, 18.0f, 52.0f, 18.0f),
            new Cell(442.0f, 18.0f, 52.0f, 74.0f),
            new Cell(516.0f, 18.0f, 52.0f, 28.0f),
            new Cell(544.0f, 18.0f, 52.0f, 4.0f),
            new Cell(44.0f, 70.0f, 156.0f, 6.0f),
            new Cell(50.0f, 70.0f, 156.0f, 4.0f),
            new Cell(54.0f, 70.0f, 156.0f, 6.0f),
            new Cell(60.0f, 70.0f, 156.0f, 4.0f),
            new Cell(64.0f, 70.0f, 156.0f, 6.0f),
            new Cell(70.0f, 70.0f, 156.0f, 4.0f),
            new Cell(74.0f, 70.0f, 156.0f, 6.0f),
            new Cell(84.0f, 70.0f, 2.0f, 6.0f),
            new Cell(90.0f, 70.0f, 156.0f, 4.0f),
            new Cell(94.0f, 70.0f, 156.0f, 6.0f),
            new Cell(100.0f, 70.0f, 156.0f, 28.0f),
            new Cell(128.0f, 70.0f, 156.0f, 4.0f),
            new Cell(132.0f, 70.0f, 156.0f, 64.0f),
            new Cell(196.0f, 70.0f, 156.0f, 66.0f),
            new Cell(262.0f, 70.0f, 156.0f, 4.0f),
            new Cell(266.0f, 70.0f, 156.0f, 84.0f),
            new Cell(350.0f, 70.0f, 156.0f, 4.0f),
            new Cell(354.0f, 70.0f, 156.0f, 32.0f),
            new Cell(386.0f, 70.0f, 156.0f, 38.0f),
            new Cell(424.0f, 70.0f, 156.0f, 18.0f),
            new Cell(442.0f, 70.0f, 156.0f, 74.0f),
            new Cell(516.0f, 70.0f, 156.0f, 28.0f),
            new Cell(544.0f, 70.0f, 156.0f, 4.0f),
            new Cell(84.0f, 72.0f, 446.0f, 6.0f),
            new Cell(90.0f, 226.0f, 176.0f, 4.0f),
            new Cell(94.0f, 226.0f, 176.0f, 6.0f),
            new Cell(100.0f, 226.0f, 176.0f, 28.0f),
            new Cell(128.0f, 226.0f, 176.0f, 4.0f),
            new Cell(132.0f, 226.0f, 176.0f, 64.0f),
            new Cell(196.0f, 226.0f, 176.0f, 66.0f),
            new Cell(262.0f, 226.0f, 176.0f, 4.0f),
            new Cell(266.0f, 226.0f, 176.0f, 84.0f),
            new Cell(350.0f, 226.0f, 176.0f, 4.0f),
            new Cell(354.0f, 226.0f, 176.0f, 32.0f),
            new Cell(386.0f, 226.0f, 176.0f, 38.0f),
            new Cell(424.0f, 226.0f, 176.0f, 18.0f),
            new Cell(442.0f, 226.0f, 176.0f, 74.0f),
            new Cell(516.0f, 226.0f, 176.0f, 28.0f),
            new Cell(544.0f, 226.0f, 176.0f, 4.0f),
            new Cell(90.0f, 402.0f, 116.0f, 4.0f),
            new Cell(94.0f, 402.0f, 116.0f, 6.0f),
            new Cell(100.0f, 402.0f, 116.0f, 28.0f),
            new Cell(128.0f, 402.0f, 116.0f, 4.0f),
            new Cell(132.0f, 402.0f, 116.0f, 64.0f),
            new Cell(196.0f, 402.0f, 116.0f, 66.0f),
            new Cell(262.0f, 402.0f, 116.0f, 4.0f),
            new Cell(266.0f, 402.0f, 116.0f, 84.0f),
            new Cell(350.0f, 402.0f, 116.0f, 4.0f),
            new Cell(354.0f, 402.0f, 116.0f, 32.0f),
            new Cell(386.0f, 402.0f, 116.0f, 38.0f),
            new Cell(424.0f, 402.0f, 116.0f, 18.0f),
            new Cell(442.0f, 402.0f, 116.0f, 74.0f),
            new Cell(516.0f, 402.0f, 116.0f, 28.0f),
            new Cell(544.0f, 402.0f, 116.0f, 4.0f),
            new Cell(84.0f, 518.0f, 246.0f, 6.0f),
            new Cell(90.0f, 518.0f, 186.0f, 4.0f),
            new Cell(94.0f, 518.0f, 186.0f, 6.0f),
            new Cell(100.0f, 518.0f, 186.0f, 28.0f),
            new Cell(128.0f, 518.0f, 186.0f, 4.0f),
            new Cell(132.0f, 518.0f, 186.0f, 64.0f),
            new Cell(196.0f, 518.0f, 186.0f, 66.0f),
            new Cell(262.0f, 518.0f, 186.0f, 4.0f),
            new Cell(266.0f, 518.0f, 186.0f, 84.0f),
            new Cell(350.0f, 518.0f, 186.0f, 4.0f),
            new Cell(354.0f, 518.0f, 186.0f, 32.0f),
            new Cell(386.0f, 518.0f, 186.0f, 38.0f),
            new Cell(424.0f, 518.0f, 186.0f, 18.0f),
            new Cell(442.0f, 518.0f, 186.0f, 74.0f),
            new Cell(516.0f, 518.0f, 186.0f, 28.0f),
            new Cell(544.0f, 518.0f, 186.0f, 4.0f),
            new Cell(90.0f, 704.0f, 60.0f, 4.0f),
            new Cell(94.0f, 704.0f, 60.0f, 6.0f),
            new Cell(100.0f, 704.0f, 60.0f, 28.0f),
            new Cell(128.0f, 704.0f, 60.0f, 4.0f),
            new Cell(132.0f, 704.0f, 60.0f, 64.0f),
            new Cell(196.0f, 704.0f, 60.0f, 66.0f),
            new Cell(262.0f, 704.0f, 60.0f, 4.0f),
            new Cell(266.0f, 704.0f, 60.0f, 84.0f),
            new Cell(350.0f, 704.0f, 60.0f, 4.0f),
            new Cell(354.0f, 704.0f, 60.0f, 32.0f),
            new Cell(386.0f, 704.0f, 60.0f, 38.0f),
            new Cell(424.0f, 704.0f, 60.0f, 18.0f),
            new Cell(442.0f, 704.0f, 60.0f, 74.0f),
            new Cell(516.0f, 704.0f, 60.0f, 28.0f),
            new Cell(544.0f, 704.0f, 60.0f, 4.0f),
            new Cell(84.0f, 764.0f, 216.0f, 6.0f),
            new Cell(90.0f, 764.0f, 216.0f, 4.0f),
            new Cell(94.0f, 764.0f, 216.0f, 6.0f),
            new Cell(100.0f, 764.0f, 216.0f, 28.0f),
            new Cell(128.0f, 764.0f, 216.0f, 4.0f),
            new Cell(132.0f, 764.0f, 216.0f, 64.0f),
            new Cell(196.0f, 764.0f, 216.0f, 66.0f),
            new Cell(262.0f, 764.0f, 216.0f, 4.0f),
            new Cell(266.0f, 764.0f, 216.0f, 84.0f),
            new Cell(350.0f, 764.0f, 216.0f, 4.0f),
            new Cell(354.0f, 764.0f, 216.0f, 32.0f),
            new Cell(386.0f, 764.0f, 216.0f, 38.0f),
            new Cell(424.0f, 764.0f, 216.0f, 18.0f),
            new Cell(442.0f, 764.0f, 216.0f, 74.0f),
            new Cell(516.0f, 764.0f, 216.0f, 28.0f),
            new Cell(544.0f, 764.0f, 216.0f, 4.0f) };
    
    public static final Rectangle[] EXPECTED_RECTANGLES = {
        new Rectangle(40.0f, 18.0f, 208.0f, 40.0f),
        new Rectangle(84.0f, 18.0f, 962.0f, 464.0f)
    };
    
    private static final Ruling[] VERTICAL_RULING_LINES = { 
            new Ruling(40.0f, 18.0f, 0.0f, 40.0f),
            new Ruling(44.0f, 70.0f, 0.0f, 36.0f),
            new Ruling(40.0f, 226.0f, 0.0f, 40.0f) 
            };

    private static final Ruling[] HORIZONTAL_RULING_LINES = {
            new Ruling(40.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(44.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(50.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(54.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(60.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(64.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(70.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(74.0f, 18.0f, 208.0f, 0.0f),
            new Ruling(80.0f, 18.0f, 208.0f, 0.0f) 
    };
    
    
    private static final Cell[] EXPECTED_CELLS = { 
            new Cell(40.0f, 18.0f, 208.0f, 4.0f),
            new Cell(44.0f, 18.0f, 52.0f, 6.0f),
            new Cell(50.0f, 18.0f, 52.0f, 4.0f),
            new Cell(54.0f, 18.0f, 52.0f, 6.0f),
            new Cell(60.0f, 18.0f, 52.0f, 4.0f),
            new Cell(64.0f, 18.0f, 52.0f, 6.0f),
            new Cell(70.0f, 18.0f, 52.0f, 4.0f),
            new Cell(74.0f, 18.0f, 52.0f, 6.0f),
            new Cell(44.0f, 70.0f, 156.0f, 6.0f),
            new Cell(50.0f, 70.0f, 156.0f, 4.0f),
            new Cell(54.0f, 70.0f, 156.0f, 6.0f),
            new Cell(60.0f, 70.0f, 156.0f, 4.0f),
            new Cell(64.0f, 70.0f, 156.0f, 6.0f),
            new Cell(70.0f, 70.0f, 156.0f, 4.0f),
            new Cell(74.0f, 70.0f, 156.0f, 6.0f) };

    @Test
    public void testLinesToCells() {
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<Cell> cells = se.findCells(Arrays.asList(HORIZONTAL_RULING_LINES), Arrays.asList(VERTICAL_RULING_LINES));
        Collections.sort(cells);
        List<Cell> expected = Arrays.asList(EXPECTED_CELLS);
        Collections.sort(expected);
        assertTrue(cells.equals(expected));
    }

    @Test
    public void testFindSpreadsheetsFromCells() {
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Rectangle> cells = Arrays.asList(CELLS);
        List<Rectangle> expected = Arrays.asList(EXPECTED_RECTANGLES);
        Collections.sort(expected);
        List<Rectangle> foundRectangles = se.findSpreadsheetsFromCells(cells);
        Collections.sort(foundRectangles);
        assertTrue(foundRectangles.equals(expected));
    }
    
    @Test
    public void testSpanningCells() throws IOException {
        // TODO Add assertions
        Page page = UtilsForTesting
                .getFirstPage("src/test/resources/org/nerdpower/tabula/spanning_cells.pdf");
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertEquals(2, tables.size());
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(0));
        System.out.println(sb.toString());
    }
    
    @Test
    public void testIncompleteGrid() throws IOException {
        Page page = UtilsForTesting.getFirstPage("src/test/resources/org/nerdpower/tabula/china.pdf");
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertEquals(2, tables.size());
        List<List<RectangularTextContainer>> rows = tables.get(0).getRows(); 
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(0));
        System.out.println(sb.toString());
    }

}
