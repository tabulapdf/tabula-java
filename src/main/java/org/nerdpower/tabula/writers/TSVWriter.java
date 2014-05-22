package org.nerdpower.tabula.writers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class TSVWriter extends CSVWriter {
    
    @Override
    void createWriter(Appendable out) {
        this.printer = new CSVPrinter(out, CSVFormat.TDF);
    }
    
}
