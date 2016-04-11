package technology.tabula;

import static org.junit.Assert.*;

import org.junit.Test;

import technology.tabula.Table.CellPosition;

public class TestCellPosition {

	@Test
	public void testHashCode() {
		Table table = new Table();
		CellPosition cellPosition = table.new CellPosition(5,  5);
		
		assertEquals(500005, cellPosition.hashCode());

	}

	@Test
	public void testEqualsObject() {
		Table table = new Table();
		CellPosition cellPosition1 = table.new CellPosition(5,  5);

		assertTrue(cellPosition1.equals(cellPosition1));
	}
	
	@Test
	public void testNotEqualsObject() {
		Table table = new Table();
		CellPosition cellPosition1 = table.new CellPosition(5,  5);
		CellPosition cellPosition2 = table.new CellPosition(5,  6);

		assertFalse(cellPosition1.equals(cellPosition2));
	}
	
	@Test
	public void testNotInstanceOfObject() {
		Table table = new Table();
		CellPosition cellPosition = table.new CellPosition(5,  5);

		assertFalse(cellPosition.equals("test"));
	}

}
