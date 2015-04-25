package technology.tabula;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import static org.junit.Assert.*;

public class UtilsForTesting {
    
    public static Page getAreaFromFirstPage(String path, float top, float left, float bottom, float right) throws IOException {
        return getAreaFromPage(path, 1, top, left, bottom, right);
    }
    
    public static Page getAreaFromPage(String path, int page, float top, float left, float bottom, float right) throws IOException {
        return getPage(path, page).getArea(top, left, bottom, right);
    }
    
    public static Page getPage(String path, int pageNumber) throws IOException {
        ObjectExtractor oe = null;
        try {
            PDDocument document = PDDocument
                    .load(path);
            oe = new ObjectExtractor(document);
            Page page = oe.extract(pageNumber);
            return page;
        } finally {
            if (oe != null)
                oe.close();
        }
    }
    
    public static void assertTableEquals(Table table, String[][] arrayOfRows) {
        List<List<RectangularTextContainer>> tableRows = table.getRows();
        assertEquals(arrayOfRows.length, tableRows.size());
        for (int i = 0; i < arrayOfRows.length; i++) {
            String[] row = arrayOfRows[i];
            assertEquals(row.length, tableRows.get(i).size());
            for (int j = 0; j < row.length; j++) {
                assertEquals(row[j].trim(), table.getCell(i, j).getText().trim());
            }
        }
    }
    

}
