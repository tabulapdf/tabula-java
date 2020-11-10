package technology.tabula;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class Cell extends RectangularTextContainer<TextChunk> {

	private boolean spanning;
	private boolean placeholder;
	private List<TextChunk> textElements;

	public Cell(float top, float left, float width, float height) {
		super(top, left, width, height);
		spanning = false;
		placeholder = false;
		textElements = new ArrayList<>();
	}

	public Cell(Point2D topLeft, Point2D bottomRight) {
		this((float) topLeft.getY(), (float) topLeft.getX(),
			  (float) (bottomRight.getX() - topLeft.getX()),
			  (float) (bottomRight.getY() - topLeft.getY()));
	}

	public String getText() {
		if (textElements.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Collections.sort(textElements, Rectangle.ILL_DEFINED_ORDER);
		double currentTop = textElements.get(0).getTop();
		for (TextChunk textChunk : textElements) {
			if (textChunk.getTop() > currentTop) {
				sb.append('\r');
			}
			sb.append(textChunk.getText());
			currentTop = textChunk.getTop();
		}
		return sb.toString().trim();
	}

	public boolean isSpanning() {
		return spanning;
	}

	public void setSpanning(boolean spanning) {
		this.spanning = spanning;
	}

	public boolean isPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(boolean placeholder) {
		this.placeholder = placeholder;
	}

	public List<TextChunk> getTextElements() {
		return textElements;
	}

	public void setTextElements(List<TextChunk> textElements) {
		this.textElements = textElements;
	}

}
