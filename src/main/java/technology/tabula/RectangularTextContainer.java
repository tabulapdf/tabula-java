package technology.tabula;

import java.util.List;

@SuppressWarnings("serial")
public abstract class RectangularTextContainer<T extends HasText> extends Rectangle {

	public RectangularTextContainer(float top, float left, float width, float height) {
		super(top, left, width, height);
	}

	public RectangularTextContainer<T> merge(RectangularTextContainer<T> other) {
		if (compareTo(other) < 0) {
			this.getTextElements().addAll(other.getTextElements());
		} else {
			this.getTextElements().addAll(0, other.getTextElements());
		}
		super.merge(other);
		return this;
	}

	public abstract String getText();

	public abstract String getText(boolean useLineReturns);

	public abstract List<T> getTextElements();

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		String s = super.toString();
		sb.append(s.substring(0, s.length() - 1));
		sb.append(String.format(",text=%s]", this.getText() == null ? "null" : "\"" + this.getText() + "\""));
		return sb.toString();
	}

}
