package technology.tabula;

import gnu.trove.procedure.TIntProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;

class RectangleSpatialIndex<T extends Rectangle> {
    
    class SaveToListProcedure implements TIntProcedure {
        private List<Integer> ids = new ArrayList<Integer>();

        public boolean execute(int id) {
          ids.add(id);
          return true;
        };
        
        private List<Integer> getIds() {
          return ids;
        }
    };
	
    private final SpatialIndex si;
    private final List<T> rectangles;
    private Rectangle bounds = null;
    
    public RectangleSpatialIndex() {
        si = new RTree();
        si.init(null);
        rectangles = new ArrayList<T>();
    }
    
    public void add(T te) {
        rectangles.add(te);
        if (bounds == null) {
            bounds = new Rectangle();
            bounds.setRect(te);
        }
        else {
            bounds.merge(te);            
        }
        si.add(rectangleToSpatialIndexRectangle(te), rectangles.size() - 1);
    }
    
    public List<T> contains(Rectangle r) {
        SaveToListProcedure proc = new SaveToListProcedure();
        si.contains(rectangleToSpatialIndexRectangle(r), proc);
        ArrayList<T> rv = new ArrayList<T>();
        for (int i : proc.getIds()) {
            rv.add(rectangles.get(i));
        }
        Utils.sort(rv);
        return rv;
    }
    
    public List<T> intersects(Rectangle r) {
        SaveToListProcedure proc = new SaveToListProcedure();
        si.intersects(rectangleToSpatialIndexRectangle(r), proc);
        ArrayList<T> rv = new ArrayList<T>();
        for (int i : proc.getIds()) {
            rv.add(rectangles.get(i));
        }
        Utils.sort(rv);
        return rv;
    }
    
    private net.sf.jsi.Rectangle rectangleToSpatialIndexRectangle(Rectangle r) {
        return new net.sf.jsi.Rectangle((float) r.getX(),
                (float) r.getY(),
                (float) (r.getX() + r.getWidth()),
                (float) (r.getY() + r.getHeight()));
    }

    
    /**
     * Minimum bounding box of all the Rectangles contained on this RectangleSpatialIndex
     * 
     * @return a Rectangle
     */
    public Rectangle getBounds() {
        return bounds;
    }

}
