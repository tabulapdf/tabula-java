package technology.tabula;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.detectors.DetectionAlgorithm;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;

/**
 * Created by matt on 2015-12-14.
 */
@RunWith(Parameterized.class)
public class TestTableDetection {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String[] regionCodes = {"eu", "us"};

        ArrayList<Object[]> data = new ArrayList<Object[]>();

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

    public TestTableDetection(File pdf) {
        this.pdf = pdf;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            this.builder = factory.newDocumentBuilder();
        } catch (Exception e) {
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

    @Ignore("Test is ignored until better table detection algorithms are implemented")
    @Test
    public void testDetectionOfTables() throws Exception {

        // xml parsing stuff for ground truth
        Document regionDocument = this.builder.parse(this.pdf.getAbsolutePath().replace(".pdf", "-reg.xml"));
        NodeList tables = regionDocument.getElementsByTagName("table");

        // tabula extractors
        PDDocument pdfDocument = PDDocument.load(this.pdf);
        ObjectExtractor extractor = new ObjectExtractor(pdfDocument);

        // parse expected tables from the ground truth dataset
        Map<Integer, List<Rectangle>> expectedTables = new HashMap<Integer, List<Rectangle>>();

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
                pageTables = new ArrayList<Rectangle>();
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
        }

        // now find tables detected by tabula-java
        Map<Integer, List<Rectangle>> detectedTables = new HashMap<Integer, List<Rectangle>>();

        // the algorithm we're going to be testing
        DetectionAlgorithm detectionAlgorithm = new SpreadsheetDetectionAlgorithm();

        PageIterator pages = extractor.extract();
        while (pages.hasNext()) {
            Page page = pages.next();
            detectedTables.put(new Integer(page.getPageNumber()), detectionAlgorithm.detect(page));
        }

        // now compare
        System.out.println("Testing " + this.pdf.getName());

        // for now for easier debugging spit out all expected/detected tables
        System.out.println("Expected Tables:");
        this.printTables(expectedTables);

        System.out.println("Detected Tables:");
        this.printTables(detectedTables);

        assertEquals("Did not detect tables on the same number of pages", expectedTables.size(), detectedTables.size());

        for (Integer page : expectedTables.keySet()) {
            List<Rectangle> expectedPageTables = expectedTables.get(page);
            List<Rectangle> detectedPageTables = detectedTables.get(page);

            assertNotNull("Expected tables not found on page " + page.toString(), detectedPageTables);

            assertEquals("Did not find the same number of tables on page " + page.toString(), expectedPageTables.size(), detectedPageTables.size());

            // from http://www.orsigiorgio.net/wp-content/papercite-data/pdf/gho*12.pdf (comparing regions):
            // for other (e.g.“black-box”) algorithms, bounding boxes and content are used. A region is correct if it
            // contains the minimal bounding box of the ground truth without intersecting additional content.
            for (int i=0; i<detectedPageTables.size(); i++) {
                Rectangle detectedTable = detectedPageTables.get(i);
                Rectangle expectedTable = expectedPageTables.get(i);

                // make sure the detected table contains the expected table
                assertTrue(detectedTable.toString() + "\ndoes not contain " + expectedTable.toString(), detectedTable.contains(expectedTable));

                // now make sure it doesn't intersect any other tables on the page
                for (int j=0; j<expectedPageTables.size(); j++) {
                    if (j != i) {
                        Rectangle otherTable = expectedPageTables.get(j);
                        assertFalse(detectedTable.toString() + "\nintersects " + otherTable.toString(), detectedTable.intersects(otherTable));
                    }
                }

                // made it, this table checks out
                System.out.println("Table " + i + " on page " + page.toString() + " OK");
            }
        }
    }
}
