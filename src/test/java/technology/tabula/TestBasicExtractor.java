package technology.tabula;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.writers.CSVWriter;

public class TestBasicExtractor {

	private static final String EU_002_PDF = "src/test/resources/technology/tabula/eu-002.pdf";
	private static final String[][] EU_002_EXPECTED = {
		{"",                                              "",                "Involvement of pupils in", ""},
		{"",                                              "Preperation and", "Production of",            "Presentation an"},
		{"",                                              "planing",         "materials",                "evaluation"},
		{"Knowledge and awareness of different cultures", "0,2885",          "0,3974",                   "0,3904"},
		{"Foreign language competence",                   "0,3057",          "0,4184",                   "0,3899"},
		{"Social skills and abilities",                   "0,3416",          "0,3369",                   "0,4303"},
		{"Acquaintance of special knowledge",             "0,2569",          "0,2909",                   "0,3557"},
		{"Self competence",                               "0,3791",          "0,3320",                   "0,4617"}
	};

	private static final String ARGENTINA_DIPUTADOS_VOTING_RECORD_PDF = "src/test/resources/technology/tabula/argentina_diputados_voting_record.pdf";
	private static final String[][] ARGENTINA_DIPUTADOS_VOTING_RECORD_EXPECTED = {
		{"ABDALA de MATARAZZO, Norma Amanda",                 "Frente Cívico por Santiago",   "Santiago del Estero", "AFIRMATIVO"},
		{"ALBRIEU, Oscar Edmundo Nicolas",                    "Frente para la Victoria - PJ", "Rio Negro",           "AFIRMATIVO"},
		{"ALONSO, María Luz",                                 "Frente para la Victoria - PJ", "La Pampa",            "AFIRMATIVO"},
		{"ARENA, Celia Isabel",                               "Frente para la Victoria - PJ", "Santa Fe",            "AFIRMATIVO"},
		{"ARREGUI, Andrés Roberto",                           "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"AVOSCAN, Herman Horacio",                           "Frente para la Victoria - PJ", "Rio Negro",           "AFIRMATIVO"},
		{"BALCEDO, María Ester",                              "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"BARRANDEGUY, Raúl Enrique",                         "Frente para la Victoria - PJ", "Entre Ríos",          "AFIRMATIVO"},
		{"BASTERRA, Luis Eugenio",                            "Frente para la Victoria - PJ", "Formosa",             "AFIRMATIVO"},
		{"BEDANO, Nora Esther",                               "Frente para la Victoria - PJ", "Córdoba",             "AFIRMATIVO"},
		{"BERNAL, María Eugenia",                             "Frente para la Victoria - PJ", "Jujuy",               "AFIRMATIVO"},
		{"BERTONE, Rosana Andrea",                            "Frente para la Victoria - PJ", "Tierra del Fuego",    "AFIRMATIVO"},
		{"BIANCHI, María del Carmen",                         "Frente para la Victoria - PJ", "Cdad. Aut. Bs. As.",  "AFIRMATIVO"},
		{"BIDEGAIN, Gloria Mercedes",                         "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"BRAWER, Mara",                                      "Frente para la Victoria - PJ", "Cdad. Aut. Bs. As.",  "AFIRMATIVO"},
		{"BRILLO, José Ricardo",                              "Movimiento Popular Neuquino",  "Neuquén",             "AFIRMATIVO"},
		{"BROMBERG, Isaac Benjamín",                          "Frente para la Victoria - PJ", "Tucumán",             "AFIRMATIVO"},
		{"BRUE, Daniel Agustín",                              "Frente Cívico por Santiago",   "Santiago del Estero", "AFIRMATIVO"},
		{"CALCAGNO, Eric",                                    "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"CARLOTTO, Remo Gerardo",                            "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"CARMONA, Guillermo Ramón",                          "Frente para la Victoria - PJ", "Mendoza",             "AFIRMATIVO"},
		{"CATALAN MAGNI, Julio César",                        "Frente para la Victoria - PJ", "Tierra del Fuego",    "AFIRMATIVO"},
		{"CEJAS, Jorge Alberto",                              "Frente para la Victoria - PJ", "Rio Negro",           "AFIRMATIVO"},
		{"CHIENO, María Elena",                               "Frente para la Victoria - PJ", "Corrientes",          "AFIRMATIVO"},
		{"CIAMPINI, José Alberto",                            "Frente para la Victoria - PJ", "Neuquén",             "AFIRMATIVO"},
		{"CIGOGNA, Luis Francisco Jorge",                     "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"CLERI, Marcos",                                     "Frente para la Victoria - PJ", "Santa Fe",            "AFIRMATIVO"},
		{"COMELLI, Alicia Marcela",                           "Movimiento Popular Neuquino",  "Neuquén",             "AFIRMATIVO"},
		{"CONTI, Diana Beatriz",                              "Frente para la Victoria - PJ", "Buenos Aires",        "AFIRMATIVO"},
		{"CORDOBA, Stella Maris",                             "Frente para la Victoria - PJ", "Tucumán",             "AFIRMATIVO"},
		{"CURRILEN, Oscar Rubén",                             "Frente para la Victoria - PJ", "Chubut",              "AFIRMATIVO"}
	};

	private static final String EU_017_PDF = "src/test/resources/technology/tabula/eu-017.pdf";
	private static final String[][] EU_017_EXPECTED = {
		{"", "Austria",         "77",  "1",  "78"},
		{"", "Belgium",        "159",  "2", "161"},
		{"", "Bulgaria",        "52",  "0",  "52"},
		{"", "Croatia",        "144",  "0", "144"},
		{"", "Cyprus",          "43",  "2",  "45"},
		{"", "Czech Republic",  "78",  "0",  "78"},
		{"", "Denmark",        "151",  "2", "153"},
		{"", "Estonia",         "46",  "0",  "46"},
		{"", "Finland",        "201",  "1", "202"},
		{"", "France",         "428",  "7", "435"},
		{"", "Germany",        "646", "21", "667"},
		{"", "Greece",         "113",  "2", "115"},
		{"", "Hungary",        "187",  "0", "187"},
		{"", "Iceland",         "18",  "0",  "18"},
		{"", "Ireland",        "213",  "4", "217"},
		{"", "Israel",          "25",  "0",  "25"},
		{"", "Italy",          "627", "12", "639"},
		{"", "Latvia",           "7",  "0",   "7"},
		{"", "Lithuania",       "94",  "1",  "95"},
		{"", "Luxembourg",      "22",  "0",  "22"},
		{"", "Malta",           "18",  "0",  "18"},
		{"", "Netherlands",    "104",  "1", "105"},
		{"", "Norway",         "195",  "0", "195"},
		{"", "Poland",         "120",  "1", "121"},
		{"", "Portugal",       "532",  "3", "535"},
		{"", "Romania",        "110",  "0", "110"},
		{"", "Slovakia",       "176",  "0", "176"},
		{"", "Slovenia",        "56",  "0",  "56"},
		{"", "Spain",          "614",  "3", "617"},
		{"", "Sweden",         "122",  "3", "125"},
		{"", "Switzerland",     "64",  "0",  "64"},
		{"", "Turkey",          "96",  "0",  "96"},
		{"", "United Kingdom", "572", "14", "586"}
	};

	private static final String FRX_2012_DISCLOSURE_PDF = "src/test/resources/technology/tabula/frx_2012_disclosure.pdf";
	private static final String[][] FRX_2012_DISCLOSURE_EXPECTED = {
		{"AANONSEN, DEBORAH, A", "",                           "STATEN ISLAND, NY", "MEALS",                "$85.00"},
		{"TOTAL",                "",                           "",                  "",                     "$85.00"},
		{"AARON, CAREN, T",      "",                           "RICHMOND, VA",      "EDUCATIONAL ITEMS",    "$78.80"},
		{"AARON, CAREN, T",      "",                           "RICHMOND, VA",      "MEALS",               "$392.45"},
		{"TOTAL",                "",                           "",                  "",                    "$471.25"},
		{"AARON, JOHN",          "",                           "CLARKSVILLE, TN",   "MEALS",                "$20.39"},
		{"TOTAL",                "",                           "",                  "",                     "$20.39"},
		{"AARON, JOSHUA, N",     "",                           "WEST GROVE, PA",    "MEALS",               "$310.33"},
		{"",                     "REGIONAL PULMONARY & SLEEP", "",                  "",                           ""},
		{"AARON, JOSHUA, N",     "",                           "WEST GROVE, PA",    "SPEAKING FEES",     "$4,700.00"},
		{"",                     "MEDICINE",                   "",                  "",                           ""},
		{"TOTAL",                "",                           "",                  "",                  "$5,010.33"},
		{"AARON, MAUREEN, M",    "",                           "MARTINSVILLE, VA",  "MEALS",               "$193.67"},
		{"TOTAL",                "",                           "",                  "",                    "$193.67"},
		{"AARON, MICHAEL, L",    "",                           "WEST ISLIP, NY",    "MEALS",                "$19.50"},
		{"TOTAL",                "",                           "",                  "",                     "$19.50"},
		{"AARON, MICHAEL, R",    "",                           "BROOKLYN, NY",      "MEALS",                "$65.92"}
	};

	private static final String[][] EXPECTED_EMPTY_TABLE = { /* actually empty! */ };


    @Test
    public void testRemoveSequentialSpaces() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/m27.pdf", 79.2f, 28.28f, 103.04f, 732.6f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        List<RectangularTextContainer> firstRow = table.getRows().get(0);

        assertTrue(firstRow.get(1).getText().equals("ALLEGIANT AIR"));
        assertTrue(firstRow.get(2).getText().equals("ALLEGIANT AIR LLC"));
    }

    @Test
    public void testColumnRecognition() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage(ARGENTINA_DIPUTADOS_VOTING_RECORD_PDF, 269.875f, 12.75f, 790.5f, 561f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        assertArrayEquals(ARGENTINA_DIPUTADOS_VOTING_RECORD_EXPECTED, UtilsForTesting.tableToArrayOfRows(table));
    }

    @Test
    public void testVerticalRulingsPreventMergingOfColumns() throws IOException {
        List<Ruling> rulings = new ArrayList<>();
        Float[] rulingsVerticalPositions = {147f, 256f, 310f, 375f, 431f, 504f};
        for (int i = 0; i < 6; i++) {
            rulings.add(new Ruling(255.57f, rulingsVerticalPositions[i], 0, 398.76f - 255.57f));
        }

        Page page = UtilsForTesting.getAreaFromFirstPage(
                "src/test/resources/technology/tabula/campaign_donors.pdf",
                255.57f, 40.43f, 398.76f, 557.35f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm(rulings);
        Table table = bea.extract(page).get(0);
        List<RectangularTextContainer> sixthRow = table.getRows().get(5);

        assertTrue(sixthRow.get(0).getText().equals("VALSANGIACOMO BLANC"));
        assertTrue(sixthRow.get(1).getText().equals("OFERNANDO JORGE"));
    }

    @Test
    public void testExtractColumnsCorrectly() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage(EU_002_PDF, 1, 115.0f, 70.0f, 233.0f, 510.0f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        assertArrayEquals(EU_002_EXPECTED, UtilsForTesting.tableToArrayOfRows(table));
    }

    @Test
    public void testExtractColumnsCorrectly2() throws IOException {
        Page page = UtilsForTesting.getPage(EU_017_PDF, 3);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm(page.getVerticalRulings());
        Table table = bea.extract(page.getArea(299.625f, 148.44f, 711.875f, 452.32f)).get(0);
        assertArrayEquals(EU_017_EXPECTED, UtilsForTesting.tableToArrayOfRows(table));
    }

    @Test
    public void testExtractColumnsCorrectly3() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage(FRX_2012_DISCLOSURE_PDF, 106.01f, 48.09f, 227.31f, 551.89f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        assertArrayEquals(FRX_2012_DISCLOSURE_EXPECTED, UtilsForTesting.tableToArrayOfRows(table));
    }

    @Test
    public void testCheckSqueezeDoesntBreak() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage("src/test/resources/technology/tabula/12s0324.pdf",
                99.0f, 17.25f, 316.5f, 410.25f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        List<List<RectangularTextContainer>> rows = table.getRows();
        List<RectangularTextContainer> firstRow = rows.get(0);
        List<RectangularTextContainer> lastRow = rows.get(rows.size() - 1);
        assertTrue(firstRow.get(0).getText().equals("Violent crime  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  ."));
        assertTrue(lastRow.get(lastRow.size() - 1).getText().equals("(X)"));
    }

    @Test
    public void testNaturalOrderOfRectangles() throws IOException {
        Page page = UtilsForTesting.getPage(
                "src/test/resources/technology/tabula/us-017.pdf", 2)
                .getArea(446.0f, 97.0f, 685.0f, 520.0f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm(
                page.getVerticalRulings());
        Table table = bea.extract(page).get(0);

        List<RectangularTextContainer> cells = new ArrayList<>(table.cells.values());
        for (RectangularTextContainer rectangularTextContainer : cells) {
            System.out.println(rectangularTextContainer.getText());
        }
        //Column headers
        assertEquals("Project", cells.get(0).getText());
        assertEquals("Agency", cells.get(1).getText());
        assertEquals("Institution", cells.get(2).getText());

        //First row
        assertEquals("Nanotechnology and its publics", cells.get(3).getText());
        assertEquals("NSF", cells.get(4).getText());
        assertEquals("Pennsylvania State Universit", cells.get(5).getText());

        //Second row
        assertEquals("Public information and deliberation in nanoscience and", cells.get(6).getText());
        assertEquals("North Carolina State", cells.get(7).getText());
        assertEquals("Interagency", cells.get(8).getText());
        assertEquals("nanotechnology policy (SGER)", cells.get(9).getText());
        assertEquals("University", cells.get(10).getText());

        //Third row
        assertEquals("Social and ethical research and education in agrifood", cells.get(11).getText());
        assertEquals("NSF", cells.get(12).getText());
        assertEquals("Michigan State University", cells.get(13).getText());
        assertEquals("nanotechnology (NIRT)", cells.get(14).getText());

        //Fourth row
        assertEquals("From laboratory to society: developing an informed", cells.get(15).getText());
        assertEquals("NSF", cells.get(16).getText());
        assertEquals("University of South Carolina", cells.get(17).getText());
        assertEquals("approach to nanoscale science and engineering (NIRT)", cells.get(18).getText());

        //Fifth row
        assertEquals("Database and innovation timeline for nanotechnology", cells.get(19).getText());
        assertEquals("NSF", cells.get(20).getText());
        assertEquals("UCLA", cells.get(21).getText());

        //Sixth row
        assertEquals("Social and ethical dimensions of nanotechnology", cells.get(22).getText());
        assertEquals("NSF", cells.get(23).getText());
        assertEquals("University of Virginia", cells.get(24).getText());

        //Seventh row
        assertEquals("Undergraduate exploration of nanoscience,", cells.get(25).getText());
        assertEquals("Michigan Technological", cells.get(26).getText());
        assertEquals("NSF", cells.get(27).getText());
        assertEquals("applications and societal implications (NUE)", cells.get(28).getText());
        assertEquals("University", cells.get(29).getText());

        //Eighth row
        assertEquals("Ethics and belief inside the development of", cells.get(30).getText());
        assertEquals("NSF", cells.get(31).getText());
        assertEquals("University of Virginia", cells.get(32).getText());
        assertEquals("nanotechnology (CAREER)", cells.get(33).getText());

        //Ninth row
        assertEquals("All centers, NNIN and NCN have a societal", cells.get(34).getText());
        assertEquals("NSF, DOE,", cells.get(35).getText());
        assertEquals("All nanotechnology centers", cells.get(36).getText());
        assertEquals("implications components", cells.get(37).getText());
        assertEquals("DOD, and NIH", cells.get(38).getText());
        assertEquals("and networks", cells.get(39).getText());

    }

    @Test
    public void testNaturalOrderOfRectanglesOneMoreTime() throws IOException {
        CSVParser parse = org.apache.commons.csv.CSVParser.parse(new File("src/test/resources/technology/tabula/csv/TestBasicExtractor-RECTANGLE_TEST_NATURAL_ORDER.csv"),
                Charset.forName("utf-8"),
                CSVFormat.DEFAULT);

        List<Rectangle> rectangles = new ArrayList<>();

        for (CSVRecord record : parse) {
            rectangles.add(new Rectangle(Float.parseFloat(record.get(0)),
                    Float.parseFloat(record.get(1)),
                    Float.parseFloat(record.get(2)),
                    Float.parseFloat(record.get(3))));
        }


        //List<Rectangle> rectangles = Arrays.asList(RECTANGLES_TEST_NATURAL_ORDER);
        Utils.sort(rectangles, Rectangle.ILL_DEFINED_ORDER);

        for (int i = 0; i < (rectangles.size() - 1); i++) {
            Rectangle rectangle = rectangles.get(i);
            Rectangle nextRectangle = rectangles.get(i + 1);
            
            assertTrue(rectangle.compareTo(nextRectangle) < 0);
        }
    }

    @Test
    public void testRealLifeRTL2() throws IOException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/indictb1h_14.csv");
        Page page = UtilsForTesting.getAreaFromPage("src/test/resources/technology/tabula/indictb1h_14.pdf", 1,
                205.0f, 120.0f, 622.82f, 459.9f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        assertEquals(expectedCsv, sb.toString());
    }


    @Test
    public void testEmptyRegion() throws IOException {
        Page page = UtilsForTesting.getAreaFromPage("src/test/resources/technology/tabula/indictb1h_14.pdf", 1, 0, 0, 80.82f, 100.9f); // an empty area
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        assertArrayEquals(EXPECTED_EMPTY_TABLE, UtilsForTesting.tableToArrayOfRows(table));
    }


    @Test
    public void testTableWithMultilineHeader() throws IOException {
        String expectedCsv = UtilsForTesting.loadCsv("src/test/resources/technology/tabula/csv/us-020.csv");
        Page page = UtilsForTesting.getAreaFromPage("src/test/resources/technology/tabula/us-020.pdf", 2, 103.0f, 35.0f, 641.0f, 560.0f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);

        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        assertEquals(expectedCsv, sb.toString());
    }

}
