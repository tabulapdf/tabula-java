package org.nerdpower.tabula.writers;

import java.io.IOException;

import org.nerdpower.tabula.Table;

public interface Writer {
    void write(Appendable out, Table table) throws IOException;
}
