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
	int size;
	char currChar;
	String textString;

	Boolean upperExists;
	Boolean lowerExists;
	int upperCount;
	int lowerCount;

	TextElement currElement;
	TextElement firstElement;
	TextElement lastElement;

	int totalMatches = 0;

	int state = 0;

	public List<Rectangle> detect(Page page, String upperBound, String lowerBound) throws ParseException {
		List<Rectangle> regexList = new ArrayList<Rectangle>();
		try {
			size = 0;
			textString = "";

			currChar = 0;
			upperExists = false;
			lowerExists = false;
			upperCount = 0;
			lowerCount = 0;
			currElement = null;
			firstElement = null;
			lastElement = null;
			state = 0;

			// have page, can check text elements

			textElements = page.getText();
			size = textElements.size();

			// this is only good for quick testing
			// should scan through text elements so that we can use coords
			for (int i = 0; i < size; i++) {
				currElement = textElements.get(i);
				currChar = currElement.getText().charAt(0);
				// textString = textString.concat(currChar);

				switch (state) {
				// no matches, look for first char of upper string
				case 0: {
					// valid first char detected
					if (currChar == upperBound.charAt(0)) {
						firstElement = currElement;

						// guards against single character case
						if (upperBound.length() == 1) {
							upperExists = true;
							state = 2;
						}
						// multiple character strings continue scanning
						else {
							upperCount = 1;
							state = 1;
						}
					}
					break;
				}
				// first char of upper string detected, look for the rest of
				// the upper string chars
				case 1: {
					// next valid char detected
					if (currChar == upperBound.charAt(upperCount)) {
						upperCount++;
					}
					// invalid char detected
					else {
						upperCount = 0;
						state = 0;
					}

					if (upperCount == upperBound.length()) {
						upperExists = true;
						state = 2;
					}

					break;
				}
				// upper string matched, look for first char of lower string
				case 2: {
					// valid first char detected
					if (currChar == lowerBound.charAt(0)) {
						// guards against single character case
						if (lowerBound.length() == 1) {
							lastElement = currElement;
							lowerExists = true;

							state = 4;
							continue;
						}
						// multiple character strings continue scanning
						else {
							lowerCount = 1;
							state = 3;
						}
					}

					break;
				}
				// first char of lower string detected, look for rest of
				// lower string chars
				case 3: {
					// next valid char detected
					if (currChar == lowerBound.charAt(lowerCount)) {
						lowerCount++;
					}
					// invalid char detected
					else {
						lowerCount = 0;
						state = 2;
					}
					if (lowerCount == lowerBound.length()) {
						lastElement = currElement;
						lowerExists = true;

						state = 4;

						continue;
					}
					break;
				}
				// lower string matched, output coordinates and exit?
				case 4: {
					regexList.add(new Rectangle(firstElement.x, firstElement.y, lastElement.x - firstElement.x,
							lastElement.y - firstElement.y));
					System.out.println("Upper Coordinates: \n  x = " + firstElement.x + "\n  y = " + firstElement.y);
					System.out
							.println("Lower Coordinates: \n  x = " + lastElement.x + "\n  y = " + lastElement.y + "\n");

					// may want to keep scanning the page? reset locals here
					// and go to state 0?
					// store coordinates somewhere?

					// dumb way to reset locals
					currChar = 0;
					upperExists = false;
					lowerExists = false;
					upperCount = 0;
					lowerCount = 0;
					currElement = null;
					firstElement = null;
					lastElement = null;
					state = 0;

					totalMatches++;

					// i = size; // jump out of this page, locals
					// automatically get reset
					// state = 4;
				}
				// you shouldn't be here
				default: {
					state = 0;
					break;
				}
				}
			}
			System.out.println("Total Matches = " + totalMatches);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Error: " + e.getMessage());
		}
		return regexList;
	}
}
