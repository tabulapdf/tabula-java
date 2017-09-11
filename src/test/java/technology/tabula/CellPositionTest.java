package technology.tabula;

import org.junit.Test;
import technology.tabula.Table.CellPosition;

import static org.assertj.core.api.Assertions.assertThat;

public class CellPositionTest {

	@Test
	public void testHashCode() {
		Table table = new Table();
		CellPosition cellPosition = table.new CellPosition(5,  5);
		
		assertThat(cellPosition.hashCode()).isEqualTo(500005);
	}

	@Test
	public void testEqualsObject() {
		Table table = new Table();
		CellPosition cellPosition1 = table.new CellPosition(5,  5);

		assertThat(cellPosition1).isEqualTo(cellPosition1);
	}
	
	@Test
	public void testNotEqualsObject() {
		Table table = new Table();
		CellPosition cellPosition1 = table.new CellPosition(5,  5);
		CellPosition cellPosition2 = table.new CellPosition(5,  6);

		assertThat(cellPosition1).isNotEqualTo(cellPosition2);
	}
	
	@Test
	public void testNotInstanceOfObject() {
		Table table = new Table();
		CellPosition cellPosition = table.new CellPosition(5,  5);

		assertThat(cellPosition).isNotEqualTo("test");
	}
}
