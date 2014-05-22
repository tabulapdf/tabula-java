package org.nerdpower.tabula;

public abstract class RectangularTextContainer extends Rectangle {

    public RectangularTextContainer(float top, float left, float width, float height) {
        super(top, left, width, height);
    }
    
    public abstract String getText();
}
