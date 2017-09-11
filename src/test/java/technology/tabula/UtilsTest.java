package technology.tabula;

import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    private static final Ruling[] RULINGS = {
        new Ruling(new Point2D.Float(0, 0), new Point2D.Float(1,1)),
        new Ruling(new Point2D.Float(2, 2), new Point2D.Float(3,3))
    };

    private static final Rectangle[] RECTANGLES = {
        new Rectangle(),
        new Rectangle(0, 0, 2, 4)
    };


    @Test
    public void testBoundsOfTwoRulings() {
        Rectangle r = Utils.bounds(Arrays.asList(RULINGS));
        assertThat(r.getMinX()).isEqualTo(0);
        assertThat(r.getMinY()).isEqualTo(0);
        assertThat(r.getWidth()).isEqualTo(3);
        assertThat(r.getHeight()).isEqualTo(3);
    }

    @Test
    public void testBoundsOfOneEmptyRectangleAndAnotherNonEmpty() {
        Rectangle r = Utils.bounds(Arrays.asList(RECTANGLES));
        assertThat(RECTANGLES[1]).isEqualTo(r);
    }

    @Test
    public void testBoundsOfOneRectangle() {
        List<Rectangle> shapes = new ArrayList<>();
        shapes.add(new Rectangle(0, 0, 20, 40));
        Rectangle r = Utils.bounds(shapes);
        assertThat(shapes.get(0)).isEqualTo(r);
    }

    @Test
    public void testParsePagesOption() throws ParseException {

        List<Integer> rv = Utils.parsePagesOption("1");
        assertThat(rv).isNotNull();
        assertThat(rv.toArray()).isEqualTo(new Integer[] { 1 });

        rv = Utils.parsePagesOption("1-4");
        assertThat(rv).isNotNull();
        assertThat(rv.toArray()).isEqualTo(new Integer[] { 1,2,3,4 });

        rv = Utils.parsePagesOption("1-4,20-24");
        assertThat(rv).isNotNull();
        assertThat(rv.toArray()).isEqualTo(new Integer[] { 1,2,3,4,20,21,22,23,24 });

        rv = Utils.parsePagesOption("all");
        assertThat(rv).isNull();
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

    	assertThat(numbers).isEmpty();
    }

    @Test
    public void testQuickSortOneElementList() {
    	List<Integer> numbers = Arrays.asList(5);
    	QuickSort.sort(numbers);

    	assertThat(numbers).containsExactly(5);
    }

    @Test
    public void testQuickSortShortList() {
    	List<Integer> numbers = Arrays.asList(4, 5, 6, 8, 7, 1, 2, 3);
    	QuickSort.sort(numbers);

    	assertThat(numbers).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
    }

    @Test
    public void testQuickSortLongList() {

        final int SIZE = 12_000;
    	List<Integer> numbers = new ArrayList<>(SIZE+1);
    	List<Integer> expectedNumbers = new ArrayList<>(SIZE+1);

    	for(int i = 0; i <= SIZE; i++){
    		numbers.add(SIZE - i);
    		expectedNumbers.add(i);
    	}

    	QuickSort.sort(numbers);

    	assertThat(numbers).isEqualTo(expectedNumbers);
    }

    @Test
    public void testJPEG2000DoesNotRaise() throws IOException {
        PDDocument pdf_document = PDDocument.load(new File("src/test/resources/technology/tabula/jpeg2000.pdf"));
        PDPage page = pdf_document.getPage(0);
        BufferedImage bufferedImage = Utils.pageConvertToImage(page, 360, ImageType.RGB);
        assertThat(bufferedImage).isNotNull();
    }
}
