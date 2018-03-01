package technology.tabula;

import static org.junit.Assert.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestAnnotationExtraction {

    @Test
    public void testAnnotationsAreDetected() throws IOException {
        PDDocument pdfDocument = PDDocument.load(new File("src/test/resources/technology/tabula/with_annotations.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdfDocument);
        PageIterator pi = oe.extract();

        Page page = pi.next();

        System.out.println(page);


    }


}