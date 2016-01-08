package technology.tabula.detectors;

import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.ImageIOUtil;
import org.apache.pdfbox.util.PDFOperator;
import technology.tabula.*;
import technology.tabula.Rectangle;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
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

    private static final int GRAYSCALE_INTENSITY_THRESHOLD = 25;
    private static final int HORIZONTAL_EDGE_WIDTH_MINIMUM = 50;
    private static final int VERTICAL_EDGE_HEIGHT_MINIMUM = 10;
    private static final int CELL_CORNER_DISTANCE_MAXIMUM = 10;
    private static final float POINT_SNAP_DISTANCE_THRESHOLD = 8f;
    private static final float TABLE_PADDING_AMOUNT = 1.0f;
    private static final int REQUIRED_TEXT_LINES_FOR_EDGE = 4;
    private static final int REQUIRED_CELLS_FOR_TABLE = 4;
    private static final float IDENTICAL_TABLE_OVERLAP_RATIO = 0.98f;

    /**
     * Comparator to sort points by top-leftmost first
     */
    private static final Comparator<Point2D.Float> pointComparator = new Comparator<Point2D.Float>() {
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
    };

    /**
     * Helper class that encapsulates a text edge
     */
    private static final class TextEdge extends Line2D.Float {
        // types of text edges
        public static final int LEFT = 0;
        public static final int MID = 1;
        public static final int RIGHT = 2;
        public static final int NUM_TYPES = 3;

        public int intersectingTextRowCount;

        public TextEdge(float x1, float y1, float x2, float y2) {
            super(x1, y1, x2, y2);
            this.intersectingTextRowCount = 0;
        }
    }

    /**
     * Helper container for all text edges on a page
     */
    private static final class TextEdges extends ArrayList<List<TextEdge>> {
        public TextEdges(List<TextEdge> leftEdges, List<TextEdge> midEdges, List<TextEdge> rightEdges) {
            super(3);
            this.add(leftEdges);
            this.add(midEdges);
            this.add(rightEdges);
        }
    }

    /**
     * Helper container for relevant text edge info
     */
    private static final class RelevantEdges {
        public int edgeType;
        public int edgeCount;

        public RelevantEdges(int edgeType, int edgeCount) {
            this.edgeType = edgeType;
            this.edgeCount = edgeCount;
        }
    }

    // for debugging
    private File currentDoc;
    private Page currentPage;
    private PDPage currentPDPage;

    @Override
    public List<Rectangle> detect(Page page, File referenceDocument) {

        // open a PDDocument to read stuff in
        PDDocument pdfDocument;
        try {
            pdfDocument = PDDocument.load(referenceDocument);
        } catch (Exception e) {
            return new ArrayList<Rectangle>();
        }

        // get the page in question (and keep refs for debugging)
        this.currentPDPage = (PDPage) pdfDocument.getDocumentCatalog().getAllPages().get(page.getPageNumber() - 1);
        this.currentDoc = referenceDocument;
        this.currentPage = page;

        // get horizontal & vertical lines
        // we get these from an image of the PDF and not the PDF itself because sometimes there are invisible PDF
        // instructions that are interpreted incorrectly as visible elements - we really want to capture what a
        // person sees when they look at the PDF
        BufferedImage image;
        try {
            image = this.currentPDPage.convertToImage(BufferedImage.TYPE_BYTE_GRAY, 144);
        } catch (IOException e) {
            return new ArrayList<Rectangle>();
        }

        List<Line2D.Float> horizontalRulings = this.getHorizontalRulings(image);

        // now check the page for vertical lines, but remove the text first to make things less confusing
        try {
            this.removeText(pdfDocument, this.currentPDPage);
            image = this.currentPDPage.convertToImage(BufferedImage.TYPE_BYTE_GRAY, 144);
        } catch (Exception e) {
            return new ArrayList<Rectangle>();
        }

        List<Line2D.Float> verticalRulings = this.getVerticalRulings(image);

        List<Line2D.Float> allEdges = new ArrayList<Line2D.Float>(horizontalRulings);
        allEdges.addAll(verticalRulings);

        List<Rectangle> tableAreas = new ArrayList<Rectangle>();

        // if we found some edges, try to find some tables based on them
        if (allEdges.size() > 0) {
            // now we need to snap edge endpoints to a grid
            Utils.snapPoints(allEdges, POINT_SNAP_DISTANCE_THRESHOLD, POINT_SNAP_DISTANCE_THRESHOLD);

            // next get the crossing points of all the edges
            Set<Point2D.Float> crossingPoints = this.getCrossingPoints(horizontalRulings, verticalRulings);

            // merge the edge lines into rulings - this makes finding edges between crossing points in the next step easier
            horizontalRulings = this.mergeHorizontalEdges(horizontalRulings);
            verticalRulings = this.mergeVerticalEdges(verticalRulings);

            // use the rulings and points to find cells
            List<Rectangle> cells = this.findRectangles(crossingPoints, horizontalRulings, verticalRulings);

            // then use those cells to make table areas
            tableAreas = this.getTableAreasFromCells(cells);
        }

        // next find any vertical rulings that intersect tables - sometimes these won't have completely been captured as
        // cells if there are missing horizontal lines (which there often are)
        // let's assume though that these lines should be part of the table
        for (Line2D.Float verticalRuling : verticalRulings) {
            for (Rectangle tableArea : tableAreas) {
                if (verticalRuling.intersects(tableArea) &&
                        !(tableArea.contains(verticalRuling.getP1()) && tableArea.contains(verticalRuling.getP2()))) {

                    tableArea.setTop((float)Math.floor(Math.min(tableArea.getTop(), verticalRuling.getY1())));
                    tableArea.setBottom((float)Math.ceil(Math.max(tableArea.getBottom(), verticalRuling.getY2())));
                    break;
                }
            }
        }

        // the tabula Page coordinate space is half the size of the PDFBox image coordinate space
        // so halve the table area size before proceeding and add a bit of padding to make sure we capture everything
        for (Rectangle area : tableAreas) {
            area.x = (float)Math.floor(area.x/2) - TABLE_PADDING_AMOUNT;
            area.y = (float)Math.floor(area.y/2) - TABLE_PADDING_AMOUNT;
            area.width = (float)Math.ceil(area.width/2) + TABLE_PADDING_AMOUNT;
            area.height = (float)Math.ceil(area.height/2) + TABLE_PADDING_AMOUNT;
        }

        // we're going to want halved horizontal lines later too
        for (Line2D.Float ruling : horizontalRulings) {
            ruling.x1 = ruling.x1/2;
            ruling.y1 = ruling.y1/2;
            ruling.x2 = ruling.x2/2;
            ruling.y2 = ruling.y2/2;
        }

        // now look at text rows to help us find more tables and flesh out existing ones
        List<TextChunk> textChunks = TextElement.mergeWords(page.getText());
        List<Line> lines = TextChunk.groupByLines(textChunks);

        // first look for text rows that intersect an existing table - those lines should probably be part of the table
        for (Line textRow : lines) {
            for (Rectangle tableArea : tableAreas) {
                if (!tableArea.contains(textRow) && textRow.intersects(tableArea)) {
                    tableArea.setLeft((float)Math.floor(Math.min(textRow.getLeft(), tableArea.getLeft())));
                    tableArea.setRight((float)Math.ceil(Math.max(textRow.getRight(), tableArea.getRight())));
                }
            }
        }

        // get rid of tables that DO NOT intersect any text areas - these are likely graphs or some sort of graphic
        for (Iterator<Rectangle> iterator = tableAreas.iterator(); iterator.hasNext();) {
            Rectangle table = iterator.next();

            boolean intersectsText = false;
            for (Line textRow : lines) {
                if (table.intersects(textRow)) {
                    intersectsText = true;
                    break;
                }
            }

            if (!intersectsText) {
                iterator.remove();
            }
        }

        // lastly, there may be some tables that don't have any vertical rulings at all
        // we'll use text edges we've found to try and guess which text rows are part of a table

        // in his thesis nurminen goes through every row to try to assign a probability that the line is in a table
        // we're going to try a general heuristic instead, trying to find what type of edge (left/right/mid) intersects
        // the most text rows, and then use that magic number of "relevant" edges to decide what text rows should be
        // part of a table.

        boolean foundTable;

        do {
            foundTable = false;

            // get rid of any text lines contained within existing tables, this allows us to find more tables
            for (Iterator<Line> iterator = lines.iterator(); iterator.hasNext();) {
                Line textRow = iterator.next();
                for (Rectangle table : tableAreas) {
                    if (table.contains(textRow)) {
                        iterator.remove();
                        break;
                    }
                }
            }

            // get text edges from remaining lines in the document
            TextEdges textEdges = this.getTextEdges(lines);
            List<TextEdge> leftTextEdges = textEdges.get(TextEdge.LEFT);
            List<TextEdge> midTextEdges = textEdges.get(TextEdge.MID);
            List<TextEdge> rightTextEdges = textEdges.get(TextEdge.RIGHT);

            // find the relevant text edges (the ones we think define where a table is)
            RelevantEdges relevantEdgeInfo = this.getRelevantEdges(textEdges, lines);

            // we found something relevant so let's look for rows that fit our criteria
            if (relevantEdgeInfo.edgeType != -1) {
                List<TextEdge> relevantEdges = null;
                switch(relevantEdgeInfo.edgeType) {
                    case TextEdge.LEFT:
                        relevantEdges = leftTextEdges;
                        break;
                    case TextEdge.MID:
                        relevantEdges = midTextEdges;
                        break;
                    case TextEdge.RIGHT:
                        relevantEdges = rightTextEdges;
                        break;
                }

                Rectangle table = this.getTableFromText(lines, relevantEdges, relevantEdgeInfo.edgeCount, horizontalRulings);

                if (table != null) {
                    foundTable = true;
                    tableAreas.add(table);
                }
            }
        } while (foundTable);

        // create a set of our current tables that will eliminate duplicate tables
        Set<Rectangle> tableSet = new TreeSet<Rectangle>(new Comparator<Rectangle>() {
            @Override
            public int compare(Rectangle o1, Rectangle o2) {
                if (o1.equals(o2)) {
                    return 0;
                }

                // o1 is "equal" to o2 if o2 contains all of o1
                if (o2.contains(o1)) {
                    return 0;
                }

                // otherwise see if these tables are "mostly" the same
                float overlap = o1.overlapRatio(o2);
                if (overlap >= IDENTICAL_TABLE_OVERLAP_RATIO) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        tableSet.addAll(tableAreas);

        this.currentDoc = null;
        this.currentPage = null;
        this.currentPDPage = null;

        return new ArrayList<Rectangle>(tableSet);
    }

    private Rectangle getTableFromText(List<Line> lines,
                                       List<TextEdge> relevantEdges,
                                       int relevantEdgeCount,
                                       List<Line2D.Float> horizontalRulings) {

        Rectangle table = new Rectangle();

        Line prevRow = null;
        Line firstTableRow = null;
        Line lastTableRow = null;

        int tableSpaceCount = 0;
        float totalRowSpacing = 0;

        // go through the lines and find the ones that have the correct count of the relevant edges
        for (Line textRow : lines) {
            int numRelevantEdges = 0;

            if (firstTableRow != null && tableSpaceCount > 0) {
                // check to make sure this text row is within a line or so of the other lines already added
                // if it's not, we should stop the table here
                float tableLineThreshold = (totalRowSpacing / tableSpaceCount) * 2.5f;
                float lineDistance = textRow.getTop() - prevRow.getTop();

                if (lineDistance > tableLineThreshold) {
                    lastTableRow = prevRow;
                    break;
                }
            }

            // for larger tables, be a little lenient on the number of relevant rows the text intersects
            // for smaller tables, not so much - otherwise we'll end up treating paragraphs as tables too
            int relativeEdgeDifferenceThreshold = 1;
            if (relevantEdgeCount <= 3) {
                relativeEdgeDifferenceThreshold = 0;
            }

            for (TextEdge edge : relevantEdges) {
                if (textRow.intersectsLine(edge)) {
                    numRelevantEdges++;
                }
            }

            // see if we have a candidate text row
            if (numRelevantEdges >= (relevantEdgeCount - relativeEdgeDifferenceThreshold)) {
                // keep track of table row spacing
                if (prevRow != null && firstTableRow != null) {
                    tableSpaceCount++;
                    totalRowSpacing += (textRow.getTop() - prevRow.getTop());
                }

                // row is part of a table
                if (table.getArea() == 0) {
                    firstTableRow = textRow;
                    table.setRect(textRow);
                } else {
                    table.setLeft(Math.min(table.getLeft(), textRow.getLeft()));
                    table.setBottom(Math.max(table.getBottom(), textRow.getBottom()));
                    table.setRight(Math.max(table.getRight(), textRow.getRight()));
                }
            } else {
                // no dice
                // if we're at the end of the table, save the last row
                if (firstTableRow != null && lastTableRow == null) {
                    lastTableRow = prevRow;
                }
            }

            prevRow = textRow;
        }

        // if we don't have a table now, we won't after the next step either
        if (table.getArea() == 0) {
            return null;
        }

        if (lastTableRow == null) {
            // takes care of one-row tables or tables that end at the bottom of a page
            lastTableRow = prevRow;
        }

        // use the average row height and nearby horizontal lines to extend the table area
        float avgRowHeight;
        if (tableSpaceCount > 0) {
            avgRowHeight = totalRowSpacing / tableSpaceCount;
        } else {
            avgRowHeight = lastTableRow.height;
        }

        float rowHeightThreshold = avgRowHeight * 1.5f;

        // check lines after the bottom of the table
        for (Line2D.Float ruling : horizontalRulings) {

            if (ruling.getY1() < table.getBottom()) {
                continue;
            }

            float distanceFromTable = (float)ruling.getY1() - table.getBottom();
            if (distanceFromTable <= rowHeightThreshold) {
                // use this ruling to help define the table
                table.setBottom((float)Math.max(table.getBottom(), ruling.getY1()));
                table.setLeft((float)Math.min(table.getLeft(), ruling.getX1()));
                table.setRight((float)Math.max(table.getRight(), ruling.getX2()));
            } else {
                // no use checking any further
                break;
            }
        }

        // do the same for lines at the top, but make the threshold greater since table headings tend to be
        // larger to fit up to two-ish rows of text (at least but we don't want to grab too much)
        rowHeightThreshold = avgRowHeight * 2.5f;

        for (int i=horizontalRulings.size() - 1; i>=0; i--) {
            Line2D.Float ruling = horizontalRulings.get(i);

            if (ruling.getY1() > table.getTop()) {
                continue;
            }

            float distanceFromTable = table.getTop() - (float)ruling.getY1();
            if (distanceFromTable <= rowHeightThreshold) {
                table.setTop((float)Math.min(table.getTop(), ruling.getY1()));
                table.setLeft((float)Math.min(table.getLeft(), ruling.getX1()));
                table.setRight((float)Math.max(table.getRight(), ruling.getX2()));
            } else {
                break;
            }
        }

        // add a bit of padding since the halved horizontal lines are a little fuzzy anyways
        table.setTop((float)Math.floor(table.getTop()) - TABLE_PADDING_AMOUNT);
        table.setBottom((float)Math.ceil(table.getBottom()) + TABLE_PADDING_AMOUNT);
        table.setLeft((float)Math.floor(table.getLeft()) - TABLE_PADDING_AMOUNT);
        table.setRight((float)Math.ceil(table.getRight()) + TABLE_PADDING_AMOUNT);

        return table;
    }

    private RelevantEdges getRelevantEdges(TextEdges textEdges, List<Line> lines) {
        List<TextEdge> leftTextEdges = textEdges.get(TextEdge.LEFT);
        List<TextEdge> midTextEdges = textEdges.get(TextEdge.MID);
        List<TextEdge> rightTextEdges = textEdges.get(TextEdge.RIGHT);

        // first we'll find the number of lines each type of edge crosses
        int[][] edgeCountsPerLine = new int[lines.size()][TextEdge.NUM_TYPES];

        for (TextEdge edge : leftTextEdges) {
            edgeCountsPerLine[edge.intersectingTextRowCount - 1][TextEdge.LEFT]++;
        }

        for (TextEdge edge : midTextEdges) {
            edgeCountsPerLine[edge.intersectingTextRowCount - 1][TextEdge.MID]++;
        }

        for (TextEdge edge : rightTextEdges) {
            edgeCountsPerLine[edge.intersectingTextRowCount - 1][TextEdge.RIGHT]++;
        }

        // now let's find the relevant edge type and the number of those edges we should look for
        // we'll only take a minimum of two edges to look for tables
        int relevantEdgeType = -1;
        int relevantEdgeCount = 0;
        for (int i=edgeCountsPerLine.length - 1; i>2; i--) {
            if (edgeCountsPerLine[i][TextEdge.LEFT] > 2 &&
                    edgeCountsPerLine[i][TextEdge.LEFT] >= edgeCountsPerLine[i][TextEdge.RIGHT] &&
                    edgeCountsPerLine[i][TextEdge.LEFT] >= edgeCountsPerLine[i][TextEdge.MID]) {
                relevantEdgeCount = edgeCountsPerLine[i][TextEdge.LEFT];
                relevantEdgeType = TextEdge.LEFT;
                break;
            }

            if (edgeCountsPerLine[i][TextEdge.RIGHT] > 1 &&
                    edgeCountsPerLine[i][TextEdge.RIGHT] >= edgeCountsPerLine[i][TextEdge.LEFT] &&
                    edgeCountsPerLine[i][TextEdge.RIGHT] >= edgeCountsPerLine[i][TextEdge.MID]) {
                relevantEdgeCount = edgeCountsPerLine[i][TextEdge.RIGHT];
                relevantEdgeType = TextEdge.RIGHT;
                break;
            }

            if (edgeCountsPerLine[i][TextEdge.MID] > 1 &&
                    edgeCountsPerLine[i][TextEdge.MID] >= edgeCountsPerLine[i][TextEdge.RIGHT] &&
                    edgeCountsPerLine[i][TextEdge.MID] >= edgeCountsPerLine[i][TextEdge.LEFT]) {
                relevantEdgeCount = edgeCountsPerLine[i][TextEdge.MID];
                relevantEdgeType = TextEdge.MID;
                break;
            }
        }

        return new RelevantEdges(relevantEdgeType, relevantEdgeCount);
    }

    private TextEdges getTextEdges(List<Line> lines) {

        // get all text edges (lines that align with the left, middle and right of chunks of text) that extend
        // uninterrupted over at least REQUIRED_TEXT_LINES_FOR_EDGE lines of text
        List<TextEdge> leftTextEdges = new ArrayList<TextEdge>();
        List<TextEdge> midTextEdges = new ArrayList<TextEdge>();
        List<TextEdge> rightTextEdges = new ArrayList<TextEdge>();

        Map<Integer, List<TextChunk>> currLeftEdges = new HashMap<Integer, List<TextChunk>>();
        Map<Integer, List<TextChunk>> currMidEdges = new HashMap<Integer, List<TextChunk>>();
        Map<Integer, List<TextChunk>> currRightEdges = new HashMap<Integer, List<TextChunk>>();

        for (Line textRow : lines) {
            for (TextChunk text : textRow.getTextElements()) {
                Integer left = new Integer((int)Math.floor(text.getLeft()));
                Integer right = new Integer((int)Math.floor(text.getRight()));
                Integer mid = new Integer(left + ((right - left)/2));

                // first put this chunk into any edge buckets it belongs to
                List<TextChunk> leftEdge = currLeftEdges.getOrDefault(left, new ArrayList<TextChunk>());
                leftEdge.add(text);
                currLeftEdges.put(left, leftEdge);

                List<TextChunk> midEdge = currMidEdges.getOrDefault(mid, new ArrayList<TextChunk>());
                midEdge.add(text);
                currMidEdges.put(mid, midEdge);

                List<TextChunk> rightEdge = currRightEdges.getOrDefault(right, new ArrayList<TextChunk>());
                rightEdge.add(text);
                currRightEdges.put(right, rightEdge);

                // now see if this text chunk blows up any other edges
                for (Iterator<Map.Entry<Integer, List<TextChunk>>> iterator = currLeftEdges.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry<Integer, List<TextChunk>> entry = iterator.next();
                    Integer key = entry.getKey();
                    if (key > left && key < right) {
                        iterator.remove();
                        List<TextChunk> edgeChunks = entry.getValue();
                        if (edgeChunks.size() >= REQUIRED_TEXT_LINES_FOR_EDGE) {
                            TextChunk first = edgeChunks.get(0);
                            TextChunk last = edgeChunks.get(edgeChunks.size() - 1);

                            TextEdge edge = new TextEdge(key, first.getTop(), key, last.getBottom());
                            edge.intersectingTextRowCount = edgeChunks.size();

                            leftTextEdges.add(edge);
                        }
                    }
                }

                for (Iterator<Map.Entry<Integer, List<TextChunk>>> iterator = currMidEdges.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry<Integer, List<TextChunk>> entry = iterator.next();
                    Integer key = entry.getKey();
                    if (key > left && key < right && Math.abs(key - mid) > 2) {
                        iterator.remove();
                        List<TextChunk> edgeChunks = entry.getValue();
                        if (edgeChunks.size() >= REQUIRED_TEXT_LINES_FOR_EDGE) {
                            TextChunk first = edgeChunks.get(0);
                            TextChunk last = edgeChunks.get(edgeChunks.size() - 1);

                            TextEdge edge = new TextEdge(key, first.getTop(), key, last.getBottom());
                            edge.intersectingTextRowCount = edgeChunks.size();

                            midTextEdges.add(edge);
                        }
                    }
                }

                for (Iterator<Map.Entry<Integer, List<TextChunk>>> iterator = currRightEdges.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry<Integer, List<TextChunk>> entry = iterator.next();
                    Integer key = entry.getKey();
                    if (key > left && key < right) {
                        iterator.remove();
                        List<TextChunk> edgeChunks = entry.getValue();
                        if (edgeChunks.size() >= REQUIRED_TEXT_LINES_FOR_EDGE) {
                            TextChunk first = edgeChunks.get(0);
                            TextChunk last = edgeChunks.get(edgeChunks.size() - 1);

                            TextEdge edge = new TextEdge(key, first.getTop(), key, last.getBottom());
                            edge.intersectingTextRowCount = edgeChunks.size();

                            rightTextEdges.add(edge);
                        }
                    }
                }
            }
        }

        // add the leftovers
        for (Integer key : currLeftEdges.keySet()) {
            List<TextChunk> edgeChunks = currLeftEdges.get(key);
            if (edgeChunks.size() >= REQUIRED_TEXT_LINES_FOR_EDGE) {
                TextChunk first = edgeChunks.get(0);
                TextChunk last = edgeChunks.get(edgeChunks.size() - 1);

                TextEdge edge = new TextEdge(key, first.getTop(), key, last.getBottom());
                edge.intersectingTextRowCount = edgeChunks.size();

                leftTextEdges.add(edge);
            }
        }

        for (Integer key : currMidEdges.keySet()) {
            List<TextChunk> edgeChunks = currMidEdges.get(key);
            if (edgeChunks.size() >= REQUIRED_TEXT_LINES_FOR_EDGE) {
                TextChunk first = edgeChunks.get(0);
                TextChunk last = edgeChunks.get(edgeChunks.size() - 1);

                TextEdge edge = new TextEdge(key, first.getTop(), key, last.getBottom());
                edge.intersectingTextRowCount = edgeChunks.size();

                midTextEdges.add(edge);
            }
        }

        for (Integer key : currRightEdges.keySet()) {
            List<TextChunk> edgeChunks = currRightEdges.get(key);
            if (edgeChunks.size() >= REQUIRED_TEXT_LINES_FOR_EDGE) {
                TextChunk first = edgeChunks.get(0);
                TextChunk last = edgeChunks.get(edgeChunks.size() - 1);

                TextEdge edge = new TextEdge(key, first.getTop(), key, last.getBottom());
                edge.intersectingTextRowCount = edgeChunks.size();

                rightTextEdges.add(edge);
            }
        }

        return new TextEdges(leftTextEdges, midTextEdges, rightTextEdges);
    }

    private List<Line2D.Float> mergeHorizontalEdges(List<Line2D.Float> horizontalEdges) {
        if (horizontalEdges.size() == 0) {
            return horizontalEdges;
        }

        List<Line2D.Float> horizontalRulings = new ArrayList<Line2D.Float>();

        // sort rulings by top-leftmost first
        Collections.sort(horizontalEdges, new Comparator<Line2D.Float>() {
            @Override
            public int compare(Line2D.Float o1, Line2D.Float o2) {
                if (o1.equals(o2)) {
                    return 0;
                }

                if (o1.y1 == o2.y1) {
                    return Float.compare(o1.x1, o2.x1);
                } else {
                    return Float.compare(o1.y1, o2.y1);
                }
            }
        });

        Line2D.Float currentRuling = horizontalEdges.get(0);
        for (int i=1; i<horizontalEdges.size(); i++) {
            Line2D.Float nextEdge = horizontalEdges.get(i);

            if (currentRuling.y1 == nextEdge.y1 &&
                    nextEdge.x1 >= currentRuling.x1 &&
                    nextEdge.x1 <= currentRuling.x2) {
                // this line segment can be part of the current line
                currentRuling.x2 = Math.max(nextEdge.x2, currentRuling.x2);
            } else {
                // store the complete line and continue
                horizontalRulings.add(currentRuling);
                currentRuling = nextEdge;
            }
        }

        horizontalRulings.add(currentRuling);

        return horizontalRulings;
    }

    private List<Line2D.Float> mergeVerticalEdges(List<Line2D.Float> verticalEdges) {
        if (verticalEdges.size() == 0) {
            return verticalEdges;
        }

        List<Line2D.Float> verticalRulings = new ArrayList<Line2D.Float>();

        // sort by left-topmost first
        Collections.sort(verticalEdges, new Comparator<Line2D.Float>() {
            @Override
            public int compare(Line2D.Float o1, Line2D.Float o2) {
                if (o1.equals(o2)) {
                    return 0;
                }

                if (o1.x1 == o2.x1) {
                    return Float.compare(o1.y1, o2.y1);
                } else {
                    return Float.compare(o1.x1, o2.x1);
                }
            }
        });

        Line2D.Float currentRuling = verticalEdges.get(0);
        for (int i=1; i<verticalEdges.size(); i++) {
            Line2D.Float nextEdge = verticalEdges.get(i);

            if (currentRuling.x1 == nextEdge.x1 &&
                    nextEdge.y1 >= currentRuling.y1 &&
                    nextEdge.y1 <= currentRuling.y2) {
                // line segment is part of the current line
                currentRuling.y2 = Math.max(nextEdge.y2, currentRuling.y2);
            } else {
                // store the complete line and continue
                verticalRulings.add(currentRuling);
                currentRuling = nextEdge;
            }
        }

        verticalRulings.add(currentRuling);

        return verticalRulings;
    }

    private List<Rectangle> getTableAreasFromCells(List<Rectangle> cells) {
        List<List<Rectangle>> cellGroups = new ArrayList<List<Rectangle>>();
        for (Rectangle cell : cells) {
            boolean addedToGroup = false;

            cellCheck:
            for (List<Rectangle> cellGroup : cellGroups) {
                for (Rectangle groupCell : cellGroup) {
                    Point2D[] groupCellCorners = groupCell.getPoints();
                    Point2D[] candidateCorners = cell.getPoints();

                    for (int i=0; i<candidateCorners.length; i++) {
                        for (int j=0; j<groupCellCorners.length; j++) {
                            if (candidateCorners[i].distance(groupCellCorners[j]) < CELL_CORNER_DISTANCE_MAXIMUM) {
                                cellGroup.add(cell);
                                addedToGroup = true;
                                break cellCheck;
                            }
                        }
                    }
                }
            }

            if (!addedToGroup) {
                ArrayList<Rectangle> cellGroup = new ArrayList<Rectangle>();
                cellGroup.add(cell);
                cellGroups.add(cellGroup);
            }
        }

        // create table areas based on cell group
        List<Rectangle> tableAreas = new ArrayList<Rectangle>();
        for (List<Rectangle> cellGroup : cellGroups) {
            // less than four cells should not make a table
            if (cellGroup.size() < REQUIRED_CELLS_FOR_TABLE) {
                continue;
            }

            float top = Float.MAX_VALUE;
            float left = Float.MAX_VALUE;
            float bottom = Float.MIN_VALUE;
            float right = Float.MIN_VALUE;

            for (Rectangle cell : cellGroup) {
                if (cell.getTop() < top) top = cell.getTop();
                if (cell.getLeft() < left) left = cell.getLeft();
                if (cell.getBottom() > bottom) bottom = cell.getBottom();
                if (cell.getRight() > right) right = cell.getRight();
            }

            tableAreas.add(new Rectangle(top, left, right - left, bottom - top));
        }

        return tableAreas;
    }

    private List<Rectangle> findRectangles(
            Set<Point2D.Float> crossingPoints,
            List<Line2D.Float> horizontalEdges,
            List<Line2D.Float> verticalEdges) {

        List<Rectangle> foundRectangles = new ArrayList<Rectangle>();

        ArrayList<Point2D.Float> sortedPoints = new ArrayList<Point2D.Float>(crossingPoints);

        // sort all points by y value and then x value - this means that the first element
        // is always the top-leftmost point
        Collections.sort(sortedPoints, pointComparator);

        for (int i=0; i<sortedPoints.size(); i++) {
            Point2D.Float topLeftPoint = sortedPoints.get(i);

            ArrayList<Point2D.Float> pointsBelow = new ArrayList<Point2D.Float>();
            ArrayList<Point2D.Float> pointsRight = new ArrayList<Point2D.Float>();

            for (int j=i+1; j<sortedPoints.size(); j++) {
                Point2D.Float checkPoint = sortedPoints.get(j);
                if (topLeftPoint.getX() == checkPoint.getX() && topLeftPoint.getY() < checkPoint.getY()) {
                    pointsBelow.add(checkPoint);
                } else if (topLeftPoint.getY() == checkPoint.getY() && topLeftPoint.getX() < checkPoint.getX()) {
                    pointsRight.add(checkPoint);
                }
            }

            nextCrossingPoint:
            for (Point2D.Float belowPoint : pointsBelow) {
                if (!this.edgeExistsBetween(topLeftPoint, belowPoint, verticalEdges)) {
                    break nextCrossingPoint;
                }

                for (Point2D.Float rightPoint : pointsRight) {
                    if (!this.edgeExistsBetween(topLeftPoint, rightPoint, horizontalEdges)) {
                        break nextCrossingPoint;
                    }

                    Point2D.Float bottomRightPoint = new Point2D.Float(rightPoint.x, belowPoint.y);

                    if (sortedPoints.contains(bottomRightPoint)
                            && this.edgeExistsBetween(belowPoint, bottomRightPoint, horizontalEdges)
                            && this.edgeExistsBetween(rightPoint, bottomRightPoint, verticalEdges)) {

                        foundRectangles.add(new Rectangle(
                                topLeftPoint.y,
                                topLeftPoint.x,
                                bottomRightPoint.x - topLeftPoint.x,
                                bottomRightPoint.y - topLeftPoint.y)
                        );

                        break nextCrossingPoint;
                    }
                }
            }
        }

        return foundRectangles;
    }

    private boolean edgeExistsBetween(Point2D.Float p1, Point2D.Float p2, List<Line2D.Float> edges) {
        for (Line2D.Float edge : edges) {
            if (p1.x >= edge.x1 && p1.x <= edge.x2 && p1.y >= edge.y1 && p1.y <= edge.y2
                    && p2.x >= edge.x1 && p2.x <= edge.x2 && p2.y >= edge.y1 && p2.y <= edge.y2) {
                return true;
            }
        }

        return false;
    }

    private Set<Point2D.Float> getCrossingPoints(List<Line2D.Float> horizontalEdges, List<Line2D.Float> verticalEdges) {
        Set<Point2D.Float> crossingPoints = new HashSet<Point2D.Float>();

        for (Line2D.Float horizontalEdge : horizontalEdges) {
            for (Line2D.Float verticalEdge : verticalEdges) {
                if (horizontalEdge.intersectsLine(verticalEdge)) {
                    crossingPoints.add(new Point2D.Float(verticalEdge.x1, horizontalEdge.y1));
                }
            }
        }

        return crossingPoints;
    }

    private List<Line2D.Float> getHorizontalRulings(BufferedImage image) {

        // get all horizontal edges, which we'll define as a change in grayscale colour
        // along a straight line of a certain length
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

                    while (lineX < width) {
                        int[] linePixel = r.getPixel(lineX, y, (int[]) null);
                        int[] abovePixel = r.getPixel(lineX, y - 1, (int[]) null);

                        if (Math.abs(linePixel[0] - abovePixel[0]) <= GRAYSCALE_INTENSITY_THRESHOLD
                                || Math.abs(currPixel[0] - linePixel[0]) > GRAYSCALE_INTENSITY_THRESHOLD) {
                            break;
                        }

                        lineX++;
                    }

                    int endX = lineX - 1;
                    int lineWidth = endX - x;
                    if (lineWidth > HORIZONTAL_EDGE_WIDTH_MINIMUM) {
                        horizontalRulings.add(new Line2D.Float(x, y, endX, y));
                    }
                }

                lastPixel = currPixel;
            }
        }

        return horizontalRulings;
    }

    private List<Line2D.Float> getVerticalRulings(BufferedImage image) {

        // get all vertical edges, which we'll define as a change in grayscale colour
        // along a straight line of a certain length
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

                    while (lineY < height) {
                        int[] linePixel = r.getPixel(x, lineY, (int[]) null);
                        int[] leftPixel = r.getPixel(x - 1, lineY, (int[]) null);

                        if (Math.abs(linePixel[0] - leftPixel[0]) <= GRAYSCALE_INTENSITY_THRESHOLD
                                || Math.abs(currPixel[0] - linePixel[0]) > GRAYSCALE_INTENSITY_THRESHOLD) {
                            break;
                        }

                        lineY++;
                    }

                    int endY = lineY - 1;
                    int lineLength = endY - y;
                    if (lineLength > VERTICAL_EDGE_HEIGHT_MINIMUM) {
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

    private void debug(Collection<? extends Shape> shapes) {
        this.debug(shapes, false);
    }

    private void debug(Collection<? extends Shape> shapes, boolean twox) {
        Color[] COLORS = { new Color(27, 158, 119),
                new Color(217, 95, 2), new Color(117, 112, 179),
                new Color(231, 41, 138), new Color(102, 166, 30) };

        try {
            int res = twox ? 144 : 72;

            BufferedImage image = this.currentPDPage.convertToImage(BufferedImage.TYPE_INT_RGB, res);
            Graphics2D g = (Graphics2D) image.getGraphics();

            g.setStroke(new BasicStroke(2f));
            int i = 0;

            for (Shape s : shapes) {
                g.setColor(COLORS[(i++) % 5]);
                g.draw(s);
            }

            String debugFileOut = this.currentDoc.getAbsolutePath().replace(".pdf", "-" + this.currentPage.getPageNumber() + ".jpg");

            ImageIOUtil.writeImage(image, debugFileOut, res);
        } catch (IOException e) {
        }
    }
}
