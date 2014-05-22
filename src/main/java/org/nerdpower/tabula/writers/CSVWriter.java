package org.nerdpower.tabula.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;
import org.nerdpower.tabula.Table;
import org.nerdpower.tabula.TextChunk;

public class CSVWriter implements Writer {
    
    CSVPrinter printer;
    
    void createWriter(Appendable out) {
        this.printer = new CSVPrinter(out, CSVFormat.EXCEL);
    }
    
    @Override
    public void write(Appendable out, Table table) throws IOException {
        this.createWriter(out);
        for (List<TextChunk> row: table.getRows()) {
            List<String> cells = new ArrayList<String>(row.size());
            for (TextChunk tc: row) {
                cells.add(tc.getText());
            }
            this.printer.printRecord(cells);
        }
        printer.flush();
    }

}
