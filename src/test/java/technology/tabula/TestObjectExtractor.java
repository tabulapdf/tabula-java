package technology.tabula;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

public class TestObjectExtractor {

    /*@Test(expected=IOException.class)
    public void testWrongPasswordRaisesException() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/encrypted.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document, "wrongpass"); 
        oe.extract().next();
    }*/

    @Test(expected = IOException.class)
    public void testEmptyOnEncryptedFileRaisesException() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/encrypted.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        oe.extract().next();
    }

    @Test
    public void testCanReadPDFWithOwnerEncryption() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/S2MNCEbirdisland.pdf"));
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
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/encrypted.pdf"), "userpassword");
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        List<Page> pages = new ArrayList<>();
        PageIterator pi = oe.extract();
        while (pi.hasNext()) {
            pages.add(pi.next());
        }
        assertEquals(1, pages.size());
    }


    @Test
    public void testTextExtractionDoesNotRaise() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/rotated_page.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();

        assertTrue(pi.hasNext());
        assertNotNull(pi.next());
        assertFalse(pi.hasNext());

    }

    @Test
    public void testShouldDetectRulings() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/should_detect_rulings.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();

        Page page = pi.next();
        List<Ruling> rulings = page.getRulings();

        for (Ruling r: rulings) {
            assertTrue(page.contains(r.getBounds()));
        }
    }

    @Test
    public void testDontThrowNPEInShfill() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/labor.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();
        assertTrue(pi.hasNext());
        try {
            Page p = pi.next();
            assertNotNull(p);
        } catch (NullPointerException e) {
            fail("NPE in ObjectExtractor " + e.toString());
        }
    }

    @Test
    public void testExtractOnePage() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/S2MNCEbirdisland.pdf"));
        assertEquals(2, pdf_document.getNumberOfPages());

        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        Page page = oe.extract(2);

        assertNotNull(page);

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testExtractWrongPageNumber() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/S2MNCEbirdisland.pdf"));
        assertEquals(2, pdf_document.getNumberOfPages());

        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        oe.extract(3);

    }

    @Test
    public void testTextElementsContainedInPage() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/cs-en-us-pbms.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);

        Page page = oe.extractPage(1);

        for (TextElement te: page.getText()) {
            assertTrue(page.contains(te));
        }
    }

    @Test public void testDoNotNPEInPointComparator() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/npe_issue_206.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);

        try {
            Page p = oe.extractPage(1);
            assertNotNull(p);
        } catch (NullPointerException e) {
            fail("NPE in ObjectExtractor " + e.toString());
        }
    }
    
    /*
    @Test
    public void testExtractWithoutExtractingRulings() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/should_detect_rulings.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document, null, false, false);
        PageIterator pi = oe.extract();
       
        assertTrue(pi.hasNext());
        Page page = pi.next();
        assertNotNull(page);
        assertEquals(0, page.getRulings().size());
        assertFalse(pi.hasNext());
    }
    */

}
