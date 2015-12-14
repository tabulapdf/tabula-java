package technology.tabula;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;

import org.junit.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by matt on 2015-12-14.
 */
public class TestTableDetection {

    @Test
    public void testDetectionOfTables() throws Exception {
        String[] regionCodes = {"eu", "us"};

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

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

                System.out.println("Testing " + pdf.getName());

                Document regionDocument = builder.parse(pdf.getAbsolutePath().replace(".pdf", "-reg.xml"));

                NodeList tables = regionDocument.getElementsByTagName("table");

                for (int i=0; i<tables.getLength(); i++) {

                    Element table = (Element) tables.item(i);
                    int tableId = Integer.parseInt(table.getAttribute("id"));

                    Element region = (Element) table.getElementsByTagName("region").item(0);
                    int regionId = Integer.parseInt(region.getAttribute("id"));
                    int page = Integer.parseInt(region.getAttribute("page"));

                    Element boundingBox = (Element) region.getElementsByTagName("bounding-box").item(0);
                    int x1 = Integer.parseInt(boundingBox.getAttribute("x1"));
                    int y1 = Integer.parseInt(boundingBox.getAttribute("y1"));
                    int x2 = Integer.parseInt(boundingBox.getAttribute("x2"));
                    int y2 = Integer.parseInt(boundingBox.getAttribute("y2"));

                    System.out.println("Table " + tableId + " is region " + regionId + " on page " + page + " with coords " + x1 + ", " + y1 + ", " + x2 + ", " + y2);
                }
            }
        }
    }
}
