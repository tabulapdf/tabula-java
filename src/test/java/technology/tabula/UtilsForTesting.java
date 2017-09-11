package technology.tabula;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

class UtilsForTesting {

    static Page getAreaFromFirstPage(String path, float top, float left, float bottom, float right) throws IOException {
        return getAreaFromPage(path, 1, top, left, bottom, right);
    }

    static Page getAreaFromPage(String path, int page, float top, float left, float bottom, float right) throws IOException {
        return getPage(path, page).getArea(top, left, bottom, right);
    }

    static Page getPage(String path, int pageNumber) throws IOException {
        ObjectExtractor oe = null;
        try {
            try (PDDocument document = PDDocument.load(new File(path))) {
                oe = new ObjectExtractor(document);
                return oe.extract(pageNumber);
            }
        } finally {
            if (oe != null)
                oe.close();
        }
    }

    static String[][] tableToArrayOfRows(Table table) {
        List<List<RectangularTextContainer>> tableRows = table.getRows();

        int maxColCount = -Integer.MAX_VALUE;

        for (List<RectangularTextContainer> row : tableRows) {
            if (maxColCount < row.size()) {
                maxColCount = row.size();
            }
        }
        String[][] rv = new String[tableRows.size()][maxColCount];

        for (int i = 0; i < tableRows.size(); i++) {
            List<RectangularTextContainer> row = tableRows.get(i);
            for (int j = 0; j < row.size(); j++) {
                rv[i][j] = table.getCell(i, j).getText();
            }
        }

        return rv;
    }

    static String loadTextFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();

    }

    static String loadNormalizedCsv(String path) throws IOException {

        StringBuilder out = new StringBuilder();
        CSVParser parse = CSVParser.parse(new File(path), Charset.forName("utf-8"), CSVFormat.DEFAULT);

        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
        printer.printRecords(parse);
        printer.close();

        return out.toString();
    }
}
