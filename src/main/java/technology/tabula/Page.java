package technology.tabula;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;

@SuppressWarnings("serial")
// TODO: this class should probably be called "PageArea" or something like that
public class Page extends Rectangle {

    private Integer rotation;
    private int pageNumber;
    private List<TextElement> texts;
    private List<Ruling> rulings, cleanRulings = null, verticalRulingLines = null, horizontalRulingLines = null;
    private float minCharWidth;
    private float minCharHeight;
    private RectangleSpatialIndex<TextElement> spatial_index;
    private PDPage pdPage;

    public Page(float top, float left, float width, float height, int rotation, int page_number, PDPage pdPage) {
        super(top, left, width, height);
        this.rotation = rotation;
        this.pageNumber = page_number;
        this.pdPage = pdPage;
    }
    
    public Page(float top, float left, float width, float height, int rotation, int page_number, PDPage pdPage,
            List<TextElement> characters, List<Ruling> rulings) {

        this(top, left, width, height, rotation, page_number, pdPage);
        this.texts = characters;
        this.rulings = rulings;
    }


    public Page(float top, float left, float width, float height, int rotation, int page_number, PDPage pdPage,
            List<TextElement> characters, List<Ruling> rulings,
            float minCharWidth, float minCharHeight, RectangleSpatialIndex<TextElement> index) {

        this(top, left, width, height, rotation, page_number, pdPage, characters, rulings);
        this.minCharHeight = minCharHeight;
        this.minCharWidth = minCharWidth;
        this.spatial_index = index;
    }

    public Page getArea(Rectangle area) {
        List<TextElement> t = getText(area);
        float min_char_width  = 7;
        float min_char_height = 7;

        if(t.size() > 0){
            min_char_width = Collections.min(t, new Comparator<TextElement>() {
                                    @Override
                                    public int compare(TextElement te1, TextElement te2) {
                                        return java.lang.Float.compare(te1.width, te2.width);
                                 }}).width;
            min_char_height = Collections.min(t, new Comparator<TextElement>() {
                                        @Override
                                        public int compare(TextElement te1, TextElement te2) {
                                            return java.lang.Float.compare(te1.height, te2.height);
                                  }}).height;
        }
        Page rv = new Page(
                area.getTop(),
                area.getLeft(),
                (float) area.getWidth(),
                (float) area.getHeight(),
                rotation,
                pageNumber,
                pdPage,
                t,
                Ruling.cropRulingsToArea(getRulings(), area),
                min_char_width,
                min_char_height,                
                spatial_index);
        
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
        return this.getArea(area);
    }
    
    public List<TextElement> getText() {
        return texts;
    }
    
    public List<TextElement> getText(Rectangle area) {
        return this.spatial_index.contains(area);
    }
    
    /** @deprecated use {@linkplain #getText(Rectangle)} instead  */
    @Deprecated public List<TextElement> getText(float top, float left, float bottom, float right) {
        return this.getText(new Rectangle(top, left, right - left, bottom - top));
    }

    public Integer getRotation() {
        return rotation;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    /** @deprecated use {@linkplain #getText()} instead  */
    @Deprecated public List<TextElement> getTexts() {
        return texts;
    }
    
    /**
     * Returns the minimum bounding box that contains all the TextElements on this Page
     */
    public Rectangle getTextBounds() {
        List<TextElement> texts = this.getText();
        if (!texts.isEmpty()) {
            return Utils.bounds(texts);
        }
        else {
            return new Rectangle();
        }
        
    }

    public List<Ruling> getRulings() {
        if (this.cleanRulings != null) {
            return this.cleanRulings;
        }
        
        if (this.rulings == null || this.rulings.isEmpty()) {
            this.verticalRulingLines = new ArrayList<>();
            this.horizontalRulingLines = new ArrayList<>();
            return new ArrayList<>();
        }
        
        Utils.snapPoints(this.rulings, this.minCharWidth, this.minCharHeight);
        
        List<Ruling> vrs = new ArrayList<>();
        for (Ruling vr: this.rulings) {
            if (vr.vertical()) {
                vrs.add(vr);
            }
        }
        this.verticalRulingLines = Ruling.collapseOrientedRulings(vrs);
        
        List<Ruling> hrs = new ArrayList<>(); 
        for (Ruling hr: this.rulings) {
            if (hr.horizontal()) {
                hrs.add(hr);
            }
        }
        this.horizontalRulingLines = Ruling.collapseOrientedRulings(hrs);
        
        this.cleanRulings = new ArrayList<>(this.verticalRulingLines);
        this.cleanRulings.addAll(this.horizontalRulingLines);
        
        return this.cleanRulings;
        
    }
    
    public List<Ruling> getVerticalRulings() {
        if (this.verticalRulingLines != null) {
            return this.verticalRulingLines;
        }
        this.getRulings();
        return this.verticalRulingLines;
    }
    
    public List<Ruling> getHorizontalRulings() {
        if (this.horizontalRulingLines != null) {
            return this.horizontalRulingLines;
        }
        this.getRulings();
        return this.horizontalRulingLines;
    }
    
    public void addRuling(Ruling r) {
        if (r.oblique()) {
            throw new UnsupportedOperationException("Can't add an oblique ruling");
        }
        this.rulings.add(r);
        // clear caches
        this.verticalRulingLines = null;
        this.horizontalRulingLines = null;
        this.cleanRulings = null;
    }
    
    public List<Ruling> getUnprocessedRulings() {
        return this.rulings;
    }

    /** @deprecated with no replacement  */
    @Deprecated public float getMinCharWidth() {
        return minCharWidth;
    }

    /** @deprecated with no replacement  */
    @Deprecated public float getMinCharHeight() {
        return minCharHeight;
    }

    public PDPage getPDPage() {
      return pdPage;
    }

    /** @deprecated with no replacement  */
    @Deprecated public RectangleSpatialIndex<TextElement> getSpatialIndex() {
        return this.spatial_index;
    }
    
    /** @deprecated with no replacement  */
    @Deprecated public boolean hasText() {
        return this.texts.size() > 0;
    }
    
    
}
