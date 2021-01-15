package technology.tabula;

public class PageDims {
    private final float top;
    private final float left;
    private final float width;
    private final float height;

    private PageDims(final float top, final float left, final float width, final float height) {
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
    }

    public static PageDims of(final float top, final float left, final float width, final float height) {
        return new PageDims(top, left, width, height);
    }

    public float getTop() {
        return top;
    }

    public float getLeft() {
        return left;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
