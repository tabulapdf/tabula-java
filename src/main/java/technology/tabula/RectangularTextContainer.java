package technology.tabula;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RectangularTextContainer<T extends Rectangle & HasText> extends Rectangle implements HasText {

	protected List<T> textElements = new ArrayList<>();

	protected RectangularTextContainer(float top, float left, float width, float height) {
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

	public List<T> getTextElements() {
		return textElements;
	}

	public void setTextElements(List<T> textElements) {
		this.textElements = textElements;
	}

	@Override
	public String getText() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getText(boolean useLineReturns) {
		throw new UnsupportedOperationException();
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		String s = super.toString();
		sb.append(s.substring(0, s.length() - 1));
		sb.append(String.format(",text=%s]", this.getText() == null ? "null" : "\"" + this.getText() + "\""));
		return sb.toString();
	}

}
