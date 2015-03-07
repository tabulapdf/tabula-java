package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.nerdpower.tabula.Cell;
import org.nerdpower.tabula.Page;
import org.nerdpower.tabula.Rectangle;
import org.nerdpower.tabula.Ruling;
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
        List<Cell> cells = SpreadsheetExtractionAlgorithm.findCells(Arrays.asList(HORIZONTAL_RULING_LINES), Arrays.asList(VERTICAL_RULING_LINES));
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
    
    // TODO Add assertions
    @Test
    public void testSpreadsheetExtraction() throws IOException {
        Page page = UtilsForTesting
                .getAreaFromFirstPage(
                        "src/test/resources/org/nerdpower/tabula/argentina_diputados_voting_record.pdf",
                        269.875f, 12.75f, 790.5f, 561f);
        
        SpreadsheetExtractionAlgorithm.findCells(page.getHorizontalRulings(), page.getVerticalRulings());
    }
 
    // TODO Add assertions
    @Test
    public void testSpanningCells() throws IOException {
        Page page = UtilsForTesting
                .getPage("src/test/resources/org/nerdpower/tabula/spanning_cells.pdf", 1);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertEquals(2, tables.size());
                
        for (List<RectangularTextContainer> r: tables.get(1).getRows()) {
            for (RectangularTextContainer rtc: r) {
                System.out.println(rtc.toString());
            }
        }
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(1));
        System.out.println(sb.toString());
    }
    
    @Test
    public void testIncompleteGrid() throws IOException {
        Page page = UtilsForTesting.getPage("src/test/resources/org/nerdpower/tabula/china.pdf", 1);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertEquals(2, tables.size());
    }
    
    @Test
    public void testNaturalOrderOfRectanglesDoesNotBreakContract() throws IOException {
        Page page = UtilsForTesting.getPage("src/test/resources/org/nerdpower/tabula/us-017.pdf", 2);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(0));
        
        String result = sb.toString();
        String expected = "Project,Agency,Institution\r\nNanotechnology and its publics,NSF,Pennsylvania State University\r\n\"Public information and deliberation in nanoscience and \rnanotechnology policy (SGER)\",Interagency,\"North Carolina State \rUniversity\"\r\n\"Social and ethical research and education in agrifood \rnanotechnology (NIRT)\",NSF,Michigan State University\r\n\"From laboratory to society: developing an informed \rapproach to nanoscale science and engineering (NIRT)\",NSF,University of South Carolina\r\nDatabase and innovation timeline for nanotechnology,NSF,UCLA\r\nSocial and ethical dimensions of nanotechnology,NSF,University of Virginia\r\n\"Undergraduate exploration of nanoscience, \rapplications and societal implications (NUE)\",NSF,\"Michigan Technological \rUniversity\"\r\n\"Ethics and belief inside the development of \rnanotechnology (CAREER)\",NSF,University of Virginia\r\n\"All centers, NNIN and NCN have a societal \rimplications components\",\"NSF, DOE, \rDOD, and NIH\",\"All nanotechnology centers \rand networks\"\r\n";
        
        assertEquals(expected, result);
    }
    
    @Test
    // TODO add assertions
    public void testMergeLinesCloseToEachOther() throws IOException {
        Page page = UtilsForTesting.getPage("src/test/resources/org/nerdpower/tabula/20.pdf", 1);
        List<Ruling> rulings = page.getVerticalRulings();
        for (Ruling ruling : rulings) {
            System.out.println(ruling.getLeft());
        }
        System.out.println(rulings.size());
        
    }
    
    @Test
    public void testSpreadsheetWithNoBoundingFrameShouldBeSpreadsheet() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage("src/test/resources/org/nerdpower/tabula/spreadsheet_no_bounding_frame.pdf", 1,
                140.25f,54.1875f,649.1875f,542.9375f);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        boolean isTabular = se.isTabular(page);
        assertTrue(isTabular);
        List<? extends Table> tables = se.extract(page);
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(0));
        System.out.println(sb.toString());
        assertTrue(false);
    }
    
    @Test
    public void testExtractSpreadsheetWithinAnArea() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage(
                "src/test/resources/org/nerdpower/tabula/puertos1.pdf",
                1,
                273.9035714285714f, 30.32142857142857f, 554.8821428571429f, 546.7964285714286f);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        Table table = tables.get(0);
        assertEquals(15, table.getRows().size());
        
        String expected = "\"\",TM,M.U$S,TM,M.U$S,TM,M.U$S,TM,M.U$S,TM,M.U$S,TM,M.U$S,TM\n" + 
                "Peces vivos,1,25,1,23,2,38,1,37,2,67,2,89,1\n" + 
                "\"Pescado fresco\n" + 
                "o refrigerado.\n" + 
                "exc. filetes\",7.704,7.175,8.931,6.892,12.635,10.255,16.742,13.688,14.357,11.674,13.035,13.429,9.727\n" + 
                "\"Pescado congelado\n" + 
                "exc. filetes\",90.560,105.950,112.645,108.416,132.895,115.874,152.767,133.765,148.882,134.847,156.619,165.134,137.179\n" + 
                "\"Filetes y demás car-\n" + 
                "nes de pescado\",105.434,200.563,151.142,218.389,152.174,227.780,178.123,291.863,169.422,313.735,176.427,381.640,144.814\n" + 
                "\"Pescado sec./sal./\n" + 
                "en salm. har./pol./\n" + 
                "pell. aptos\n" + 
                "p/c humano\",6.837,14.493,6.660,9.167,14.630,17.579,18.150,21.302,18.197,25.739,13.460,23.549,11.709\n" + 
                "Crustáceos,61.691,375.798,52.488,251.043,47.635,387.783,27.815,217.443,7.123,86.019,39.488,373.583,45.191\n" + 
                "Moluscos,162.027,174.507,109.436,111.443,90.834,104.741,57.695,109.141,98.182,206.304,187.023,251.352,157.531\n" + 
                "\"Prod. no exp. en\n" + 
                "otros capítulos.\n" + 
                "No apto p/c humano\",203,328,7,35,521,343,\"1,710\",\"1,568\",125,246,124,263,131\n" + 
                "\"Grasas y aceites de\n" + 
                "pescado y mamíferos\n" + 
                "marinos\",913,297,\"1,250\",476,\"1,031\",521,\"1,019\",642,690,483,489,710,959\n" + 
                "\"Extractos y jugos de\n" + 
                "pescado y mariscos\",5,25,1,3,4,4,31,93,39,117,77,230,80\n" + 
                "\"Preparaciones y con-\n" + 
                "servas de pescado\",846,\"3,737\",\"1,688\",\"4,411\",\"1,556\",\"3,681\",\"2,292\",\"5,474\",\"2,167\",\"7,494\",\"2,591\",\"8,833\",\"2,795\"\n" + 
                "\"Preparaciones y con-\n" + 
                "servas de mariscos\",348,\"3,667\",345,\"1,771\",738,\"3,627\",561,\"2,620\",607,\"3,928\",314,\"2,819\",250\n" + 
                "\"Harina, polvo y pe-\n" + 
                "llets de pescado.No\n" + 
                "aptos p/c humano\",\"16,947\",\"8,547\",\"11,867\",\"6,315\",\"32,528\",\"13,985\",\"37,313\",\"18,989\",\"35,787\",\"19,914\",\"37,821\",\"27,174\",\"30,000\"\n" + 
                "TOTAL,\"453,515\",\"895,111\",\"456,431\",\"718,382\",\"487,183\",\"886,211\",\"494,220\",\"816,623\",\"495,580\",\"810,565\",\"627,469\",\"1,248,804\",\"540,367\"\n";

      
        // TODO add better assertions
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(0));
        String result = sb.toString();

        List<CSVRecord> parsedExpected = org.apache.commons.csv.CSVParser.parse(expected, CSVFormat.EXCEL).getRecords();
        List<CSVRecord> parsedResult = org.apache.commons.csv.CSVParser.parse(result, CSVFormat.EXCEL).getRecords();
      
        assertEquals(parsedResult.size(), parsedExpected.size());
        for (int i = 0; i < parsedResult.size(); i ++) {
            assertEquals(parsedResult.get(i).size(), parsedExpected.get(i).size());
        }
        
    }
    
    @Test
    public void testAlmostIntersectingRulingsShouldIntersect() {
        Ruling v = new Ruling(new Point2D.Float(555.960876f, 271.569641f), new Point2D.Float(555.960876f, 786.899902f));
        Ruling h = new Ruling(new Point2D.Float(25.620499f, 786.899902f), new Point2D.Float(555.960754f, 786.899902f));
        Map<Point2D, Ruling[]> m = Ruling.findIntersections(Arrays.asList(new Ruling[] { h }), Arrays.asList(new Ruling[] { v }));
        assertEquals(m.values().size(), 1);
    }
    
    // TODO add assertions
    @Test
    public void testDontRaiseSortException() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage(
                "src/test/resources/org/nerdpower/tabula/us-017.pdf",
                2,
                446.0f, 97.0f, 685.0f, 520.0f);
        page.getText();
        //BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
    }
}
