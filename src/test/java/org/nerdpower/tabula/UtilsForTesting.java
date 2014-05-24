package org.nerdpower.tabula;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

public class UtilsForTesting {
    
    public static Page getAreaFromFirstPage(String path, float top, float left, float bottom, float right) throws IOException {
        return getFirstPage(path).getArea(top, left, bottom, right);
    }
    
    public static Page getFirstPage(String path) throws IOException {
        ObjectExtractor oe = null;
        try {
            PDDocument document = PDDocument
                    .load(path);
            oe = new ObjectExtractor(document);
            Page page = oe.extract().next();
            return page;
        } finally {
            oe.close();
        }
    }
    

}
