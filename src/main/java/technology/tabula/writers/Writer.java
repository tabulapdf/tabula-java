package technology.tabula.writers;

import java.io.IOException;

import technology.tabula.Table;

public interface Writer {
    void write(Appendable out, Table table) throws IOException;
}
