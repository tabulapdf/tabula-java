package technology.tabula.detectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.TextElement;

public class StringSearch {

	public List<Rectangle> detect(Page page, String[] stringList) throws ParseException {
		// check if page object is null
		if(page == null) return new ArrayList<Rectangle>(); // return empty arraylist
		
		// only 3 and 4 string arrays accepted
		if(stringList.length < 3 || stringList.length > 4) return new ArrayList<Rectangle>(); // return empty arraylist;
		
		// this is so dumb
		String upperLeft = null;
		String upperRight = null;
		String lowerLeft = null;
		String lowerRight = null;
		
		// parse out the input string array
		for(int i = 0; i < stringList.length; i++) {
			switch(i) {
				case 0:
					upperLeft = stringList[i];
					break;
				case 1:
					upperRight = stringList[i];
					break;
				case 2:
					lowerLeft = stringList[i];
					break;
				case 3:
					lowerRight = stringList[i];
					break;
				default:
					break;
			}
		}		
		
		// replace null strings with empty fields
		if(upperLeft == null) upperLeft = "";
		if(upperRight == null) upperRight = "";
		if(lowerLeft == null) lowerLeft = "";
		if(lowerRight == null) lowerRight = "";
		
		// count number of empty fields
		int emptyCount = 0;
		if(upperLeft.equals("")) emptyCount++;
		if(upperRight.equals("")) emptyCount++;
		if(lowerLeft.equals("")) emptyCount++;
		if(lowerRight.equals("")) emptyCount++;
		
		// if 0 or 1 empty fields, perform 4 corner search
		if(emptyCount <= 1) {
			return detectFour(page, upperLeft, upperRight, lowerLeft, lowerRight);
		}
		
		// if 2 fields are null, determine which then perform 2 corner search
		else if(emptyCount == 2 
				&& (upperLeft.equals("") ^ upperRight.equals("")) 
				&& (lowerLeft.equals("") ^ lowerRight.equals(""))) {
			String upperBound;
			String lowerBound;
			if((upperRight.equals("")) && !(upperLeft.equals(""))) upperBound = upperLeft;
			else upperBound = upperRight;
			if((lowerRight.equals("")) && !(lowerLeft.equals(""))) lowerBound = lowerLeft;
			else lowerBound = lowerRight;
			return detectTwo(page, upperBound, lowerBound);
		}
		
		// if 3 fields are null, determine which then perform single string search
		else if(emptyCount == 3){
			// determine which field is not empty
			String searchString;
			if(!upperLeft.equals("")) searchString = upperLeft;
			else if(!upperRight.equals("")) searchString = upperRight;
			else if(!lowerLeft.equals("")) searchString = lowerLeft;
			else if(!lowerRight.equals("")) searchString = lowerRight;
			else return new ArrayList<Rectangle>(); // return empty arraylist
			
			return detectString(page, searchString);
		}
		
		return new ArrayList<Rectangle>(); // return empty arraylist
		
	}
	
	private List<Rectangle> detectString(Page page, String searchString) {		
		List<Rectangle> stringList = new ArrayList<Rectangle>();
		char currChar = 0;
		TextElement currElement = null;
		TextElement firstElement = null;
		TextElement lastElement = null;
		int state = 0;
		
		int stringCount = 0;
		boolean stringFound = false;

		// check for empty upper and lower here instead of later
		// Dont really need to check these if they are private and only called by detect function				
		if(page == null){
			return new ArrayList<Rectangle>(); // empty list
		}
		if(searchString == null){
			return new ArrayList<Rectangle>(); // empty list
		}
		
		// have page, can check text elements
		List<TextElement> textElements = page.getText();
		int size = textElements.size();

		// should scan through text elements so that we can use coords
		for (int i = 0; i < size; i++) {
			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);
			
			if(!stringFound){
				switch(state){
				case 0:
					if(currChar == searchString.charAt(0)) { // valid first upper
						firstElement = currElement;
						
						// check for single character case
						if(searchString.length() == 1){
							stringFound = true;
							stringList.add(new Rectangle(firstElement.y,
														firstElement.x,
														firstElement.width,
														firstElement.height));
						}
						// start searching for next characters in string
						else{
							stringCount = 1;
							state = 1;
						}					
					}
					break;
					
				case 1:
					if (currChar == searchString.charAt(stringCount)) {
						stringCount++;
					} 
					else { // invalid char detected
						stringCount = 0;
						state = 0;
					}
					if (stringCount == searchString.length()) {
						lastElement = currElement;
						stringFound = true;
						
						stringCount = 0;
						state = 0;
					}
					break;				
				}
			}
			else break;
		}
		
		if(!stringFound) return new ArrayList<Rectangle>(); // empty list
		else{
			// assume that first and last elements could appear on different lines
			// this method will not draw a proper box encapsulating all elements of string
			//		if elements are on different lines
			float topBound = Math.min(firstElement.y, lastElement.y);
			float botBound = Math.max(firstElement.y + firstElement.height, lastElement.y + lastElement.height);
			float leftBound = Math.min(firstElement.x - firstElement.width, lastElement.x - lastElement.width);
			float rightBound = Math.max(firstElement.x + firstElement.width, lastElement.x + firstElement.width);
			float width = rightBound - leftBound;
			float height = botBound - topBound;
			
			Rectangle eval = new Rectangle(topBound, leftBound, width, height);			
			
			// try to find overlapping autodetect table for box surrounding string			
			// get auto detected list of rectangles
			NurminenDetectionAlgorithm nurmDetect = new NurminenDetectionAlgorithm();
			List<Rectangle> auto = nurmDetect.detect(page);

			// try spreadsheet in case auto doesnt work
			SpreadsheetDetectionAlgorithm spreadDetect = new SpreadsheetDetectionAlgorithm();
			List<Rectangle> spread = spreadDetect.detect(page);

			float bestOverlap = 0;
			Rectangle bestGuess = eval;
			Boolean replaced = false;
			
			// compare with autodetected tables
			if (!auto.isEmpty()) {
				for (int k = 0; k < auto.size(); k++) {
					Rectangle eval1 = auto.get(k);

					// look for rectangle overlap
					float overlap = eval.overlapRatio(eval1);

					if (overlap > bestOverlap) {
						bestOverlap = overlap;
						bestGuess = eval1;
						replaced = true;
					}
				}
			}

			// compare with spreadsheet tables
			if (!spread.isEmpty()) {
				for (int k = 0; k < spread.size(); k++) {
					Rectangle eval2 = spread.get(k);

					// look for rectangle overlap
					float overlap = eval.overlapRatio(eval2);

					if (overlap > bestOverlap) {
						bestOverlap = overlap;
						bestGuess = eval2;
						replaced = true;
					}
				}
			}
			
			if(!replaced) return new ArrayList<Rectangle>(); // empty list
			else stringList.add(bestGuess);
		}
		
		return stringList;
	}

	private List<Rectangle> detectTwo(Page page, String upperBound, String lowerBound) throws ParseException {
		List<Rectangle> stringList = new ArrayList<Rectangle>();
		char currChar = 0;
		Boolean upperExists = false;
		Boolean lowerExists = false;
		int upperCount = 0;
		int lowerCount = 0;
		TextElement currElement = null;
		TextElement firstElement = null;
		TextElement lastElement = null;
		int state = 0;

		// Dont really need to check these if they are private and only called by detect function				
		if(page == null){
			return new ArrayList<Rectangle>(); // empty list
		}
		// check for null or empty fields
		if(upperBound == null || lowerBound == null || upperBound.isEmpty() || lowerBound.isEmpty()){
			return new ArrayList<Rectangle>(); // empty list
		}

		// have page, can check text elements
		List<TextElement> textElements = page.getText();
		int size = textElements.size();

		// scan through list of text elements for character arrays that match input strings
		for (int i = 0; i < size; i++) {
			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);
			if (!upperExists) {
				switch (state) {
				case 0:
					if (currChar == upperBound.charAt(0)) { // valid first upper
						firstElement = currElement;
						if (upperBound.length() == 1) { // single character case
							state = 0;
							upperCount = 0;
							upperExists = true;						
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
						upperExists = true;						
					}
					break;
				}
			} else if(!lowerExists) {
				switch (state) {
				case 0:
					if (currChar == lowerBound.charAt(0)) { // valid first lower
						if (lowerBound.length() == 1) {
							lastElement = currElement;
							state = 0;
							lowerCount = 0;
							lowerExists = true;
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
						lowerExists = true;
					}
					break;
				}
			} else break;
		}
		
		if(upperExists && lowerExists){
			float topBound = Math.min(firstElement.y, lastElement.y);
			float botBound = Math.max(firstElement.y + firstElement.height, lastElement.y + lastElement.height);
			float leftBound = 0; // expand found table to page
			float rightBound = page.width; // expand found table to page
			float width = rightBound - leftBound;
			float height = botBound - topBound;
			
			Rectangle foundRectangle = new Rectangle(topBound, leftBound, width, height);
			
			stringList.add(foundRectangle);
		}
		
		return stringList;
	}

	private List<Rectangle> detectFour(Page page, String upperLeft, String upperRight, String lowerLeft, String lowerRight)
			throws ParseException {

		// Dont really need to check these if they are private and only called by detect function				
		if(page == null){
			return new ArrayList<Rectangle>(); // empty list
		}
		// if both top strings are null, return null
		if (upperLeft == null && upperRight == null) {
			return new ArrayList<Rectangle>(); // empty list
		}
		
		if (lowerLeft == null && lowerRight == null) {
			return new ArrayList<Rectangle>(); // empty list
		}

		List<Rectangle> stringList = new ArrayList<Rectangle>();
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
		int lastGoodIndex = 0;
		int resetCount = 0;
		
		int foundCount = 0; // use to count number of detected strings

		int upperLeftCount = 0;
		int upperRightCount = 0;
		int lowerLeftCount = 0;
		int lowerRightCount = 0;

		TextElement currElement = null;
		TextElement firstElement = null;

		int searchArea = 0;
		int upperState = 0;
		int lowerState = 0;

		// have page, can check text elements
		List<TextElement> textElements = page.getText();
		int size = textElements.size();

		// should scan through text elements so that we can use coords
		for (int i = 0; i < size; i++) {

			currElement = textElements.get(i);
			currChar = currElement.getText().charAt(0);

			if (upperLeftExists && upperRightExists) { // found both top, temp solution
				searchArea = 1;
			}
			
			// this part of loop body is very difficult to modify and should be revamped
			try{
				switch(searchArea) {
				case 0:
				// search for top elements
				//if (!(upperLeftExists && upperRightExists)) {
					switch (upperState) {
					case 0: // search for first char of either top string
					{
						// valid first upper left
						if (upperLeft.startsWith(Character.toString(currChar)) && !(upperLeftExists))
						{
							if (upperLeft.length() == 1) // single character case
							{
								upperLeftElement = currElement;
								upperLeftExists = true;
								lastGoodIndex = i;
								foundCount++;
							}
	
							else {
								firstElement = currElement;
								backup = i;
								upperLeftCount = 1;
								upperState = 1;
							}
						}
	
						// valid first upper right
						else if (upperRight.startsWith(Character.toString(currChar)) && !(upperRightExists))
						{
							if (upperRight.length() == 1) // single character case
							{
								upperRightElement = currElement;
								upperRightExists = true;
								lastGoodIndex = i;
								foundCount++;
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
	
							//if (upperLeft.charAt(0) == upperRight.charAt(0)) {
							if (upperRight.startsWith(Character.toString(upperLeft.charAt(0)))) {
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
							
							lastGoodIndex = i;
							foundCount++;
	
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
							
							lastGoodIndex = i;
							foundCount++;
	
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
						break;
					}
				}
					
					break;
				
				case 1: // lower strings
				//else {
					switch (lowerState) {
					case 0: // search for first char of either top string
					{
						// valid first lower left
						if (lowerLeft.startsWith(Character.toString(currChar)) && !(lowerLeftExists))
						{
							if (lowerLeft.length() == 1) // single character case
							{
								lowerLeftElement = currElement;
								lowerLeftExists = true;
								lastGoodIndex = i;
								foundCount++;
							}
	
							else {
								firstElement = currElement;
								backup = i;
								lowerLeftCount = 1;
								lowerState = 1;
							}
						}
	
						// valid first lower right
						else if (lowerRight.startsWith(Character.toString(currChar)) && !(lowerRightExists))
						{
							if (lowerRight.length() == 1) // single character case
							{
								lowerRightElement = currElement;
								lowerRightExists = true;
								lastGoodIndex = i;
								foundCount++;
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
	
							//if (lowerLeft.charAt(0) == lowerRight.charAt(0)) {
							if (lowerRight.startsWith(Character.toString(lowerLeft.charAt(0)))) {
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
							
							lastGoodIndex = i;
							foundCount++;
	
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
							
							lastGoodIndex = i;
							foundCount++;
	
							if (lowerLeftExists)
								lowerState = 3;
							else
								lowerState = 0;
						}
	
						break;
					}
	
					case 3: 
					{
						// zzz
						break;
					}
				}
					
					break;
				}
			}
			// this is so dumb
			catch(NullPointerException npe){
				//System.out.println(npe);
			}
			
			// if at end of page and only one upper string detected, need to roll back to last good index
			// then begin searching for the bottom strings
			if((i == (size - 1)) && !(upperRightExists && upperLeftExists) && (foundCount == 1) && (resetCount == 0)){
				resetCount = 1;
				i = lastGoodIndex;
				searchArea = 1;
			}
		} // end of for loop

		// need at least 3 matched strings to form box
		if (foundCount < 3)	{
			return new ArrayList<Rectangle>(); // empty arraylist
		}

		float topBound;
		float botBound;
		float leftBound;
		float rightBound;
		
		// should this not be y?
		if(!upperLeftExists && upperRightExists) topBound = upperRightElement.y - upperRightElement.height;
		else if(upperLeftExists && !upperRightExists) topBound = upperLeftElement.y - upperLeftElement.height;
		else topBound = Math.min(upperLeftElement.y - upperLeftElement.height,
				upperRightElement.y - upperRightElement.height);
		
		if(!lowerRightExists && lowerLeftExists) botBound = lowerLeftElement.y + lowerLeftElement.height;
		else if(lowerRightExists && !lowerLeftExists) botBound = lowerRightElement.y + lowerRightElement.height;
		else botBound = Math.max((lowerLeftElement.y + lowerLeftElement.height),
				(lowerRightElement.y + lowerRightElement.height));
		
		if(!upperLeftExists && lowerLeftExists) leftBound = lowerLeftElement.x - lowerLeftElement.width;
		else if(upperLeftExists && !lowerLeftExists) leftBound = upperLeftElement.x - upperLeftElement.width;
		else leftBound = Math.min(upperLeftElement.x - upperLeftElement.width,
				lowerLeftElement.x - lowerLeftElement.width);
		
		if(!upperRightExists && lowerRightExists) rightBound = lowerRightElement.x + 2*lowerRightElement.width;
		else if(upperRightExists && !lowerRightExists) rightBound = upperRightElement.x + 2*upperRightElement.width;
		else rightBound = Math.max((upperRightElement.x + 2*upperRightElement.width),
				(lowerRightElement.x + 2*lowerRightElement.width));
		
		float width = rightBound - leftBound;
		float height = botBound - topBound;
		
		Rectangle foundRectangle = new Rectangle(topBound, leftBound, width, height);
		stringList.add(foundRectangle);

		return stringList;
	}
}
