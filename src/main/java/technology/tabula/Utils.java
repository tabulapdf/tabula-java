package technology.tabula;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.ParseException;

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
    
    private final static float EPSILON = 0.01f;
    protected static boolean useQuickSort = useCustomQuickSort(); 
    
    public static boolean feq(double f1, double f2) {
        return (Math.abs(f1 - f2) < EPSILON);
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

    /**
     * Wrap Collections.sort so we can fallback to a non-stable quicksort
     * if we're running on JDK7+ 
     * @param list
     */
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        if (useQuickSort) {
            QuickSort.sort(list);
        }
        else {
            Collections.sort(list);
        }
    }
    
    private static boolean useCustomQuickSort() {
        // taken from PDFBOX:
        
        // check if we need to use the custom quicksort algorithm as a
        // workaround to the transitivity issue of TextPositionComparator:
        // https://issues.apache.org/jira/browse/PDFBOX-1512
        String[] versionComponents = System.getProperty("java.version").split(
                "\\.");
        int javaMajorVersion = Integer.parseInt(versionComponents[0]);
        int javaMinorVersion = Integer.parseInt(versionComponents[1]);
        boolean is16orLess = javaMajorVersion == 1 && javaMinorVersion <= 6;
        String useLegacySort = System.getProperty("java.util.Arrays.useLegacyMergeSort");
        return !is16orLess || (useLegacySort != null && useLegacySort.equals("true"));
    }
    
    
    
    public static List<Integer> parsePagesOption(String pagesSpec) throws ParseException {
        if (pagesSpec.equals("all")) {
            return null;
        }
        
        List<Integer> rv = new ArrayList<Integer>();
        
        String[] ranges = pagesSpec.split(",");
        for (int i = 0; i < ranges.length; i++) {
            String[] r = ranges[i].split("-");
            if (r.length == 0 || !Utils.isNumeric(r[0]) || r.length > 1 && !Utils.isNumeric(r[1])) {
                throw new ParseException("Syntax error in page range specification");
            }
            
            if (r.length < 2) {
                rv.add(Integer.parseInt(r[0]));
            }
            else {
                int t = Integer.parseInt(r[0]);
                int f = Integer.parseInt(r[1]); 
                if (t > f) {
                    throw new ParseException("Syntax error in page range specification");
                }
                rv.addAll(Utils.range(t, f+1));       
            }
        }
        
        Collections.sort(rv);
        return rv;
    }
}
