package technology.tabula;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.rendering.ImageType;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

public class TestUtils {

    public static final Ruling[] RULINGS = {
        new Ruling(new Point2D.Float(0, 0), new Point2D.Float(1,1)),
        new Ruling(new Point2D.Float(2, 2), new Point2D.Float(3,3))
    };

    public static final Rectangle[] RECTANGLES = {
        new Rectangle(),
        new Rectangle(0, 0, 2, 4)
    };


    @Test
    public void testBoundsOfTwoRulings() {
        Rectangle r = Utils.bounds(Arrays.asList(RULINGS));
        assertEquals(0, r.getMinX(), 0);
        assertEquals(0, r.getMinY(), 0);
        assertEquals(3, r.getWidth(), 0);
        assertEquals(3, r.getHeight(), 0);
    }

    @Test
    public void testBoundsOfOneEmptyRectangleAndAnotherNonEmpty() {
        Rectangle r = Utils.bounds(Arrays.asList(RECTANGLES));
        assertEquals(r, RECTANGLES[1]);
    }

    @Test
    public void testBoundsOfOneRectangle() {
        ArrayList<Rectangle> shapes = new ArrayList<>();
        shapes.add(new Rectangle(0, 0, 20, 40));
        Rectangle r = Utils.bounds(shapes);
        assertEquals(r, shapes.get(0));
    }

    @Test
    public void testParsePagesOption() throws ParseException {

        List<Integer> rv = Utils.parsePagesOption("1");
        assertArrayEquals(new Integer[] { 1 }, rv.toArray());

        rv = Utils.parsePagesOption("1-4");
        assertArrayEquals(new Integer[] { 1,2,3,4 }, rv.toArray());

        rv = Utils.parsePagesOption("1-4,20-24");
        assertArrayEquals(new Integer[] { 1,2,3,4,20,21,22,23,24 }, rv.toArray());

        rv = Utils.parsePagesOption("all");
        assertNull(rv);
    }

    @Test(expected=ParseException.class)
    public void testExceptionInParsePages() throws ParseException {
        Utils.parsePagesOption("1-4,24-22");
    }

    @Test(expected=ParseException.class)
    public void testAnotherExceptionInParsePages() throws ParseException {
        Utils.parsePagesOption("quuxor");
    }

    @Test
    public void testQuickSortEmptyList() {
    	List<Integer> numbers = new ArrayList<>();
    	QuickSort.sort(numbers);

    	assertEquals(Collections.emptyList(), numbers);
    }

    @Test
    public void testQuickSortOneElementList() {
    	List<Integer> numbers = Arrays.asList(5);
    	QuickSort.sort(numbers);

    	assertEquals(Arrays.asList(5), numbers);
    }

    @Test
    public void testQuickSortShortList() {
    	List<Integer> numbers = Arrays.asList(4, 5, 6, 8, 7, 1, 2, 3);
    	QuickSort.sort(numbers);

    	assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8), numbers);
    }

    @Test
    public void testQuickSortLongList() {

    	List<Integer> numbers = new ArrayList<>();
    	List<Integer> expectedNumbers = new ArrayList<>();

    	for(int i = 0; i <= 12000; i++){
    		numbers.add(12000 - i);
    		expectedNumbers.add(i);
    	}

    	QuickSort.sort(numbers);

    	assertEquals(expectedNumbers, numbers);
    }

    @Test
    public void testJPEG2000DoesNotRaise() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/jpeg2000.pdf"));
        PDPage page = pdf_document.getPage(0);
        Utils.pageConvertToImage(page, 360, ImageType.RGB);
    }

}
