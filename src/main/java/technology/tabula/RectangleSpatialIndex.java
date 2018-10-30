package technology.tabula;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;

public class RectangleSpatialIndex<T extends Rectangle> {
    

    private final STRtree si = new STRtree();
    private final List<T> rectangles = new ArrayList<>();

    public void add(T te) {
        rectangles.add(te);
        si.insert(new Envelope(te.getLeft(), te.getRight(), te.getBottom(), te.getTop()), te);
    }
    
    public List<T> contains(Rectangle r) {
        List<T> intersection = si.query(new Envelope(r.getLeft(), r.getRight(), r.getTop(), r.getBottom()));
        List<T> rv = new ArrayList<T>();

        for (T ir: intersection) {
            if (r.contains(ir)) {
                rv.add(ir);
            }
        }

        Utils.sort(rv, Rectangle.ILL_DEFINED_ORDER);
        return rv;
    }
    
    public List<T> intersects(Rectangle r) {
        List rv = si.query(new Envelope(r.getLeft(), r.getRight(), r.getTop(), r.getBottom()));
        return rv;
    }
    
    /**
     * Minimum bounding box of all the Rectangles contained on this RectangleSpatialIndex
     * 
     * @return a Rectangle
     */
    public Rectangle getBounds() {
        return Rectangle.boundingBoxOf(rectangles);
    }

}
