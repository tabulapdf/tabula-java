package technology.tabula;

import static org.junit.Assert.*;

import org.junit.Test;

public class TableTest {

	@Test public void testEmpty() {
		Table empty = Table.empty();

		assertEquals(TextChunk.EMPTY, empty.getCell(0, 0));
		assertEquals(TextChunk.EMPTY, empty.getCell(1, 1));
		
		assertEquals(0, empty.getRowCount());
		assertEquals(0, empty.getColCount());
		
		assertEquals("", empty.getExtractionMethod());
		
		assertEquals(0, empty.getTop(), 0);
		assertEquals(0, empty.getRight(), 0);
		assertEquals(0, empty.getBottom(), 0);
		assertEquals(0, empty.getLeft(), 0);
		
		assertEquals(0, empty.getArea(), 0);
	}

	@Test public void testRowColCounts() {
		Table table = Table.empty();

		assertEquals(0, table.getRowCount());
		assertEquals(0, table.getColCount());

		table.add(TextChunk.EMPTY, 0, 0);

		assertEquals(1, table.getRowCount());
		assertEquals(1, table.getColCount());

		table.add(TextChunk.EMPTY, 9, 9);

		assertEquals(10, table.getRowCount());
		assertEquals(10, table.getColCount());
	}

}
