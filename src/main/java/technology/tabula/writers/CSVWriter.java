package technology.tabula.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;

import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

public class CSVWriter implements Writer {

	public CSVWriter() {
		this(CSVFormat.EXCEL);
	}

	protected CSVWriter(CSVFormat format) {
		this.format = format;
	}

	private final CSVFormat format;

	@Override
	public void write(Appendable out, Table table) throws IOException {
		write(out, Collections.singletonList(table));
	}

	@Override
	public void write(Appendable out, List<Table> tables) throws IOException {
		try (CSVPrinter printer = new CSVPrinter(out, format)) {
			for (Table table : tables) {
				for (List<RectangularTextContainer> row : table.getRows()) {
					List<String> cells = new ArrayList<>(row.size());
					for (RectangularTextContainer<?> tc : row) cells.add(tc.getText());
					printer.printRecord(cells);
				}
			}
			printer.flush();
		}
	}

}
