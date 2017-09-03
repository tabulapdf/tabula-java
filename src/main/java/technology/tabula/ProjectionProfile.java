package technology.tabula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// NOTE: this class is currently not used by the extraction algorithms
// keeping it for potential use.
public class ProjectionProfile {

    public static final int DECIMAL_PLACES = 1; // fixed <-> float conversion precision
    private final Page area;
    private final Rectangle textBounds;
    private float[] verticalProjection;
    private float[] horizontalProjection;
    private final double areaWidth, areaHeight, areaTop, areaLeft;
    private float minCharWidth = Float.MAX_VALUE, minCharHeight = Float.MAX_VALUE, horizontalKernelSize, verticalKernelSize;
    private float maxHorizontalProjection = 0, maxVerticalProjection = 0;
    
    public ProjectionProfile(Page area, List<? extends Rectangle> elements, float horizontalKernelSize, float verticalKernelSize) {
        this.area = area;
        this.areaWidth = area.getWidth();
        this.areaHeight = area.getHeight();
        this.areaTop = area.getTop();
        this.areaLeft = area.getLeft();
        this.verticalProjection = new float[toFixed(areaHeight)];
        this.horizontalProjection = new float[toFixed(areaWidth)];
        this.horizontalKernelSize = horizontalKernelSize;
        this.verticalKernelSize = verticalKernelSize;
        this.textBounds = area.getTextBounds();
        
        for (Rectangle element: elements) {
            // exclude elements that take more than 80% of the width
            // of the area. They won't contribute to determining columns
            if (element.getWidth() / this.textBounds.getWidth() > 0.8) {
                continue;
            }
            this.addRectangle(element);
        }
        
        this.verticalProjection = smooth(this.verticalProjection, toFixed(verticalKernelSize));
        this.horizontalProjection = smooth(this.horizontalProjection, toFixed(horizontalKernelSize));
    }
    
    private void addRectangle(Rectangle element) {
        // calculate horizontal and vertical projection profiles
        if (!area.contains(element)) {
            return;
        }
        
        this.minCharHeight = (float) Math.min(this.minCharHeight, element.getHeight());
        this.minCharWidth = (float) Math.min(this.minCharWidth, element.getWidth());
        
        for (int k = toFixed(element.getLeft()); k < toFixed(element.getRight()); k++) {
            this.horizontalProjection[k - toFixed(areaLeft)] += element.getHeight();
            this.maxHorizontalProjection = Math.max(this.maxHorizontalProjection, this.horizontalProjection[k - toFixed(areaLeft)]);
        }
        for(int k = toFixed(element.getTop()); k < toFixed(element.getBottom()); k++) {
            this.verticalProjection[k - toFixed(areaTop)] += element.getWidth();
            this.maxVerticalProjection = Math.max(this.maxVerticalProjection, this.verticalProjection[k - toFixed(areaTop)]);
        }
    }
    
    public float[] getVerticalProjection() {
        return verticalProjection;
    }

    public float[] getHorizontalProjection() {
        return horizontalProjection;
    }
    
    public float[] findVerticalSeparators(float minColumnWidth) {
        boolean foundNarrower = false;

        List<Integer> verticalSeparators = new ArrayList<>();
        for (Ruling r: area.getVerticalRulings()) {
            if (r.length() / this.textBounds.getHeight() >= 0.95) {
                verticalSeparators.add(toFixed(r.getPosition() - this.areaLeft));
            }
        }
        
        List<Integer> seps = findSeparatorsFromProjection(filter(getFirstDeriv(this.horizontalProjection), 0.1f));
        
        for (Integer foundSep: seps) {
            for (Integer explicitSep: verticalSeparators) {
                if (Math.abs(toDouble(foundSep - explicitSep)) <= minColumnWidth) {
                    foundNarrower = true;
                    break;
                } 
            }
            if (!foundNarrower) {
                verticalSeparators.add(foundSep);
            }
            foundNarrower = false;
        }
        Collections.sort(verticalSeparators);
        float[] rv = new float[verticalSeparators.size()];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = (float) toDouble(verticalSeparators.get(i));
        }
        return rv;
    }
    
    public float[] findHorizontalSeparators(float minRowHeight) {
        boolean foundShorter = false;

        List<Integer> horizontalSeparators = new ArrayList<>();
        for (Ruling r: area.getHorizontalRulings()) {
            System.out.println(r.length() / this.textBounds.getWidth());
            if (r.length() / this.textBounds.getWidth() >= 0.95) {
                horizontalSeparators.add(toFixed(r.getPosition() - this.areaTop));
            }
        }
        
        List<Integer> seps = findSeparatorsFromProjection(filter(getFirstDeriv(this.verticalProjection), 0.1f));
        
        for (Integer foundSep: seps) {
            for (Integer explicitSep: horizontalSeparators) {
                if (Math.abs(toDouble(foundSep - explicitSep)) <= minRowHeight) {
                    foundShorter = true;
                    break;
                } 
            }
            if (!foundShorter) {
                horizontalSeparators.add(foundSep);
            }
            foundShorter = false;
        }
        Collections.sort(horizontalSeparators);
        float[] rv = new float[horizontalSeparators.size()];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = (float) toDouble(horizontalSeparators.get(i));
        }
        return rv;
    }
    
    private static List<Integer> findSeparatorsFromProjection(float[] derivative) {
        List<Integer> separators = new ArrayList<>();
        Integer lastNeg = null;
        float s;
        boolean positiveSlope = false;
        
        // find separators based on histogram
        for (int i = 0; i < derivative.length; i++) {
            s = derivative[i];
            if (s > 0 && !positiveSlope) {
                positiveSlope = true;
                separators.add(lastNeg != null ? lastNeg : i);
            }
            else if (s < 0) {
                lastNeg = i;
                positiveSlope = false;
            }
        }
        return separators;
    }
    
    public static float[] smooth(float[] data, int kernelSize) {
        float[] rv = new float[data.length];
        float s;
        
        for (int pass = 0; pass < 1; pass++) {
            for (int i = 0; i < data.length; i++) {
                s = 0;
                for (int j = Math.max(0, i - kernelSize / 2); j < Math.min(i
                        + kernelSize / 2, data.length); j++) {
                    s += data[j];
                }
                rv[i] = (float) Math.floor(s / kernelSize);
            }
        }
        return rv;
    }
    
    
    /** 
     * Simple Low pass filter
     */
    public static float[] filter(float[] data, float alpha) {

        float[] rv = new float[data.length];
        rv[0] = data[0];
        for (int i = 1; i < data.length; i++) {
            rv[i] = rv[i-1] + alpha * (data[i] - rv[i-1]);
        }

        return rv;
    }
    
    public static float[] getAutocorrelation(float[] projection) {
        float[] rv = new float[projection.length-1];
        for (int i = 1; i < projection.length - 1; i++) {
            rv[i] = (projection[i] * projection[i-1]) / 100f;
        }
        return rv;
        
    }
    
    public static float[] getFirstDeriv(float[] projection) {
        float[] rv = new float[projection.length];
        rv[0] = projection[1] - projection[0];
        for (int i = 1; i < projection.length - 1; i++) {
            rv[i] = projection[i+1] - projection[i-1];
        }
        rv[projection.length - 1] = projection[projection.length - 1] - projection[projection.length - 2];
        return rv;
    }

    // pretty lame fixed precision math here
    private static int toFixed(double value) {
        return (int) Math.round(value * (Math.pow(10, DECIMAL_PLACES)));
    }
    
    private static double toDouble(int value) {
        return value / Math.pow(10, DECIMAL_PLACES);
    }
    
}
