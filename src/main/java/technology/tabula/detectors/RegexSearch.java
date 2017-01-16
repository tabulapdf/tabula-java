package technology.tabula.detectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.TextElement;

public class RegexSearch {

	private List<TextElement> textElements;
	// these should go somewhere else
	// private globals?
	// reset methods?
	char currChar;

	Boolean upperExists;
	int upperCount;
	int lowerCount;

	TextElement currElement;
	TextElement firstElement;
	TextElement lastElement;

	int totalMatches = 0;

	int state = 0;

	public List<Rectangle> detect(Page page, String upperBound, String lowerBound) throws ParseException {
		List<Rectangle> regexList = new ArrayList<Rectangle>();
		currChar = 0;
		upperExists = false;
		upperCount = 0;
		lowerCount = 0;
		currElement = null;
		firstElement = null;
		lastElement = null;
		state = 0;

		// have page, can check text elements
		textElements = page.getText();
		int size = textElements.size();

		// should scan through text elements so that we can use coords
		for (int i = 0; i < size; i++) {
			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);
			if (!upperBound.isEmpty() && upperExists == false) {
				switch (state) {
				case 0:
					if (currChar == upperBound.charAt(0)) { // valid first upper
						firstElement = currElement;
						if (upperBound.length() == 1) { // single character case
							state = 0;
							upperCount = 0;
							if (lowerBound.isEmpty()) {
								// single character upper, no lower
								regexList.add(
										new Rectangle(firstElement.y, 0, page.width, page.height - firstElement.y));
							} else {
								// single character upper, yes lower
								upperExists = true;
							}
						} else { // multiple character strings continue scanning
							upperCount = 1;
							state = 1;
						}
					}
					break;
				case 1:
					if (currChar == upperBound.charAt(upperCount)) {
						upperCount++;
					} else { // invalid char detected
						upperCount = 0;
						state = 0;
					}
					if (upperCount == upperBound.length()) {
						state = 0;
						upperCount = 0;
						if (lowerBound.isEmpty()) {
							// Multiple character upper, no lower
							regexList.add(new Rectangle(firstElement.y, 0, page.width, page.height - firstElement.y));
						} else {
							// Multiple character upper, yes lower
							upperExists = true;
						}
					}
					break;
				}
			} else {
				switch (state) {
				case 0:
					if (currChar == lowerBound.charAt(0)) { // valid first lower
						if (lowerBound.length() == 1) {
							lastElement = currElement;
							state = 0;
							if (upperExists) { // upper and lower single char
								regexList.add(
										new Rectangle(firstElement.y, 0, page.width, lastElement.y - firstElement.y));
							} else {
								regexList.add(new Rectangle(0, 0, page.width, lastElement.y));
							}
							upperExists = false;
							continue;
						} else {// multiple character strings continue scanning
							lowerCount = 1;
							state = 1;
						}
					}
					break;
				case 1:
					// next valid char detected
					if (currChar == lowerBound.charAt(lowerCount)) {
						lowerCount++;
					} else { // invalid char detected
						lowerCount = 0;
						state = 0;
					}
					if (lowerCount == lowerBound.length()) {
						lastElement = currElement;
						state = 0;
						lowerCount = 0;
						if (upperExists) {
							regexList.add(new Rectangle(firstElement.y, 0, page.width, lastElement.y - firstElement.y));
						} else {
							regexList.add(new Rectangle(0, 0, page.width, lastElement.y));
						}
						upperExists = false;
					}
					break;
				}
			}
		}
		for (Rectangle walk : regexList) {
			System.out.println("x=" + walk.x + "\ny=" + walk.y + "\nwidth=" + walk.width + "\nheight=" + walk.height);
		}
		return regexList;
	}
}
