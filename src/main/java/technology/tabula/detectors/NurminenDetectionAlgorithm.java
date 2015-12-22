package technology.tabula.detectors;

import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.pdfbox.util.PDFOperator;
import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.Ruling;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

/**
 * Created by matt on 2015-12-17.
 *
 * Attempt at an implementation of the table finding algorithm described by
 * Anssi Nurminen's master's thesis:
 * http://dspace.cc.tut.fi/dpub/bitstream/handle/123456789/21520/Nurminen.pdf?sequence=3
 */
public class NurminenDetectionAlgorithm implements DetectionAlgorithm {

    private static int GRAYSCALE_INTENSITY_THRESHOLD = 150;

    @Override
    public List<Rectangle> detect(Page page, File referenceDocument) {

        // open a PDDocument to read stuff in
        PDDocument pdfDocument;
        try {
            pdfDocument = PDDocument.load(referenceDocument);
        } catch (Exception e) {
            return new ArrayList<Rectangle>();
        }

        // get the page in question
        PDPage pdfPage = (PDPage) pdfDocument.getDocumentCatalog().getAllPages().get(page.getPageNumber() - 1);

        BufferedImage image;
        BufferedImage debugImage;
        try {
            image = pdfPage.convertToImage(BufferedImage.TYPE_BYTE_GRAY, 144);
            debugImage = pdfPage.convertToImage(BufferedImage.TYPE_INT_RGB, 144);
        } catch (IOException e) {
            return new ArrayList<Rectangle>();
        }

        List<Line2D.Float> horizontalRulings = this.getHorizontalRulings(image);

        // now check the page for vertical lines, but remove the text first to make things less confusing
        try {
            this.removeText(pdfDocument, pdfPage);
            image = pdfPage.convertToImage(BufferedImage.TYPE_BYTE_GRAY, 144);
        } catch (Exception e) {
            return new ArrayList<Rectangle>();
        }

        List<Line2D.Float> verticalRulings = this.getVerticalRulings(image);

        // now we need to snap edge endpoints to a grid
        this.snapEndpoints(horizontalRulings, verticalRulings);

        // debugging stuff - spit out an image with what we want to see
        String debugFileOut = referenceDocument.getAbsolutePath().replace(".pdf", "-" + page.getPageNumber() + ".jpg");

        Color[] COLORS = { new Color(27, 158, 119),
                new Color(217, 95, 2), new Color(117, 112, 179),
                new Color(231, 41, 138), new Color(102, 166, 30) };

        Graphics2D g = (Graphics2D) debugImage.getGraphics();

        g.setStroke(new BasicStroke(2f));
        int i = 0;
        for (Shape s: horizontalRulings) {
            g.setColor(COLORS[(i++) % 5]);
            g.draw(s);
        }

        for (Shape s: verticalRulings) {
            g.setColor(COLORS[(i++) % 5]);
            g.draw(s);
        }

        try {
            ImageIOUtil.writeImage(debugImage, debugFileOut, 144);
        } catch (IOException e) {
        }

        return new ArrayList<Rectangle>();
    }

    private void snapEndpoints(List<Line2D.Float> horizontalRulings, List<Line2D.Float> verticalRulings) {
        // let's store a hash of start/end points -> edges that contain those points
        TreeMap<Point2D.Float, ArrayList<Line2D.Float>> points = new TreeMap<Point2D.Float, ArrayList<Line2D.Float>>(new Comparator<Point2D.Float>() {
            @Override
            public int compare(Point2D.Float o1, Point2D.Float o2) {
                if (o1.equals(o2)) {
                    return 0;
                }

                if (o1.getY() == o2.getY()) {
                    return Double.compare(o1.getX(), o2.getX());
                } else {
                    return Double.compare(o1.getY(), o2.getY());
                }
            }
        });

        for (List<Line2D.Float> edges : new List[] {horizontalRulings, verticalRulings}) {
            for (Line2D.Float edge : edges) {
                for (Point2D.Float point : new Point2D.Float[]{(Point2D.Float) edge.getP1(), (Point2D.Float) edge.getP2()}) {
                    ArrayList<Line2D.Float> lines = points.get(point);

                    if (lines == null) {
                        lines = new ArrayList<Line2D.Float>();
                        points.put(point, lines);
                    }

                    lines.add(edge);
                }
            }
        }

        // now go through points to find points we can snap together
        ArrayList<Point2D.Float> snapPoints = new ArrayList<Point2D.Float>();
        NavigableSet<Point2D.Float> remainingPoints = points.navigableKeySet();

        while (remainingPoints.size() > 1) {
            Point2D.Float checkPoint = remainingPoints.first();

            snapPoints.add(checkPoint);

            Iterator<Point2D.Float> iterator = remainingPoints.tailSet(checkPoint, false).iterator();

            while (iterator.hasNext()) {
                Point2D.Float nextPoint = iterator.next();
                if (checkPoint.distance(nextPoint) <= 5) {
                    snapPoints.add(nextPoint);
                }
            }

            if (snapPoints.size() > 1) {
                // ok, we've got a list of candidates now, snap them
                // first get a mid point to snap to
                double x = 0;
                double y = 0;
                for (Point2D.Float point : snapPoints) {
                    x += point.getX();
                    y += point.getY();
                }

                Point2D.Float newPoint = new Point2D.Float(
                        (float) Math.floor(x / snapPoints.size()),
                        (float) Math.floor(y / snapPoints.size())
                );

                // now set all lines that have one of these endpoints to the new value
                for (Point2D.Float point : snapPoints) {
                    List<Line2D.Float> edges = points.get(point);
                    for (Line2D.Float edge : edges) {
                        if (edge.getP1().equals(point)) {
                            edge.setLine(newPoint, edge.getP2());
                        } else {
                            edge.setLine(edge.getP1(), newPoint);
                        }
                    }
                }
            }

            // remove the points we've checked and set up the loop again
            remainingPoints.removeAll(snapPoints);
            snapPoints.clear();
        }
    }

    private List<Line2D.Float> getHorizontalRulings(BufferedImage image) {
        ArrayList<Line2D.Float> horizontalRulings = new ArrayList<Line2D.Float>();

        Raster r = image.getRaster();
        int width = r.getWidth();
        int height = r.getHeight();

        for (int x=0; x<width; x++) {

            int[] lastPixel = r.getPixel(x, 0, (int[])null);

            for (int y=1; y<height-1; y++) {

                int[] currPixel = r.getPixel(x, y, (int[])null);

                int diff = Math.abs(currPixel[0] - lastPixel[0]);
                if (diff > GRAYSCALE_INTENSITY_THRESHOLD) {
                    // we hit what could be a line
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

                    int lineX = x + 1;

                    if (lineX < width) {
                        int[] linePixel = r.getPixel(lineX, y, (int[]) null);
                        int[] abovePixel = r.getPixel(lineX, y - 1, (int[]) null);

                        while (lineX < width
                                && Math.abs(linePixel[0] - abovePixel[0]) > GRAYSCALE_INTENSITY_THRESHOLD
                                && Math.abs(currPixel[0] - linePixel[0]) <= GRAYSCALE_INTENSITY_THRESHOLD) {

                            lineX++;
                            linePixel = r.getPixel(lineX, y, (int[]) null);
                            abovePixel = r.getPixel(lineX, y - 1, (int[]) null);
                        }
                    }

                    int endX = lineX - 1;
                    int lineWidth = endX - x;
                    if (lineWidth > 100) {
                        horizontalRulings.add(new Line2D.Float(x, y, endX, y));
                    }
                }

                lastPixel = currPixel;
            }
        }

        return horizontalRulings;
    }

    private List<Line2D.Float> getVerticalRulings(BufferedImage image) {
        ArrayList<Line2D.Float> verticalRulings = new ArrayList<Line2D.Float>();

        Raster r = image.getRaster();
        int width = r.getWidth();
        int height = r.getHeight();

        for (int y=0; y<height; y++) {

            int[] lastPixel = r.getPixel(0, y, (int[])null);

            for (int x=1; x<width-1; x++) {

                int[] currPixel = r.getPixel(x, y, (int[])null);

                int diff = Math.abs(currPixel[0] - lastPixel[0]);
                if (diff > GRAYSCALE_INTENSITY_THRESHOLD) {
                    // we hit what could be a line
                    // don't bother scanning it if we've hit a pixel in the line before
                    boolean alreadyChecked = false;
                    for (Line2D.Float line : verticalRulings) {
                        if (x == line.getX1() && y >= line.getY1() && y <= line.getY2()) {
                            alreadyChecked = true;
                            break;
                        }
                    }

                    if (alreadyChecked) {
                        lastPixel = currPixel;
                        continue;
                    }

                    int lineY = y + 1;

                    if (lineY < height) {
                        int[] linePixel = r.getPixel(x, lineY, (int[]) null);
                        int[] leftPixel = r.getPixel(x - 1, lineY, (int[]) null);

                        while (lineY < height
                                && Math.abs(linePixel[0] - leftPixel[0]) > GRAYSCALE_INTENSITY_THRESHOLD
                                && Math.abs(currPixel[0] - linePixel[0]) <= GRAYSCALE_INTENSITY_THRESHOLD) {

                            lineY++;
                            linePixel = r.getPixel(x, lineY, (int[]) null);
                            leftPixel = r.getPixel(x - 1, lineY, (int[]) null);
                        }
                    }

                    int endY = lineY - 1;
                    int lineLength = endY - y;
                    if (lineLength > 10) {
                        verticalRulings.add(new Line2D.Float(x, y, x, endY));
                    }
                }

                lastPixel = currPixel;
            }
        }

        return verticalRulings;
    }

    // taken from http://www.docjar.com/html/api/org/apache/pdfbox/examples/util/RemoveAllText.java.html
    private void removeText(PDDocument document, PDPage page) throws IOException {
        PDFStreamParser parser = new PDFStreamParser(page.getContents());
        parser.parse();

        List tokens = parser.getTokens();
        List newTokens = new ArrayList();

        for (int i=0; i<tokens.size(); i++) {
            Object token = tokens.get(i);
            if (token instanceof PDFOperator) {
                PDFOperator op = (PDFOperator)token;
                if (op.getOperation().equals("TJ") || op.getOperation().equals("Tj")) {
                    newTokens.remove(newTokens.size() - 1);
                    continue;
                }
            }
            newTokens.add(token);
        }

        PDStream newContents = new PDStream(document);
        ContentStreamWriter writer = new ContentStreamWriter(newContents.createOutputStream());
        writer.writeTokens(newTokens);
        newContents.addCompression();
        page.setContents(newContents);
    }
}
