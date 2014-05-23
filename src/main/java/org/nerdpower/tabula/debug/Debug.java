package org.nerdpower.tabula.debug;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nerdpower.tabula.ObjectExtractor;
import org.nerdpower.tabula.Page;
import org.nerdpower.tabula.Ruling;
import org.nerdpower.tabula.TextChunk;
import org.nerdpower.tabula.TextElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.ImageIOUtil;

public class Debug {
        
    private static final Color[] COLORS = { new Color(27, 158, 119),
            new Color(217, 95, 2), new Color(117, 112, 179),
            new Color(231, 41, 138), new Color(102, 166, 30) };

    public static List<TextElement> debugTextElements(Page page) {
        return page.getText();
    }
    
    public static List<Point2D> debugRulingIntersections(Page page) {
        Map<Point2D, Ruling[]> map = Ruling.findIntersections(page.getHorizontalRulings(), page.getVerticalRulings());
        return new ArrayList<Point2D>(map.keySet());
    }
    
    public static void renderPage(String pdfPath, String outPath, int pageNumber) throws IOException {
        PDDocument document = PDDocument.load(pdfPath);
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImage(pageNumber);
        
        ObjectExtractor oe = new ObjectExtractor(document);
        Page page = oe.extract(pageNumber + 1);
        
        Graphics2D g = (Graphics2D) image.getGraphics();
        
        // draw detected lines
        List<Ruling> rulings = new ArrayList<Ruling>(page.getHorizontalRulings());
        rulings.addAll(page.getVerticalRulings());
        int i = 0;
        for (Ruling r: rulings) {
            g.setColor(COLORS[(i++) % 3]);
            g.draw(r);
        }
        
        // draw text chunks
        List<TextChunk> chunks = TextElement.mergeWords(page.getText(), page.getVerticalRulings());
        i = 0;
        for (TextChunk tc: chunks) {
            g.setColor(COLORS[(i++) % 3]);
            g.draw(tc);
        }

        document.close();
        
        ImageIOUtil.writeImage(image, outPath, 72);
    }
    
    
    
}
