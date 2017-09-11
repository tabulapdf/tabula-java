package technology.tabula;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;
import technology.tabula.writers.JSONWriter;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.util.Arrays.array;
import static technology.tabula.UtilsForTesting.*;

public class SpreadsheetExtractorTest {


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
            new Cell(74.0f, 70.0f, 156.0f, 6.0f)};

    private static final Ruling[][] SINGLE_CELL_RULINGS = { {
                new Ruling(new Point2D.Float(151.653545f, 185.66929f), new Point2D.Float(380.73438f, 185.66929f)),
                new Ruling(new Point2D.Float(151.653545f, 314.64567f), new Point2D.Float(380.73438f, 314.64567f))
            }, {
                new Ruling(new Point2D.Float(151.653545f, 185.66929f), new Point2D.Float(151.653545f, 314.64567f)),
                new Ruling(new Point2D.Float(380.73438f, 185.66929f), new Point2D.Float(380.73438f, 314.64567f))
            }
    };

    private static final Ruling[][] TWO_SINGLE_CELL_RULINGS = { {
                new Ruling(new Point2D.Float(151.653545f, 185.66929f), new Point2D.Float(287.4074f, 185.66929f)),
                new Ruling(new Point2D.Float(151.653545f, 262.101f), new Point2D.Float(287.4074f, 262.101f)),
                new Ruling(new Point2D.Float(232.44095f, 280.62992f), new Point2D.Float(368.1948f, 280.62992f)),
                new Ruling(new Point2D.Float(232.44095f, 357.06164f), new Point2D.Float(368.1948f, 357.06164f))
            }, {
                new Ruling(new Point2D.Float(151.653545f, 185.66929f), new Point2D.Float(151.653545f, 262.101f)),
                new Ruling(new Point2D.Float(287.4074f, 185.66929f), new Point2D.Float(287.4074f, 262.101f)),
                new Ruling(new Point2D.Float(232.44095f, 280.62992f), new Point2D.Float(232.44095f, 357.06164f)),
                new Ruling(new Point2D.Float(368.1948f, 280.62992f), new Point2D.Float(368.1948f, 357.06164f))
            }
    };

    private static final Ruling[] EXTERNALLY_DEFINED_RULINGS = {
            new Ruling(new Point2D.Float(320.0f, 285.0f), new Point2D.Float(564.4409f, 285.0f)),
            new Ruling(new Point2D.Float(320.0f, 457.0f), new Point2D.Float(564.4409f, 457.0f)),
            new Ruling(new Point2D.Float(320.0f, 331.0f), new Point2D.Float(564.4409f, 331.0f)),
            new Ruling(new Point2D.Float(320.0f, 315.0f), new Point2D.Float(564.4409f, 315.0f)),
            new Ruling(new Point2D.Float(320.0f, 347.0f), new Point2D.Float(564.4409f, 347.0f)),
            new Ruling(new Point2D.Float(320.0f, 363.0f), new Point2D.Float(564.44088f, 363.0f)),
            new Ruling(new Point2D.Float(320.0f, 379.0f), new Point2D.Float(564.44087f, 379.0f)),
            new Ruling(new Point2D.Float(320.0f, 395.5f), new Point2D.Float(564.44086f, 395.5f)),
            new Ruling(new Point2D.Float(320.00006f, 415.0f), new Point2D.Float(564.4409f, 415.0f)),
            new Ruling(new Point2D.Float(320.00007f, 431.0f), new Point2D.Float(564.4409f, 431.0f)),

            new Ruling(new Point2D.Float(320.0f, 285.0f), new Point2D.Float(320.0f, 457.0f)),
            new Ruling(new Point2D.Float(565.0f, 285.0f), new Point2D.Float(564.4409f, 457.0f)),
            new Ruling(new Point2D.Float(470.5542f, 285.0f), new Point2D.Float(470.36865f, 457.0f))
    };

    private static final Ruling[] EXTERNALLY_DEFINED_RULINGS2 = {
            new Ruling(new Point2D.Float(51.796964f, 180.0f), new Point2D.Float(560.20312f, 180.0f)),
            new Ruling(new Point2D.Float(51.797017f, 219.0f), new Point2D.Float(560.2031f, 219.0f)),
            new Ruling(new Point2D.Float(51.797f, 239.0f), new Point2D.Float(560.2031f, 239.0f)),
            new Ruling(new Point2D.Float(51.797f, 262.0f), new Point2D.Float(560.20312f, 262.0f)),
            new Ruling(new Point2D.Float(51.797f, 283.50247f), new Point2D.Float(560.05024f, 283.50247f)),
            new Ruling(new Point2D.Float(51.796964f, 309.0f), new Point2D.Float(560.20312f, 309.0f)),
            new Ruling(new Point2D.Float(51.796982f, 333.0f), new Point2D.Float(560.20312f, 333.0f)),
            new Ruling(new Point2D.Float(51.797f, 366.0f), new Point2D.Float(560.20312f, 366.0f)),

            new Ruling(new Point2D.Float(52.0f, 181.0f), new Point2D.Float(51.797f, 366.0f)),
            new Ruling(new Point2D.Float(208.62891f, 181.0f), new Point2D.Float(208.62891f, 366.0f)),
            new Ruling(new Point2D.Float(357.11328f, 180.0f), new Point2D.Float(357.0f, 366.0f)),
            new Ruling(new Point2D.Float(560.11328f, 180.0f), new Point2D.Float(560.0f, 366.0f))
    };

    @Test
    public void testLinesToCells() {
        List<Cell> cells = SpreadsheetExtractionAlgorithm.findCells(Arrays.asList(HORIZONTAL_RULING_LINES), Arrays.asList(VERTICAL_RULING_LINES));
        Collections.sort(cells);
        List<Cell> expected = Arrays.asList(EXPECTED_CELLS);
        Collections.sort(expected);

        assertThat(cells).isEqualTo(expected);
    }

    @Test
    public void testDetectSingleCell() {
        List<Cell> cells = SpreadsheetExtractionAlgorithm.findCells(Arrays.asList(SINGLE_CELL_RULINGS[0]), Arrays.asList(SINGLE_CELL_RULINGS[1]));
        assertThat(cells).hasSize(1);
        Cell cell = cells.get(0);

        assertThat(cell.getLeft()).isCloseTo(151.65355f, within(0.01f));
        assertThat(cell.getTop()).isCloseTo(185.6693f, within(0.01f));
        assertThat(cell.getWidth()).isCloseTo(229.08083, within(0.01d));
        assertThat(cell.getHeight()).isCloseTo(128.97636, within(0.01d));
    }

    @Test
    public void testDetectTwoSingleCells() {
        List<Cell> cells = SpreadsheetExtractionAlgorithm.findCells(Arrays.asList(TWO_SINGLE_CELL_RULINGS[0]), Arrays.asList(TWO_SINGLE_CELL_RULINGS[1]));
        assertThat(cells).hasSize(2);
        // should not overlap
        assertThat(cells.get(0).intersects(cells.get(1))).isFalse();
    }

    @Test
    public void testFindSpreadsheetsFromCells() throws IOException {

        CSVParser parse = CSVParser.parse(new File("src/test/resources/technology/tabula/csv/SpreadsheetExtractorTest-CELLS.csv"),
                Charset.forName("utf-8"), CSVFormat.DEFAULT);

        List<Cell> cells = new ArrayList<>();

        for (CSVRecord record : parse) {
            cells.add(new Cell(Float.parseFloat(record.get(0)),
                    Float.parseFloat(record.get(1)),
                    Float.parseFloat(record.get(2)),
                    Float.parseFloat(record.get(3))));
        }

        List<Rectangle> expected = Arrays.asList(EXPECTED_RECTANGLES);
        Collections.sort(expected);
        List<? extends Rectangle> foundRectangles = SpreadsheetExtractionAlgorithm.findSpreadsheetsFromCells(cells);
        Collections.sort(foundRectangles);
        assertThat(foundRectangles.equals(expected)).isTrue();
    }

    // TODO Add assertions
    @Test
    public void testSpreadsheetExtraction() throws IOException {
        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/argentina_diputados_voting_record.pdf",
                        269.875f, 12.75f, 790.5f, 561f);

        SpreadsheetExtractionAlgorithm.findCells(page.getHorizontalRulings(), page.getVerticalRulings());
    }

    @Test
    public void testSpanningCells() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/spanning_cells.pdf", 1);
        String expectedJson = UtilsForTesting.loadTextFile("src/test/resources/technology/tabula/json/spanning_cells.json");
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertThat(tables).hasSize(2);

        StringBuilder sb = new StringBuilder();
        (new JSONWriter()).write(sb, tables);
        assertThat(sb.toString()).isEqualTo(expectedJson);

    }

    @Test
    public void testSpanningCellsToCsv() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/spanning_cells.pdf", 1);
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/spanning_cells.csv");
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertThat(tables).hasSize(2);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables);
        assertThat(sb.toString()).isEqualTo(expectedCsv);
    }

    @Test
    public void testIncompleteGrid() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/china.pdf", 1);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        assertThat(tables).hasSize(2);
    }

    @Test
    public void testNaturalOrderOfRectanglesDoesNotBreakContract() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/us-017.pdf", 2);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables.get(0));

        String result = sb.toString();
        String expected = "Project,Agency,Institution\r\n" +
                "Nanotechnology and its publics,NSF,Pennsylvania State University\r\n" +
                "\"Public information and deliberation in nanoscience and\rnanotechnology policy (SGER)\",Interagency,\"North Carolina State\rUniversity\"\r\n" +
                "\"Social and ethical research and education in agrifood\rnanotechnology (NIRT)\",NSF,Michigan State University\r\n" +
                "\"From laboratory to society: developing an informed\rapproach to nanoscale science and engineering (NIRT)\",NSF,University of South Carolina\r\n" +
                "Database and innovation timeline for nanotechnology,NSF,UCLA\r\n" +
                "Social and ethical dimensions of nanotechnology,NSF,University of Virginia\r\n" +
                "\"Undergraduate exploration of nanoscience,\rapplications and societal implications (NUE)\",NSF,\"Michigan Technological\rUniversity\"\r\n" +
                "\"Ethics and belief inside the development of\rnanotechnology (CAREER)\",NSF,University of Virginia\r\n" +
                "\"All centers, NNIN and NCN have a societal\rimplications components\",\"NSF, DOE,\rDOD, and NIH\",\"All nanotechnology centers\rand networks\"\r\n";

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testMergeLinesCloseToEachOther() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/20.pdf", 1);
        List<Ruling> rulings = page.getVerticalRulings();
        float[] expectedRulings = new float[]{105.549774f, 107.52332f, 160.58167f, 377.1792f, 434.95804f, 488.21783f};
        for (int i = 0; i < rulings.size(); i++) {
            assertThat(rulings.get(i).getLeft()).isCloseTo(expectedRulings[i], within(0.1f));
        }
        assertThat(rulings).hasSize(6);
    }

    @Test
    public void testSpreadsheetWithNoBoundingFrameShouldBeSpreadsheet() throws IOException {
        Page page = getAreaFromPage("src/test/resources/technology/tabula/spreadsheet_no_bounding_frame.pdf",
                1,150.56f, 58.9f, 654.7f, 536.12f);

        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/spreadsheet_no_bounding_frame.csv");

        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        boolean isTabular = se.isTabular(page);
        assertThat(isTabular).isTrue();
        List<? extends Table> tables = se.extract(page);
        String s = CSVWriter.writeString(tables.get(0));

        for (int i = 0; i < expectedCsv.length(); i++) {
            assertThat(s.charAt(i)).as("check char at [%s]", i).isEqualTo(expectedCsv.charAt(i));
        }
        assertThat(s).isEqualTo(expectedCsv);

    }

    @Test
    public void testExtractSpreadsheetWithinAnArea() throws IOException {
        Page page = getAreaFromPage("src/test/resources/technology/tabula/puertos1.pdf", 1,
                273.9035714285714f, 30.32142857142857f, 554.8821428571429f, 546.7964285714286f);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);
        Table table = tables.get(0);
        assertThat(table.getRows()).hasSize(15);

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

        List<CSVRecord> parsedExpected = CSVParser.parse(expected, CSVFormat.EXCEL).getRecords();
        List<CSVRecord> parsedResult = CSVParser.parse(result, CSVFormat.EXCEL).getRecords();

        assertThat(parsedExpected).hasSize(parsedResult.size());
        for (int i = 0; i < parsedResult.size(); i++) {
            assertThat(parsedExpected.get(i)).hasSize(parsedResult.get(i).size());
        }
        // TODO check
        assertThat(parsedResult).isEqualTo (array(parsedExpected));
    }

    @Test
    public void testAlmostIntersectingRulingsShouldIntersect() {
        Ruling v = new Ruling(new Point2D.Float(555.960876f, 271.569641f), new Point2D.Float(555.960876f, 786.899902f));
        Ruling h = new Ruling(new Point2D.Float(25.620499f, 786.899902f), new Point2D.Float(555.960754f, 786.899902f));
        Map<Point2D, Ruling[]> m = Ruling.findIntersections(Collections.singletonList(h), Collections.singletonList(v));

        assertThat(m.values()).hasSize(1);
    }

    // TODO add assertions
    @Test
    public void testDontRaiseSortException() throws IOException {
        Page page = getAreaFromPage("src/test/resources/technology/tabula/us-017.pdf", 2, 446.0f, 97.0f, 685.0f, 520.0f);

        assertThat(page.getText()).isNotNull();
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        Table table = bea.extract(page).get(0);

        assertThat(table).isNotNull();
    }

    @Test
    public void testShouldDetectASingleSpreadsheet() throws IOException {
        Page page = getAreaFromPage("src/test/resources/technology/tabula/offense.pdf", 1, 68.08f, 16.44f, 680.85f, 597.84f);
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = bea.extract(page);
        assertThat(tables).hasSize(1);
    }

    @Test
    public void testExtractTableWithExternallyDefinedRulings() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/us-007.pdf",1);
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = bea.extract(page, Arrays.asList(EXTERNALLY_DEFINED_RULINGS));
        assertThat(tables).hasSize(1);
        Table table = tables.get(0);

        assertThat(table.getRows().get(0).get(0).getText()).isEqualTo("Payroll Period");
        assertThat(table.getRows().get(0).get(1).getText()).isEqualTo("One Withholding\rAllowance");
        assertThat(table.getRows().get(1).get(0).getText()).isEqualTo("Weekly");
        assertThat(table.getRows().get(1).get(1).getText()).isEqualTo("$71.15");
        assertThat(table.getRows().get(2).get(0).getText()).isEqualTo("Biweekly");
        assertThat(table.getRows().get(2).get(1).getText()).isEqualTo("142.31");
        assertThat(table.getRows().get(3).get(0).getText()).isEqualTo("Semimonthly");
        assertThat(table.getRows().get(3).get(1).getText()).isEqualTo("154.17");
        assertThat(table.getRows().get(4).get(0).getText()).isEqualTo("Monthly");
        assertThat(table.getRows().get(4).get(1).getText()).isEqualTo("308.33");
        assertThat(table.getRows().get(5).get(0).getText()).isEqualTo("Quarterly");
        assertThat(table.getRows().get(5).get(1).getText()).isEqualTo("925.00");
        assertThat(table.getRows().get(6).get(0).getText()).isEqualTo("Semiannually");
        assertThat(table.getRows().get(6).get(1).getText()).isEqualTo("1,850.00");
        assertThat(table.getRows().get(7).get(0).getText()).isEqualTo("Annually");
        assertThat(table.getRows().get(7).get(1).getText()).isEqualTo("3,700.00");
        assertThat(table.getRows().get(8).get(0).getText()).isEqualTo("Daily or Miscellaneous\r(each day of the payroll period)");
        assertThat(table.getRows().get(8).get(1).getText()).isEqualTo("14.23");

    }

    @Test
    public void testAnotherExtractTableWithExternallyDefinedRulings() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/us-024.pdf",1);
        SpreadsheetExtractionAlgorithm bea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = bea.extract(page, Arrays.asList(EXTERNALLY_DEFINED_RULINGS2));
        assertThat(tables).hasSize(1);
        Table table = tables.get(0);

        assertThat(table.getRows().get(4).get(0).getText()).isEqualTo("Total Supply");
        assertThat(table.getRows().get(6).get(2).getText()).isEqualTo("6.6");
    }

    @Test
    public void testSpreadsheetsSortedByTopAndRight() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/sydney_disclosure_contract.pdf", 1);

        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        for (int i = 1; i < tables.size(); i++) {
            assertThat(tables.get(i - 1).getTop()).isLessThanOrEqualTo(tables.get(i).getTop());
        }
    }

    @Test
    public void testDontStackOverflowQuicksort() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/failing_sort.pdf", 1);

        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        for (int i = 1; i < tables.size(); i++) {
            assertThat(tables.get(i - 1).getTop()).isLessThanOrEqualTo(tables.get(i).getTop());
        }
    }

    @Test
    public void testRTL() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/arabic.pdf", 1);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        // assertThat(tables).hasSize(1);
        Table table = tables.get(0);

        assertThat(table.getRows().get(1).get(1).getText()).isEqualTo("اسمي سلطان");
        assertThat(table.getRows().get(2).get(1).getText()).isEqualTo("من اين انت؟");
        assertThat(table.getRows().get(3).get(0).getText()).isEqualTo("1234");
        assertThat(table.getRows().get(4).get(0).getText()).isEqualTo("هل انت شباك؟");
        assertThat(table.getRows().get(2).get(0).getText()).isEqualTo("انا من ولاية كارولينا الشمال"); // conjoined lam-alif gets missed
        assertThat(table.getRows().get(4).get(1).getText()).isEqualTo("اسمي Jeremy في الانجليزية"); // conjoined lam-alif gets missed
        assertThat(table.getRows().get(3).get(1).getText()).isEqualTo("عندي 47 قطط"); // the real right answer is 47.
        assertThat(table.getRows().get(5).get(0).getText()).isEqualTo("Jeremy is جرمي in Arabic"); // the real right answer is 47.
        assertThat(table.getRows().get(1).get(0).getText()).isEqualTo("مرحباً"); // really ought to be ً, but this is forgiveable for now

        // there is one remaining problems that are not yet addressed
        // - diacritics (e.g. Arabic's tanwinً and probably Hebrew nekudot) are put in the wrong place.
        // this should get fixed, but this is a good first stab at the problem.

        // these (commented-out) tests reflect the theoretical correct answer,
        // which is not currently possible because of the two problems listed above
        // assertThat(table.getRows().get(0).get(0).getText()).isEqualTo("مرحباً"); // really ought to be ً, but this is forgiveable for now
    }

    @Test
    public void testRealLifeRTL() throws IOException {
        Page page = getPage("src/test/resources/technology/tabula/mednine.pdf", 1);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        // assertThat(tables).hasSize(1);
        Table table = tables.get(0);

        assertThat(table.getRows().get(0).get(0).getText()).isEqualTo("الانتخابات التشريعية  2014"); // the doubled spaces might be a bug in my implementation.
        assertThat(table.getRows().get(1).get(0).getText()).isEqualTo("ورقة كشف نتائج دائرة مدنين");
        assertThat(table.getRows().get(4).get(0).getText()).isEqualTo("426");
        assertThat(table.getRows().get(4).get(1).getText()).isEqualTo("63");
        assertThat(table.getRows().get(4).get(2).getText()).isEqualTo("43");
        assertThat(table.getRows().get(4).get(3).getText()).isEqualTo("56");
        assertThat(table.getRows().get(4).get(4).getText()).isEqualTo("58");
        assertThat(table.getRows().get(4).get(5).getText()).isEqualTo("49");
        assertThat(table.getRows().get(4).get(6).getText()).isEqualTo("55");
        assertThat(table.getRows().get(4).get(7).getText()).isEqualTo("33");
        assertThat(table.getRows().get(4).get(8).getText()).isEqualTo("32");
        assertThat(table.getRows().get(4).get(9).getText()).isEqualTo("37");
        assertThat(table.getRows().get(4).get(10).getText()).isEqualTo("قائمة من أجل تحقيق سلطة الشعب");

        // there is one remaining problems that are not yet addressed
        // - diacritics (e.g. Arabic's tanwinً and probably Hebrew nekudot) are put in the wrong place.
        // this should get fixed, but this is a good first stab at the problem.

        // these (commented-out) tests reflect the theoretical correct answer,
        // which is not currently possible because of the two problems listed above
        // assertThat(table.getRows().get(0).get(0).getText()).isEqualTo("مرحباً"); // really ought to be ً, but this is forgiveable for now
    }

    @Test
    public void testExtractColumnsCorrectly3() throws IOException {

        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/frx_2012_disclosure.pdf", 106.01f, 48.09f, 227.31f, 551.89f);
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        Table table = sea.extract(page).get(0);

        assertThat(table.getRows().get(8).get(1).getText()).isEqualTo("REGIONAL PULMONARY & SLEEP\rMEDICINE");
    }

    @Test
    public void testSpreadsheetExtractionIssue656() throws IOException {
        Page page = getAreaFromFirstPage("src/test/resources/technology/tabula/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.pdf",
                        56.925f,24.255f,549.945f,786.555f);
        String expectedCsv = loadNormalizedCsv("src/test/resources/technology/tabula/csv/Publication_of_award_of_Bids_for_Transport_Sector__August_2016.csv");

        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        assertThat(tables).hasSize(1);
        Table table = tables.get(0);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        String result = sb.toString();
        assertThat(result).isEqualTo(expectedCsv);
    }
}
