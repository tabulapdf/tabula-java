package org.nerdpower.tabula;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author manuel
 */
public class Utils {
    public static boolean within(double first, double second, double variance) {
        return second < first + variance && second > first - variance;
    }
    
    public static boolean overlap(double y1, double height1, double y2, double height2, double variance) {
        return within( y1, y2, variance) || (y2 <= y1 && y2 >= y1 - height1) || (y1 <= y2 && y1 >= y2-height2);
    }
    
    public static boolean overlap(double y1, double height1, double y2, double height2) {
        return overlap(y1, height1, y2, height2, 0.1f);
    }
    
    public static float round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    
    public static Rectangle bounds(Collection<? extends Shape> shapes) {
        if (shapes.isEmpty()) {
            throw new IllegalArgumentException("shapes can't be empty");
        }
        
        Iterator<? extends Shape> iter = shapes.iterator();
        Rectangle rv = new Rectangle();
        rv.setRect(iter.next().getBounds2D());

        do {
            Rectangle2D.union(iter.next().getBounds2D(), rv, rv);
        } while (iter.hasNext());
        
        return rv;
        
    }
    
    // range iterator
    public static List<Integer> range(final int begin, final int end) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int index) {
                return begin + index;
            }

            @Override
            public int size() {
                return end - begin;
            }
        };
    }
    

    /* from apache.commons-lang */
    public static boolean isNumeric(final CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static String join(String glue, String...s) {
        int k = s.length;
        if ( k == 0 )
        {
          return null;
        }
        StringBuilder out = new StringBuilder();
        out.append( s[0] );
        for ( int x=1; x < k; ++x )
        {
          out.append(glue).append(s[x]);
        }
        return out.toString();
    }
    
    public static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

}
