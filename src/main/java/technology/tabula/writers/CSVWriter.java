package technology.tabula.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;

import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

public class CSVWriter implements Writer {
    
    CSVPrinter printer;
    private boolean useLineReturns = true;
    
//    public CSVWriter() {
//        super();
//    }
//    
//    public CSVWriter(boolean useLineReturns) {
//        super();
//        this.useLineReturns = useLineReturns;
//    }

    public static String writeString(Table table) throws IOException {
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, table);
        return sb.toString();
    }
    public static String writeString(List<? extends Table> tables) throws IOException {
        StringBuilder sb = new StringBuilder();
        (new CSVWriter()).write(sb, tables);
        return sb.toString();
    }

    void createWriter(Appendable out) {
        try {
            printer = new CSVPrinter(out, CSVFormat.EXCEL);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public void write(Appendable out, Table table) throws IOException {
        createWriter(out);
        for (List<RectangularTextContainer> row: table.getRows()) {
            List<String> cells = new ArrayList<>(row.size());
            for (RectangularTextContainer tc: row) {
                cells.add(tc.getText());
            }
            printer.printRecord(cells);
        }
        printer.flush();
    }

	@Override
	public void write(Appendable out, List<? extends Table> tables) throws IOException {
		for (Table table : tables) {
			write(out, table);
		}
	}
}
