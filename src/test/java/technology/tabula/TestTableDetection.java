package technology.tabula;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.detectors.NurminenDetectionAlgorithm;

/**
 * Created by matt on 2015-12-14.
 */
@RunWith(Parameterized.class)
public class TestTableDetection {

    private static int numTests = 0;
    private static int numPassingTests = 0;
    private static int totalExpectedTables = 0;
    private static int totalCorrectlyDetectedTables = 0;
    private static int totalErroneouslyDetectedTables = 0;

    private static Level defaultLogLevel;

    private static final class TestStatus {
        public int numExpectedTables;
        public int numCorrectlyDetectedTables;
        public int numErroneouslyDetectedTables;
        public boolean expectedFailure;

        private transient boolean firstRun;
        private transient String pdfFilename;

        public TestStatus(String pdfFilename) {
            this.numExpectedTables = 0;
            this.numCorrectlyDetectedTables = 0;
            this.expectedFailure = false;
            this.pdfFilename = pdfFilename;
        }

        public static TestStatus load(String pdfFilename) {
            TestStatus status;

            try {
                String json = UtilsForTesting.loadJson(jsonFilename(pdfFilename));
                status = new Gson().fromJson(json, TestStatus.class);
                status.pdfFilename = pdfFilename;
            } catch (IOException ioe) {
                status = new TestStatus(pdfFilename);
                status.firstRun = true;
            }

            return status;
        }

        public void save() {
            try (FileWriter w = new FileWriter(jsonFilename(this.pdfFilename))) {
                Gson gson = new Gson();
                w.write(gson.toJson(this));
                w.close();
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        public boolean isFirstRun() {
            return this.firstRun;
        }

        private static String jsonFilename(String pdfFilename) {
            return pdfFilename.replace(".pdf", ".json");
        }
    }

    @BeforeClass
    public static void disableLogging() {
        Logger pdfboxLogger = Logger.getLogger("org.apache.pdfbox");
        defaultLogLevel = pdfboxLogger.getLevel();
        pdfboxLogger.setLevel(Level.OFF);
    }

    @AfterClass
    public static void enableLogging() {
        Logger.getLogger("org.apache.pdfbox").setLevel(defaultLogLevel);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String[] regionCodes = {"eu", "us"};

        ArrayList<Object[]> data = new ArrayList<>();

        for (String regionCode : regionCodes) {
            String directoryName = "src/test/resources/technology/tabula/icdar2013-dataset/competition-dataset-" + regionCode + "/";
            File dir = new File(directoryName);

            File[] pdfs = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".pdf");
                }
            });

            for (File pdf : pdfs) {
                data.add(new Object[] {pdf});
            }
        }

        return data;
    }

    private File pdf;
    private DocumentBuilder builder;
    private TestStatus status;

    private int numCorrectlyDetectedTables = 0;
    private int numErroneouslyDetectedTables = 0;

    public TestTableDetection(File pdf) {
        this.pdf = pdf;
        this.status = TestStatus.load(pdf.getAbsolutePath());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            this.builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            // ignored
        }
    }

    private void printTables(Map<Integer, List<Rectangle>> tables) {
        for (Integer page : tables.keySet()) {
            System.out.println("Page " + page.toString());
            for (Rectangle table : tables.get(page)) {
                System.out.println(table);
            }
        }
    }

    @Test
    public void testDetectionOfTables() throws Exception {
        numTests++;

        // xml parsing stuff for ground truth
        Document regionDocument = this.builder.parse(this.pdf.getAbsolutePath().replace(".pdf", "-reg.xml"));
        NodeList tables = regionDocument.getElementsByTagName("table");

        // tabula extractors
        PDDocument pdfDocument = PDDocument.load(this.pdf);
        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);

        // parse expected tables from the ground truth dataset
        Map<Integer, List<Rectangle>> expectedTables = new HashMap<>();

        int numExpectedTables = 0;

        for (int i=0; i<tables.getLength(); i++) {

            Element table = (Element) tables.item(i);
            Element region = (Element) table.getElementsByTagName("region").item(0);
            Element boundingBox = (Element) region.getElementsByTagName("bounding-box").item(0);

            // we want to know where tables appear in the document - save the page and areas where tables appear
            Integer page = Integer.decode(region.getAttribute("page"));
            float x1 = Float.parseFloat(boundingBox.getAttribute("x1"));
            float y1 = Float.parseFloat(boundingBox.getAttribute("y1"));
            float x2 = Float.parseFloat(boundingBox.getAttribute("x2"));
            float y2 = Float.parseFloat(boundingBox.getAttribute("y2"));

            List<Rectangle> pageTables = expectedTables.get(page);
            if (pageTables == null) {
                pageTables = new ArrayList<>();
                expectedTables.put(page, pageTables);
            }

            // have to invert y co-ordinates
            // unfortunately the ground truth doesn't contain page dimensions
            // do some extra work to extract the page with tabula and get the dimensions from there
            Page extractedPage = extractor.extractPage(page);

            float top = (float)extractedPage.getHeight() - y2;
            float left = x1;
            float width = x2 - x1;
            float height = y2 - y1;

            pageTables.add(new Rectangle(top, left, width, height));
            numExpectedTables++;
        }

        // now find tables detected by tabula-java
        Map<Integer, List<Rectangle>> detectedTables = new HashMap<>();

        // the algorithm we're going to be testing
        NurminenDetectionAlgorithm detectionAlgorithm = new NurminenDetectionAlgorithm();

        PageIterator pages = extractor.extract();
        while (pages.hasNext()) {
            Page page = pages.next();
            List<Rectangle> tablesOnPage = detectionAlgorithm.detect(page);
            if (tablesOnPage.size() > 0) {
                detectedTables.put(new Integer(page.getPageNumber()), tablesOnPage);
            }
        }

        // now compare
        System.out.println("Testing " + this.pdf.getName());

        List<String> errors = new ArrayList<>();
        this.status.numExpectedTables = numExpectedTables;
        totalExpectedTables += numExpectedTables;

        for (Integer page : expectedTables.keySet()) {
            List<Rectangle> expectedPageTables = expectedTables.get(page);
            List<Rectangle> detectedPageTables = detectedTables.get(page);

            if (detectedPageTables == null) {
                errors.add("Page " + page.toString() + ": " + expectedPageTables.size() + " expected tables not found");
                continue;
            }

            errors.addAll(this.comparePages(page, detectedPageTables, expectedPageTables));

            detectedTables.remove(page);
        }

        // leftover pages means we detected extra tables
        for (Integer page : detectedTables.keySet()) {
            List<Rectangle> detectedPageTables = detectedTables.get(page);
            errors.add("Page " + page.toString() + ": " + detectedPageTables.size() + " tables detected where there are none");

            this.numErroneouslyDetectedTables += detectedPageTables.size();
            totalErroneouslyDetectedTables += detectedPageTables.size();
        }

        boolean failed = errors.size() > 0;

        if (failed) {
            System.out.println("==== CURRENT TEST ERRORS ====");
            for (String error : errors) {
                System.out.println(error);
            }
        } else {
            numPassingTests++;
        }

        System.out.println("==== CUMULATIVE TEST STATISTICS ====");

        System.out.println(numPassingTests + " out of " + numTests + " currently passing");
        System.out.println(totalCorrectlyDetectedTables + " out of " + totalExpectedTables + " expected tables detected");
        System.out.println(totalErroneouslyDetectedTables + " tables incorrectly detected");


        if(this.status.isFirstRun()) {
            // make the baseline
            this.status.expectedFailure = failed;
            this.status.numCorrectlyDetectedTables = this.numCorrectlyDetectedTables;
            this.status.numErroneouslyDetectedTables = this.numErroneouslyDetectedTables;
            this.status.save();
        } else {
            // compare to baseline
            if (this.status.expectedFailure) {
                // make sure the failure didn't get worse
                assertTrue("This test is an expected failure, but it now detects even fewer tables.", this.numCorrectlyDetectedTables >= this.status.numCorrectlyDetectedTables);
                assertTrue("This test is an expected failure, but it now detects more bad tables.", this.numErroneouslyDetectedTables <= this.status.numErroneouslyDetectedTables);
                assertTrue("This test used to fail but now it passes! Hooray! Please update the test's JSON file accordingly.", failed);
            } else {
                assertFalse("Table detection failed. Please see the error messages for more information.", failed);
            }
        }
    }

    private List<String> comparePages(Integer page, List<Rectangle> detected, List<Rectangle> expected) {
        ArrayList<String> errors = new ArrayList<>();

        // go through the detected tables and try to match them with expected tables
        // from http://www.orsigiorgio.net/wp-content/papercite-data/pdf/gho*12.pdf (comparing regions):
        // for other (e.g.“black-box”) algorithms, bounding boxes and content are used. A region is correct if it
        // contains the minimal bounding box of the ground truth without intersecting additional content.
        for (Iterator<Rectangle> detectedIterator = detected.iterator(); detectedIterator.hasNext();) {
            Rectangle detectedTable = detectedIterator.next();

            for (int i=0; i<expected.size(); i++) {
                if (detectedTable.contains(expected.get(i))) {
                    // we have a candidate for the detected table, make sure it doesn't intersect any others
                    boolean intersectsOthers = false;
                    for (int j=0; j<expected.size(); j++) {
                        if (i == j) continue;
                        if (detectedTable.intersects(expected.get(j))) {
                            intersectsOthers = true;
                            break;
                        }
                    }

                    if (!intersectsOthers) {
                        // success
                        detectedIterator.remove();
                        expected.remove(i);

                        this.numCorrectlyDetectedTables++;
                        totalCorrectlyDetectedTables++;

                        break;
                    }
                }
            }
        }

        // any expected tables left over weren't detected
        for (Rectangle expectedTable : expected) {
            errors.add("Page " + page.toString() + ": " + expectedTable.toString() + " not detected");
        }

        // any detected tables left over were detected erroneously
        for (Rectangle detectedTable : detected) {
            errors.add("Page " + page.toString() + ": " + detectedTable.toString() + " detected where there is no table");
            this.numErroneouslyDetectedTables++;
            totalErroneouslyDetectedTables++;
        }

        return errors;
    }
}
