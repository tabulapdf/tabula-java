package technology.tabula.detectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.TextElement;

public class RegexSearch {

	// test without expanding to page?
	public List<Rectangle> detect(Page page, String upperBound, String lowerBound) throws ParseException {
		List<Rectangle> regexList = new ArrayList<Rectangle>();
		char currChar = 0;
		Boolean upperExists = false;
		int upperCount = 0;
		int lowerCount = 0;
		TextElement currElement = null;
		TextElement firstElement = null;
		TextElement lastElement = null;
		int state = 0;

		// check for empty upper and lower here instead of later

		// have page, can check text elements
		List<TextElement> textElements = page.getText();
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
								regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
										page.height - firstElement.y));
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
							regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
									page.height - firstElement.y));
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
								regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
										(lastElement.y - firstElement.y) + lastElement.height));
							} else {
								regexList.add(new Rectangle(0, 0, page.width, lastElement.y + lastElement.height));
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
							regexList.add(new Rectangle(firstElement.y - firstElement.height, 0, page.width,
									(lastElement.y - firstElement.y) + lastElement.height));
						} else {
							regexList.add(new Rectangle(0, 0, page.width, lastElement.y + lastElement.height));
						}
						upperExists = false;
					}
					break;
				}
			}
		}
		for (Rectangle walk : regexList) {
			System.out.println(
					"Box Found at x=" + walk.x + " y=" + walk.y + " width=" + walk.width + " height=" + walk.height);
		}
		return regexList;
	}

	// possible issues:
	// multiple strings share the same name
	// right string to the left of left string?
	// need first char of left strings, last char of right strings <-- if left
	// and right reversed???
	// recover loop value if string detect fails
	// two strings on same line with same first char may fail to correctly reset
	// todo:
	// implement empty field handling
	//
	// check for both strings at once in first state, then set flag
	public List<Rectangle> detect(Page page, String upperLeft, String upperRight, String lowerLeft, String lowerRight)
			throws ParseException {

		// if both top strings are null, return null
		if (upperLeft == null && upperRight == null) {
			return new ArrayList<Rectangle>(); // empty list
		}

		else if (lowerLeft == null && lowerRight == null) {
			return new ArrayList<Rectangle>(); // empty list
		}

		List<Rectangle> regexList = new ArrayList<Rectangle>();
		char currChar = 0;

		// could check elements for null instead
		Boolean upperLeftExists = false;
		Boolean upperRightExists = false;
		Boolean lowerLeftExists = false;
		Boolean lowerRightExists = false;

		TextElement upperLeftElement = null;
		TextElement upperRightElement = null;
		TextElement lowerLeftElement = null;
		TextElement lowerRightElement = null;

		int backup = 0;

		int upperLeftCount = 0;
		int upperRightCount = 0;
		int lowerLeftCount = 0;
		int lowerRightCount = 0;

		TextElement currElement = null;
		TextElement firstElement = null;
		// TextElement lastElement = null;

		int upperState = 0;
		int lowerState = 0;

		// have page, can check text elements
		List<TextElement> textElements = page.getText();
		int size = textElements.size();

		// should scan through text elements so that we can use coords
		for (int i = 0; i < size; i++) {

			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);

			// search for top elements
			if (!(upperLeftExists && upperRightExists)) {
				switch (upperState) {
				case 0: // search for first char of either top string
				{

					if (currChar == upperLeft.charAt(0) && !(upperLeftExists)) // valid
																				// first
																				// upper
																				// left
					{
						if (upperLeft.length() == 1) // single character case
						{
							upperLeftElement = currElement;
							upperLeftExists = true;
						}

						else {
							firstElement = currElement;
							backup = i;
							upperLeftCount = 1;
							upperState = 1;
						}
					}

					else if (currChar == upperRight.charAt(0) && !(upperRightExists)) // valid
																						// first
																						// upper
																						// right
					{
						if (upperRight.length() == 1) // single character case
						{
							upperRightElement = currElement;
							upperRightExists = true;
						} else {
							firstElement = currElement;
							backup = i;
							upperRightCount = 1;
							upperState = 2;
						}
					}

					break;
				}

				case 1: // evaluate chars for top left string
				{
					if (currChar == upperLeft.charAt(upperLeftCount)) {
						upperLeftCount++;
					} else { // invalid char detected
						upperLeftCount = 0;

						if (upperLeft.charAt(0) == upperRight.charAt(0)) {
							upperRightCount = 1;
							upperState = 2;
							i = backup; // go back to first character
						} else {
							upperState = 0;
							i = backup; // go to second character
						}
					}

					if (upperLeftCount == upperLeft.length()) {
						upperLeftElement = firstElement;
						upperLeftExists = true;
						firstElement = null;

						if (upperRightExists)
							upperState = 3;
						else
							upperState = 0;
					}

					break;
				}

				case 2: // evaluate chars for top right string
				{
					if (currChar == upperRight.charAt(upperRightCount)) {
						upperRightCount++;
					} else { // invalid char detected
						upperRightCount = 0;
						upperState = 0;
						i = backup;
					}

					if (upperRightCount == upperRight.length()) {
						upperRightElement = currElement;
						upperRightExists = true;
						firstElement = null;

						if (upperLeftExists)
							upperState = 3; // replace
						else
							upperState = 0;
					}

					break;
				}

				case 3: // found both upper strings, probably not needed because
						// of if statement
				{
					// zzz
					break;
				}
				}
			}

			else {
				switch (lowerState) {
				case 0: // search for first char of either top string
				{
					if (currChar == lowerLeft.charAt(0) && !(lowerLeftExists)) // valid
																				// first
																				// lower
																				// left
					{
						if (lowerLeft.length() == 1) // single character case
						{
							lowerLeftElement = currElement;
							lowerLeftExists = true;
						}

						else {
							firstElement = currElement;
							backup = i;
							lowerLeftCount = 1;
							lowerState = 1;
						}
					}

					else if (currChar == lowerRight.charAt(0) && !(lowerRightExists)) // valid
																						// first
																						// lower
																						// right
					{
						if (lowerRight.length() == 1) // single character case
						{
							lowerRightElement = currElement;
							lowerRightExists = true;
						} else {
							firstElement = currElement;
							backup = i;
							lowerRightCount = 1;
							lowerState = 2;
						}
					}

					break;
				}

				case 1: // evaluate chars for top left string
				{
					if (currChar == lowerLeft.charAt(lowerLeftCount)) {
						lowerLeftCount++;
					} else { // invalid char detected
						lowerLeftCount = 0;

						if (lowerLeft.charAt(0) == lowerRight.charAt(0)) {
							lowerRightCount = 1;
							lowerState = 2;
							i = backup; // go back to first character
						} else {
							lowerState = 0;
							i = backup; // go to second character
						}
					}

					if (lowerLeftCount == lowerLeft.length()) {
						lowerLeftElement = firstElement;
						lowerLeftExists = true;
						firstElement = null;

						if (lowerRightExists)
							lowerState = 3;
						else
							lowerState = 0;
					}

					break;
				}

				case 2: // evaluate chars for top right string
				{
					if (currChar == lowerRight.charAt(lowerRightCount)) {
						lowerRightCount++;
					} else { // invalid char detected
						lowerRightCount = 0;
						lowerState = 0;
						i = backup;
					}

					if (lowerRightCount == lowerRight.length()) {
						lowerRightElement = currElement;
						lowerRightExists = true;
						firstElement = null;

						if (lowerLeftExists)
							lowerState = 3;
						else
							lowerState = 0;
					}

					break;
				}

				case 3: {
					// zzz
					break;
				}
				}
			}
		} // end of for loop

		if (!(upperLeftExists && upperRightExists && lowerLeftExists && lowerRightExists)) // missing
																							// item,
																							// placeholder
		{
			return new ArrayList<Rectangle>(); // empty arraylist
		}

		// may want to check if parameters match left/right schema first
		float leftBound = Math.min(upperLeftElement.x - upperLeftElement.width,
				lowerLeftElement.x - lowerLeftElement.width);
		float topBound = Math.min(upperLeftElement.y - upperLeftElement.height,
				upperRightElement.y - upperRightElement.height);
		float width = Math.max((upperRightElement.x + upperRightElement.width - leftBound),
				(lowerRightElement.x + lowerRightElement.width - leftBound));
		float height = Math.max((lowerLeftElement.y + lowerLeftElement.height - topBound),
				(lowerRightElement.y + lowerRightElement.height - topBound));

		Rectangle foundRectangle = new Rectangle(topBound, leftBound, width, height);
		regexList.add(foundRectangle);

		for (Rectangle walk : regexList) {
			System.out.println(
					"Box found at x=" + walk.x + " y=" + walk.y + " width=" + walk.width + " height=" + walk.height);
		}
		return regexList;
	}
}
