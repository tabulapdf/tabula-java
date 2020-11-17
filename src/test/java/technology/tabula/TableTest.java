package technology.tabula;

import static org.junit.Assert.*;
import static technology.tabula.TextChunk.EMPTY;

import org.junit.Test;

public class TableTest {

	@Test
	public void testEmpty() {
		Table table = Table.empty();

		assertEquals(EMPTY, table.getCell(0, 0));
		assertEquals(EMPTY, table.getCell(1, 1));
		
		assertEquals(0, table.getRowCount());
		assertEquals(0, table.getColCount());
		
		assertEquals("", table.getExtractionMethod());
		
		assertEquals(0, table.getTop(), 0);
		assertEquals(0, table.getRight(), 0);
		assertEquals(0, table.getBottom(), 0);
		assertEquals(0, table.getLeft(), 0);
		
		assertEquals(0, table.getArea(), 0);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	@Test
	public void testRowColCounts() {
		Table table = Table.empty();

		assertEquals(0, table.getRowCount());
		assertEquals(0, table.getColCount());

		table.add(EMPTY, 0, 0);

		assertEquals(1, table.getRowCount());
		assertEquals(1, table.getColCount());

		table.add(EMPTY, 9, 9);

		assertEquals(10, table.getRowCount());
		assertEquals(10, table.getColCount());
	}

}
