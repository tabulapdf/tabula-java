package technology.tabula;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

public class TestObjectExtractor {

    @Test(expected=IOException.class)
    public void testWrongPasswordRaisesException() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/encrypted.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document, "wrongpass"); 
        oe.extract().next();
    }
    
    @Test(expected=IOException.class)
    public void testEmptyOnEncryptedFileRaisesException() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/encrypted.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document); 
        oe.extract().next();
    }
    
    @Test
    public void testCanReadPDFWithOwnerEncryption() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/S2MNCEbirdisland.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();
        int i = 0;
        while (pi.hasNext()) {
            i++;
            pi.next();
        }
        assertEquals(2, i);
    }
    
    @Test
    public void testGoodPassword() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/encrypted.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document, "userpassword"); 
        List<Page> pages = new ArrayList<Page>();
        PageIterator pi = oe.extract();
        while (pi.hasNext()) {
            pages.add(pi.next());
        }
        assertEquals(1, pages.size());
    }
    
    @Test
    public void testTextExtractionDoesNotRaise() throws IOException {
        //PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/test_text_exception.pdf");
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/rotated_page.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();
        while (pi.hasNext()) {
            // System.out.println(pi.next().getText());
            // pages.add(pi.next());
        }
    }
    
    @Test
    public void testShouldDetectRulings() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/should_detect_rulings.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();
       
        while (pi.hasNext()) {
            assertNotEquals(0, pi.next().getRulings().size());
        }
    }
    
    @Test
    public void testDontThrowNPEInShfill() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/labor.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();
        try {
            Page p = pi.next();
        }
        catch (NullPointerException e) {
            fail("NPE in ObjectExtractor " + e.toString());
        }
    }
    
    
}
