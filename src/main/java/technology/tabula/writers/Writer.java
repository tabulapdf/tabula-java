package technology.tabula.writers;

import java.io.IOException;
import java.util.List;

import technology.tabula.Table;

public interface Writer {
    void write(Appendable out, Table table) throws IOException;
    void write(Appendable out, List<Table> tables) throws IOException;
}
