package technology.tabula;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;

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
    
    public static String loadJson(String path) throws IOException {
    	
    	BufferedReader reader = new BufferedReader( new FileReader (path));
    	StringBuilder  stringBuilder = new StringBuilder();
    	String line = null;
    	
        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
        }

        return stringBuilder.toString();
    	
    }
    
    public static String loadCsv(String path) throws IOException {
    	
    	StringBuilder out = new StringBuilder();
    	CSVParser parse = org.apache.commons.csv.CSVParser.parse(new File(path), Charset.forName("utf-8"), CSVFormat.EXCEL);
        
    	CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL);
        printer.printRecords(parse);
        printer.close();

        String csv = out.toString().replaceAll("(?<!\r)\n", "\r");
        return csv;
    	
    }
    

}
