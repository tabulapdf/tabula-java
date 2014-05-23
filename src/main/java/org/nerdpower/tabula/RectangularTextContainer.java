package org.nerdpower.tabula;

public abstract class RectangularTextContainer extends Rectangle {

    public RectangularTextContainer(float top, float left, float width, float height) {
        super(top, left, width, height);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String s = super.toString();
        sb.append(s.substring(0, s.length() - 1));
        sb.append(String.format(",text=\"%s\"]", this.getText()));
        return sb.toString();
    }
    
    public abstract String getText();
}
