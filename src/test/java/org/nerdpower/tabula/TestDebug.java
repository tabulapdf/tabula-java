package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nerdpower.tabula.debug.Debug;

public class TestDebug {

    private final static String PATH = "src/test/resources/org/nerdpower/tabula/spanning_cells.pdf";
    

    @Test
    public void test() throws IOException {
        String outPath = new File(new File(System.getProperty("java.io.tmpdir")), "/rendered_page.jpg").getAbsolutePath();
        Debug.renderPage(PATH, outPath, 0);
        System.out.println(outPath);
    }

}
