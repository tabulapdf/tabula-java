package technology.tabula.detectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.ImageIOUtil;
import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.Ruling;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 2015-12-17.
 */
public class NurminenDetectionAlgorithm implements DetectionAlgorithm {
    public String debugFilename;

    @Override
    public List<Rectangle> detect(Page page, PDDocument referenceDocument) {

        PDPage pdfPage = (PDPage) referenceDocument.getDocumentCatalog().getAllPages().get(page.getPageNumber() - 1);

        BufferedImage image;
        BufferedImage debugImage;
        try {
            image = pdfPage.convertToImage(BufferedImage.TYPE_BYTE_GRAY, 144);
            debugImage = pdfPage.convertToImage(BufferedImage.TYPE_INT_RGB, 144);
        } catch (IOException e) {
            return new ArrayList<Rectangle>();
        }

        Graphics2D g = (Graphics2D) debugImage.getGraphics();

        Raster r = image.getRaster();
        int width = r.getWidth();
        int height = r.getHeight();

        ArrayList<Line2D.Float> horizontalRulings = new ArrayList<Line2D.Float>();

        int threshold = 150;
        int white = 255;

        for (int x=0; x<width; x++) {

            int[] lastPixel = r.getPixel(x, 0, (int[])null);

            for (int y=1; y<height-1; y++) {

                int[] currPixel = r.getPixel(x, y, (int[])null);

                int diff = Math.abs(currPixel[0] - lastPixel[0]);
                if (diff > threshold) {
                    // we hit what could be a line

                    if (currPixel[0] >= white) {
                        // don't check white-ish, it can't be a line
                        lastPixel = currPixel;
                        continue;
                    }

                    // don't bother scanning it if we've hit a pixel in the line before
                    boolean alreadyChecked = false;
                    for (Line2D.Float line : horizontalRulings) {
                        if (y == line.getY1() && x >= line.getX1() && x <= line.getX2()) {
                            alreadyChecked = true;
                            break;
                        }
                    }

                    if (alreadyChecked) {
                        lastPixel = currPixel;
                        continue;
                    }

                    for (int lineX=x; lineX<width; lineX++) {
                        int[] linePixel = r.getPixel(lineX, y, (int[])null);
                        if (linePixel[0] >= white || Math.abs(currPixel[0] - linePixel[0]) > threshold || lineX == width - 1) {
                            int lineWidth = lineX - x;
                            if (lineWidth > 100) {
                                horizontalRulings.add(new Line2D.Float(x, y, lineX, y));
                            }
                            break;
                        }
                    }
                }

                lastPixel = currPixel;
            }
        }


        String debugFileOut = debugFilename.replace(".pdf", "-" + page.getPageNumber() + ".jpg");

        Color[] COLORS = { new Color(27, 158, 119),
                new Color(217, 95, 2), new Color(117, 112, 179),
                new Color(231, 41, 138), new Color(102, 166, 30) };

        g.setStroke(new BasicStroke(2f));
        int i = 0;
        for (Shape s: horizontalRulings) {
            g.setColor(COLORS[(i++) % 5]);
            g.draw(s);
        }

        try {
            ImageIOUtil.writeImage(debugImage, debugFileOut, 144);
        } catch (IOException e) {
        }

        return new ArrayList<Rectangle>();
    }
}
