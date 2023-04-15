package technology.tabula;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import static java.lang.Float.compare;
import static java.util.Collections.min;

@SuppressWarnings("serial")
// TODO: this class should probably be called "PageArea" or something like that
public class Page extends Rectangle {

    private int number;
    private Integer rotation;
    private float minCharWidth;
    private float minCharHeight;

    private List<TextElement> textElements;

    // TODO: Create a class for 'List <Ruling>' that encapsulates all of these lists and their behaviors?
    private Rulings rulings;
    private PDPage pdPage;
    private PDDocument pdDoc;

    private RectangleSpatialIndex<TextElement> spatialIndex;

    private static final float DEFAULT_MIN_CHAR_LENGTH = 7;

    private Page(
            PageDims pageDims,
            int rotation,
            int number,
            PDPage pdPage,
            PDDocument doc,
            List<TextElement> characters,
            List<Ruling> rulings,
            float minCharWidth,
            float minCharHeight,
            RectangleSpatialIndex<TextElement> index
    ) {
        super(pageDims.getTop(), pageDims.getLeft(), pageDims.getWidth(), pageDims.getHeight());
        this.rotation = rotation;
        this.number = number;
        this.pdPage = pdPage;
        this.pdDoc = doc;
        this.textElements = characters;
        this.rulings = new Rulings(rulings);
        this.minCharWidth = minCharWidth;
        this.minCharHeight = minCharHeight;
        this.spatialIndex = index;
    }

  /**
   *
   * @deprecated use {@link Builder} instead
   */
    @Deprecated
    public Page(float top, float left, float width, float height, int rotation, int number, PDPage pdPage, PDDocument doc) {
      super(top, left, width, height);
      this.rotation = rotation;
      this.number = number;
      this.pdPage = pdPage;
      this.pdDoc = doc;
    }

   /**
    *
    * @deprecated use {@link Builder} instead
    */
    public Page(float top, float left, float width, float height, int rotation, int number, PDPage pdPage, PDDocument doc,
                List<TextElement> characters, List<Ruling> rulings) {
      this(top, left, width, height, rotation, number, pdPage, doc);
      this.textElements = characters;
      this.rulings = new Rulings(rulings);
    }

   /**
    *
    * @deprecated use {@link Builder} instead
    */
    public Page(float top, float left, float width, float height, int rotation, int number, PDPage pdPage, PDDocument doc,
                ObjectExtractorStreamEngine streamEngine, TextStripper textStripper) {
      this(top, left, width, height, rotation, number, pdPage, doc, textStripper.getTextElements(), streamEngine.rulings);
      this.minCharWidth = textStripper.getMinCharWidth();
      this.minCharHeight = textStripper.getMinCharHeight();
      this.spatialIndex = textStripper.getSpatialIndex();
    }



   /**
    *
    * @deprecated use {@link Builder} instead
    */
    public Page(float top, float left, float width, float height, int rotation, int number, PDPage pdPage, PDDocument doc,
                List<TextElement> characters, List<Ruling> rulings,
                float minCharWidth, float minCharHeight, RectangleSpatialIndex<TextElement> index) {
      this(top, left, width, height, rotation, number, pdPage, doc, characters, rulings);
      this.minCharHeight = minCharHeight;
      this.minCharWidth = minCharWidth;
      this.spatialIndex = index;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    public Page getArea(Rectangle area) {
        List<TextElement> areaTextElements = getText(area);

        float minimumCharWidth = getMinimumCharWidthFrom(areaTextElements);
        float minimumCharHeight = getMinimumCharHeightFrom(areaTextElements);

        final Page page = Page.Builder.newInstance()
                .withPageDims(PageDims.of(area.getTop(), area.getLeft(), (float) area.getWidth(), (float) area.getHeight()))
                .withRotation(rotation)
                .withNumber(number)
                .withPdPage(pdPage)
                .withPdDocument(pdDoc)
                .withTextElements(areaTextElements)
                .withRulings(Ruling.cropRulingsToArea(rulings.getRulings(), area))
                .withMinCharWidth(minimumCharWidth)
                .withMinCharHeight(minimumCharHeight)
                .withIndex(spatialIndex)
                .build();

        addBorderRulingsTo(page);

        return page;
    }

    private float getMinimumCharWidthFrom(List<TextElement> areaTextElements) {
        if (!areaTextElements.isEmpty()) {
            return min(areaTextElements, (te1, te2) -> compare(te1.width, te2.width)).width;
        }
        return DEFAULT_MIN_CHAR_LENGTH;
    }

    private float getMinimumCharHeightFrom(List<TextElement> areaTextElements) {
        if (!areaTextElements.isEmpty()) {
            return min(areaTextElements, (te1, te2) -> compare(te1.height, te2.height)).height;
        }
        return DEFAULT_MIN_CHAR_LENGTH;
    }

    private void addBorderRulingsTo(Page page) {
        Point2D.Double leftTop = new Point2D.Double(page.getLeft(), page.getTop()),
                rightTop = new Point2D.Double(page.getRight(), page.getTop()),
                rightBottom = new Point2D.Double(page.getRight(), page.getBottom()),
                leftBottom = new Point2D.Double(page.getLeft(), page.getBottom());
        page.addRuling(new Ruling(leftTop, rightTop));
        page.addRuling(new Ruling(rightTop, rightBottom));
        page.addRuling(new Ruling(rightBottom, leftBottom));
        page.addRuling(new Ruling(leftBottom, leftTop));
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
        List<Ruling> rulings = this.rulings.getRulings();

        if (rulings != null && !rulings.isEmpty()) {
            Utils.snapPoints(rulings, minCharWidth, minCharHeight);;
        }
        return rulings;
    }

    public List<Ruling> getVerticalRulings() {
        return rulings.getVerticalRulings();
    }

    public List<Ruling> getHorizontalRulings() {
        return rulings.getHorizontalRulings();
    }

    public void addRuling(Ruling ruling) {
        rulings.addRuling(ruling);
    }

    public List<Ruling> getUnprocessedRulings() {
        return rulings.getUnprocessedRulings();
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

    public static class Builder {
        private PageDims pageDims;
        private int rotation;
        private int number;
        private PDPage pdPage;
        private PDDocument pdDocument;
        private List<TextElement> textElements;
        private List<Ruling> rulings;
        private float minCharWidth;
        private float minCharHeight;
        private RectangleSpatialIndex<TextElement> index;

        private Builder() {}

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder withPageDims(PageDims pageDims) {
            this.pageDims = pageDims;

            return this;
        }

        public Builder withRotation(int rotation) {
            this.rotation = rotation;

            return this;
        }

        public Builder withNumber(int number) {
            this.number = number;

            return this;
        }

        public Builder withPdPage(PDPage pdPage) {
            this.pdPage = pdPage;

            return this;
        }

        public Builder withPdDocument(PDDocument pdDocument) {
            this.pdDocument = pdDocument;

            return this;
        }

        public Builder withTextElements(List<TextElement> textElements) {
            this.textElements = textElements;

            return this;
        }

        public Builder withRulings(List<Ruling> rulings) {
            this.rulings = rulings;

            return this;
        }

        public Builder withMinCharWidth(float minCharWidth) {
            this.minCharWidth = minCharWidth;

            return this;
        }

        public Builder withMinCharHeight(float minCharHeight) {
            this.minCharHeight = minCharHeight;

            return this;
        }

        public Builder withIndex(RectangleSpatialIndex<TextElement> index) {
            this.index = index;

            return this;
        }

        public Page build() {
            return new Page(pageDims, rotation, number, pdPage, pdDocument, textElements, rulings, minCharWidth, minCharHeight, index);
        }
    }
}
