package org.nerdpower.tabula;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class Page extends Rectangle {

    private Integer rotation;
    private int pageNumber;
    private List<TextElement> texts;
    private List<Ruling> rulings, cleanRulings = null, verticalRulingLines = null, horizontalRulingLines = null;
    private float minCharWidth;
    private float minCharHeight;
    private TextElementIndex spatial_index;

    public Page(float width, float height, Integer rotation, int page_number) {
        super();
        this.setRect(0, 0, width, height);
        this.rotation = rotation;
        this.pageNumber = page_number;
    }
    
    public Page(float width, float height, Integer rotation, int page_number,
            List<TextElement> characters, List<Ruling> rulings) {

        this(width, height, rotation, page_number);
        this.texts = characters;
        this.rulings = rulings;
    }


    public Page(float width, float height, Integer rotation, int page_number,
            List<TextElement> characters, List<Ruling> rulings,
            float minCharWidth, float minCharHeight, TextElementIndex index) {

        this(width, height, rotation, page_number, characters, rulings);
        this.minCharHeight = minCharHeight;
        this.minCharWidth = minCharWidth;
        this.spatial_index = index;
    }

    
    public Page getArea(Rectangle2D area) {
        List<TextElement> t = getText(area);
        
        return new Page((float) area.getWidth(),
                        (float) area.getHeight(),
                        rotation,
                        pageNumber,
                        t,
                        Ruling.cropRulingsToArea(getRulings(), area),

                        Collections.min(t, new Comparator<TextElement>() {
                            @Override
                            public int compare(TextElement te1, TextElement te2) {
                                return java.lang.Double.compare(te1.width, te2.width);
                            }}).width,
                        
                        Collections.min(t, new Comparator<TextElement>() {
                                @Override
                                public int compare(TextElement te1, TextElement te2) {
                                    return java.lang.Double.compare(te1.height, te2.height);
                        }}).height,
                        
                        spatial_index);
    }
    
    public Page getArea(float top, float left, float bottom, float right) {
        Rectangle2D.Float area = new Rectangle2D.Float(left, top, Math.abs(right - left), Math.abs(bottom - top));
        return this.getArea(area);
    }
    
    public List<TextElement> getText() {
        return texts;
    }
    
    public List<TextElement> getText(Rectangle2D area) {
        return this.spatial_index.contains(area);
    }
    
    public List<TextElement> getText(float top, float left, float bottom, float right) {
        Rectangle2D.Float area = new Rectangle2D.Float(left, top, Math.abs(right - left), Math.abs(bottom - top));
        return this.getText(area);
    }

    public Integer getRotation() {
        return rotation;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public List<TextElement> getTexts() {
        return texts;
    }

    public List<Ruling> getRulings() {
        if (this.cleanRulings != null) {
            return this.rulings;
        }
        
        if (this.rulings == null || this.rulings.isEmpty()) {
            return new ArrayList<Ruling>();
        }
        
        this.snapPoints();
        
        List<Ruling> vrs = new ArrayList<Ruling>();
        for (Ruling vr: this.rulings) {
            if (vr.vertical()) {
                vrs.add(vr);
            }
        }
        this.verticalRulingLines = Ruling.collapseOrientedRulings(vrs);
        
        List<Ruling> hrs = new ArrayList<Ruling>(); 
        for (Ruling hr: this.rulings) {
            if (hr.horizontal()) {
                hrs.add(hr);
            }
        }
        this.horizontalRulingLines = Ruling.collapseOrientedRulings(hrs);
        
        this.cleanRulings = new ArrayList<Ruling>(this.verticalRulingLines);
        this.cleanRulings.addAll(this.horizontalRulingLines);
        
        return this.rulings;
        
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


    public float getMinCharWidth() {
        return minCharWidth;
    }

    public float getMinCharHeight() {
        return minCharHeight;
    }
    
    public TextElementIndex getSpatialIndex() {
        return this.spatial_index;
    }
    
    public void snapPoints() {

        // collect points and keep a Line -> p1,p2 map
        Map<Ruling, Point2D[]> linesToPoints = new HashMap<Ruling, Point2D[]>();
        List<Point2D> points = new ArrayList<Point2D>();
        for (Ruling r: this.rulings) {
            Point2D p1 = r.getP1();
            Point2D p2 = r.getP2();
            linesToPoints.put(r, new Point2D[] { p1, p2 });
            points.add(p1);
            points.add(p2);
        }
        
        // snap by X
        Collections.sort(points, new Comparator<Point2D>() {
            @Override
            public int compare(Point2D arg0, Point2D arg1) {
                return java.lang.Double.compare(arg0.getX(), arg1.getX());
            }
        });
        
        List<List<Point2D>> groupedPoints = new ArrayList<List<Point2D>>();
        groupedPoints.add(new ArrayList<Point2D>(Arrays.asList(new Point2D[] { points.get(0) })));
        
        for (Point2D p: points.subList(1, points.size() - 1)) {
            List<Point2D> last = groupedPoints.get(groupedPoints.size() - 1);
            if (Math.abs(p.getX() - last.get(0).getX()) < this.minCharWidth) {
                groupedPoints.get(groupedPoints.size() - 1).add(p);
            }
            else {
                groupedPoints.add(new ArrayList<Point2D>(Arrays.asList(new Point2D[] { p })));
            }
        }
        
        for(List<Point2D> group: groupedPoints) {
            float avgLoc = 0;
            for(Point2D p: group) {
                avgLoc += p.getX();
            }
            avgLoc /= group.size();
            for(Point2D p: group) {
                p.setLocation(avgLoc, p.getY());
            }
        }
        // ---

        // snap by Y
        Collections.sort(points, new Comparator<Point2D>() {
            @Override
            public int compare(Point2D arg0, Point2D arg1) {
                return java.lang.Double.compare(arg0.getY(), arg1.getY());
            }
        });
        
        groupedPoints = new ArrayList<List<Point2D>>();
        groupedPoints.add(new ArrayList<Point2D>(Arrays.asList(new Point2D[] { points.get(0) })));
        
        for (Point2D p: points.subList(1, points.size() - 1)) {
            List<Point2D> last = groupedPoints.get(groupedPoints.size() - 1);
            if (Math.abs(p.getY() - last.get(0).getY()) < this.minCharHeight) {
                groupedPoints.get(groupedPoints.size() - 1).add(p);
            }
            else {
                groupedPoints.add(new ArrayList<Point2D>(Arrays.asList(new Point2D[] { p })));
            }
        }
        
        for(List<Point2D> group: groupedPoints) {
            float avgLoc = 0;
            for(Point2D p: group) {
                avgLoc += p.getY();
            }
            avgLoc /= group.size();
            for(Point2D p: group) {
                p.setLocation(p.getX(), avgLoc);
            }
        }
        // ---
        
        // finally, modify lines
        for(Map.Entry<Ruling, Point2D[]> ltp: linesToPoints.entrySet()) {
            Point2D[] p = ltp.getValue();
            ltp.getKey().setLine(p[0], p[1]);
        }
    }
    
    
}
