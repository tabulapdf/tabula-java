package technology.tabula;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class ObjectExtractorTest {

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
        fail("IOException");
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
        assertThat(i).isEqualTo(2);
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
        assertThat(pages).hasSize(1);
    }


    @Test
    public void testTextExtractionDoesNotRaise() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/rotated_page.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();

        assertThat(pi.hasNext()).isTrue();
        assertThat(pi.next()).isNotNull();
        assertThat(pi.hasNext()).isFalse();

    }

    @Test
    public void testShouldDetectRulings() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/should_detect_rulings.pdf"));
        ObjectExtractor oe = new ObjectExtractor(pdf_document);
        PageIterator pi = oe.extract();

        Page page = pi.next();
        List<Ruling> rulings = page.getRulings();

        for (Ruling r: rulings) {
            assertThat(page.contains(r.getBounds())).isTrue();
        }
    }

    @Test
    public void testDontThrowNPEInShfill() throws IOException {

        try (PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/labor.pdf"))) {
            ObjectExtractor oe = new ObjectExtractor(pdf_document);
            PageIterator pi = oe.extract();
            assertThat(pi.hasNext()).isTrue();
            try {
                Page p = pi.next();
                assertThat(p).isNotNull();
            } catch (NullPointerException e) {
                fail("NPE in ObjectExtractor " + e.toString());
            }
        }
    }

    @Test
    public void testExtractOnePage() throws IOException {
        try (PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/S2MNCEbirdisland.pdf"))) {

            assertThat(pdf_document.getNumberOfPages()).isEqualTo(2);

            ObjectExtractor oe = new ObjectExtractor(pdf_document);
            Page page = oe.extract(2);

            assertThat(page).isNotNull();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testExtractWrongPageNumber() throws IOException {
        try (PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/S2MNCEbirdisland.pdf"))) {
            assertThat(pdf_document.getNumberOfPages()).isEqualTo(2);

            ObjectExtractor oe = new ObjectExtractor(pdf_document);
            oe.extract(3);
        }
    }

    @Test
    public void testTextElementsContainedInPage() throws IOException {
        try (PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/cs-en-us-pbms.pdf"))) {
            ObjectExtractor oe = new ObjectExtractor(pdf_document);

            Page page = oe.extractPage(1);

            for (TextElement te : page.getText()) {
                assertThat(page.contains(te)).isTrue();
            }
        }
    }
    
    /*
    @Test
    public void testExtractWithoutExtractingRulings() throws IOException {
        PDDocument pdf_document = PDDocument.load("src/test/resources/technology/tabula/should_detect_rulings.pdf");
        ObjectExtractor oe = new ObjectExtractor(pdf_document, null, false, false);
        PageIterator pi = oe.extract();
       
        assertThat(pi.hasNext()).isTrue();
        Page page = pi.next();
        assertThat(page).isNotNull();
        assertThat(page.getRulings()).isEmpty();
        assertThat(pi.hasNext()).isFalse();
    }
    */
}
