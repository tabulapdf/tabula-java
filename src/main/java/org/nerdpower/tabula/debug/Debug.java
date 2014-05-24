package org.nerdpower.tabula.debug;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nerdpower.tabula.ObjectExtractor;
import org.nerdpower.tabula.Page;
import org.nerdpower.tabula.Rectangle;
import org.nerdpower.tabula.Ruling;
import org.nerdpower.tabula.Table;
import org.nerdpower.tabula.TextChunk;
import org.nerdpower.tabula.TextElement;
import org.nerdpower.tabula.extractors.SpreadsheetExtractionAlgorithm;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.ImageIOUtil;

public class Debug {
        
    private static final Color[] COLORS = { new Color(27, 158, 119),
            new Color(217, 95, 2), new Color(117, 112, 179),
            new Color(231, 41, 138), new Color(102, 166, 30) };

    public static List<Point2D> debugRulingIntersections(Page page) {
        Map<Point2D, Ruling[]> map = Ruling.findIntersections(page.getHorizontalRulings(), page.getVerticalRulings());
        return new ArrayList<Point2D>(map.keySet());
    }
    
    private static void debugRulings(Graphics2D g, Page page) {
        // draw detected lines
        List<Ruling> rulings = new ArrayList<Ruling>(page.getHorizontalRulings());
        rulings.addAll(page.getVerticalRulings());
        drawShapes(g, rulings);
    }
    
    private static void debugTextChunks(Graphics2D g, Page page) {
        List<TextChunk> chunks = TextElement.mergeWords(page.getText(), page.getVerticalRulings());
        drawShapes(g, chunks);
    }
    
    private static void debugSpreadsheets(Graphics2D g, Page page) {
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = sea.extract(page);
        drawShapes(g, tables);
    }
    
    private static void drawShapes(Graphics2D g, List<? extends Shape> shapes) {
        int i = 0;
        g.setStroke(new BasicStroke(2f));
        for (Shape s: shapes) {
            g.setColor(COLORS[(i++) % 5]);
            g.draw(s);
        }
    }

    public static void renderPage(String pdfPath, String outPath, int pageNumber) throws IOException {
        PDDocument document = PDDocument.load(pdfPath);
        PDFRenderer renderer = new PDFRenderer(document);
        BufferedImage image = renderer.renderImage(pageNumber);
        
        ObjectExtractor oe = new ObjectExtractor(document);
        Page page = oe.extract(pageNumber + 1);
        
        Graphics2D g = (Graphics2D) image.getGraphics();
        
        // debugRulings(g, page);
        debugSpreadsheets(g, page);
                
        document.close();
        
        ImageIOUtil.writeImage(image, outPath, 72);
    }
    
    
    
}
