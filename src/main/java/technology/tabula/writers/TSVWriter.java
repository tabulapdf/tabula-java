package technology.tabula.writers;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class TSVWriter extends CSVWriter {
    
    @Override
    void createWriter(Appendable out) {
        try {
            this.printer = new CSVPrinter(out, CSVFormat.TDF);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
