package technology.tabula;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

@SuppressWarnings("serial")
// TODO: this class should probably be called "PageArea" or something like that
public class Page extends Rectangle {

    private int number;
    private Integer rotation;
    private float minCharWidth;
    private float minCharHeight;

    private List<TextElement> textElements;

    // TODO: Create a class for 'List <Ruling>' that encapsulates all of these lists and their behaviors?
    private List<Ruling> rulings,
            cleanRulings = null,
            verticalRulingLines = null,
            horizontalRulingLines = null;

    private PDPage pdPage;
    private PDDocument pdDoc;

    private RectangleSpatialIndex<TextElement> spatialIndex;

    public Page(float top, float left, float width, float height, int rotation, int pageNumber, PDPage pdPage, PDDocument doc) {
        super(top, left, width, height);
        this.rotation = rotation;
        this.number = pageNumber;
        this.pdPage = pdPage;
        this.pdDoc = doc;
    }

    public Page(float top, float left, float width, float height, int rotation, int pageNumber, PDPage pdPage, PDDocument doc,
                List<TextElement> characters, List<Ruling> rulings) {
        this(top, left, width, height, rotation, pageNumber, pdPage, doc);
        this.textElements = characters;
        this.rulings = rulings;
    }

    public Page(float top, float left, float width, float height, int rotation, int number, PDPage pdPage, PDDocument doc,
                ObjectExtractorStreamEngine streamEngine, TextStripper textStripper) {
        this(top, left, width, height, rotation, number, pdPage, doc, textStripper.textElements, streamEngine.rulings);
        this.minCharWidth = textStripper.minCharWidth;
        this.minCharHeight = textStripper.minCharHeight;
        this.spatialIndex = textStripper.spatialIndex;
    }

    public Page(float top, float left, float width, float height, int rotation, int pageNumber, PDPage pdPage, PDDocument doc,
                List<TextElement> characters, List<Ruling> rulings,
                float minCharWidth, float minCharHeight, RectangleSpatialIndex<TextElement> index) {
        this(top, left, width, height, rotation, pageNumber, pdPage, doc, characters, rulings);
        this.minCharHeight = minCharHeight;
        this.minCharWidth = minCharWidth;
        this.spatialIndex = index;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public Page getArea(Rectangle area) {
        List<TextElement> t = getText(area);
        float min_char_width = 7;
        float min_char_height = 7;

        if (t.size() > 0) {
            min_char_width = Collections.min(t, new Comparator<TextElement>() {
                @Override
                public int compare(TextElement te1, TextElement te2) {
                    return java.lang.Float.compare(te1.width, te2.width);
                }
            }).width;
            min_char_height = Collections.min(t, new Comparator<TextElement>() {
                @Override
                public int compare(TextElement te1, TextElement te2) {
                    return java.lang.Float.compare(te1.height, te2.height);
                }
            }).height;
        }
        Page rv = new Page(
                area.getTop(),
                area.getLeft(),
                (float) area.getWidth(),
                (float) area.getHeight(),
                rotation, number,
                pdPage,
                pdDoc,
                t,
                Ruling.cropRulingsToArea(getRulings(), area),
                min_char_width,
                min_char_height,
                spatialIndex);

        rv.addRuling(new Ruling(
                new Point2D.Double(rv.getLeft(),
                        rv.getTop()),
                new Point2D.Double(rv.getRight(),
                        rv.getTop())));
        rv.addRuling(new Ruling(
                new Point2D.Double(rv.getRight(),
                        rv.getTop()),
                new Point2D.Double(rv.getRight(),
                        rv.getBottom())));
        rv.addRuling(new Ruling(
                new Point2D.Double(rv.getRight(),
                        rv.getBottom()),
                new Point2D.Double(rv.getLeft(),
                        rv.getBottom())));
        rv.addRuling(new Ruling(
                new Point2D.Double(rv.getLeft(),
                        rv.getBottom()),
                new Point2D.Double(rv.getLeft(),
                        rv.getTop())));

        return rv;
    }

    public Page getArea(float top, float left, float bottom, float right) {
        Rectangle area = new Rectangle(top, left, right - left, bottom - top);
        return getArea(area);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public Integer getRotation() {
        return rotation;
    }

    public int getPageNumber() {
        return number;
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public float getMinCharWidth() {
        return minCharWidth;
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public float getMinCharHeight() {
        return minCharHeight;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public List<TextElement> getText() {
        return textElements;
    }

    public List<TextElement> getText(Rectangle area) {
        return spatialIndex.contains(area);
    }

    /**
     * @deprecated use {@linkplain #getText(Rectangle)} instead
     */
    @Deprecated
    public List<TextElement> getText(float top, float left, float bottom, float right) {
        return getText(new Rectangle(top, left, right - left, bottom - top));
    }

    /**
     * @deprecated use {@linkplain #getText()} instead
     */
    @Deprecated
    public List<TextElement> getTexts() {
        return textElements;
    }

    /**
     * Returns the minimum bounding box that contains all the TextElements on this Page
     */
    public Rectangle getTextBounds() {
        List<TextElement> texts = this.getText();
        if (!texts.isEmpty()) {
            return Utils.bounds(texts);
        } else {
            return new Rectangle();
        }
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public boolean hasText() {
        return textElements.size() > 0;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public List<Ruling> getRulings() {
        if (cleanRulings != null) {
            return cleanRulings;
        }

        if (rulings == null || rulings.isEmpty()) {
            verticalRulingLines = new ArrayList<>();
            horizontalRulingLines = new ArrayList<>();
            return new ArrayList<>();
        }

        // TODO: Move as a static method to the Ruling class?
        Utils.snapPoints(rulings, minCharWidth, minCharHeight);

        verticalRulingLines = getCollapsedVerticalRulings();
        horizontalRulingLines = getCollapsedHorizontalRulings();

        cleanRulings = new ArrayList<>(verticalRulingLines);
        cleanRulings.addAll(horizontalRulingLines);

        return cleanRulings;
    }

    // TODO: Create a class for 'List <Ruling>' and encapsulate these behaviors within it?
    private List<Ruling> getCollapsedVerticalRulings() {
        List<Ruling> verticalRulings = new ArrayList<>();
        for (Ruling ruling : rulings) {
            if (ruling.vertical()) {
                verticalRulings.add(ruling);
            }
        }
        return Ruling.collapseOrientedRulings(verticalRulings);
    }

    private List<Ruling> getCollapsedHorizontalRulings() {
        List<Ruling> horizontalRulings = new ArrayList<>();
        for (Ruling ruling : rulings) {
            if (ruling.horizontal()) {
                horizontalRulings.add(ruling);
            }
        }
        return Ruling.collapseOrientedRulings(horizontalRulings);
    }

    public List<Ruling> getVerticalRulings() {
        if (verticalRulingLines != null) {
            return verticalRulingLines;
        }
        getRulings();
        return verticalRulingLines;
    }

    public List<Ruling> getHorizontalRulings() {
        if (horizontalRulingLines != null) {
            return horizontalRulingLines;
        }
        getRulings();
        return horizontalRulingLines;
    }

    public void addRuling(Ruling ruling) {
        if (ruling.oblique()) {
            throw new UnsupportedOperationException("Can't add an oblique ruling.");
        }
        rulings.add(ruling);
        // Clear caches:
        verticalRulingLines = null;
        horizontalRulingLines = null;
        cleanRulings = null;
    }

    public List<Ruling> getUnprocessedRulings() {
        return rulings;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public PDPage getPDPage() {
        return pdPage;
    }

    public PDDocument getPDDoc() {
        return pdDoc;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public RectangleSpatialIndex<TextElement> getSpatialIndex() {
        return spatialIndex;
    }

}
