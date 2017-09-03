package technology.tabula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import technology.tabula.extractors.ExtractionAlgorithm;

@SuppressWarnings("serial")
public class TableWithRulingLines extends Table {

    List<Ruling> verticalRulings, horizontalRulings;
    RectangleSpatialIndex<Cell> si = new RectangleSpatialIndex<>();
    
    public TableWithRulingLines(Rectangle area, List<Cell> cells, List<Ruling> horizontalRulings, List<Ruling> verticalRulings, ExtractionAlgorithm extractionAlgorithm) {
        super(extractionAlgorithm);
        this.setRect(area);
        this.verticalRulings = verticalRulings;
        this.horizontalRulings = horizontalRulings;
        this.addCells(cells);
    }
    
    private void addCells(List<Cell> cells) {

        if (cells.isEmpty()) {
            return;
        } 
        
        for (Cell ce: cells) {
            si.add(ce);
        }
        
        List<List<Cell>> rowsOfCells = rowsOfCells(cells);
        for (int i = 0; i < rowsOfCells.size(); i++) {
            List<Cell> row = rowsOfCells.get(i);
            Iterator<Cell> rowCells = row.iterator();
            Cell cell = rowCells.next();
            List<List<Cell>> others = rowsOfCells(
                    si.contains(
                            new Rectangle(cell.getBottom(), si.getBounds().getLeft(), cell.getLeft() - si.getBounds().getLeft(), 
                                    si.getBounds().getBottom() - cell.getBottom())
                            ));
            int startColumn = 0;
            for (List<Cell> r: others) {
                startColumn = Math.max(startColumn, r.size());
            }
            this.add(cell, i, startColumn++);
            while (rowCells.hasNext()) {
                this.add(rowCells.next(), i, startColumn++);
            }
        }
    }
    
    private static List<List<Cell>> rowsOfCells(List<Cell> cells) {
        Cell c;
        float lastTop;
        List<List<Cell>> rv = new ArrayList<>();
        List<Cell> lastRow;
        
        if (cells.isEmpty()) {
            return rv;
        }
        
        Collections.sort(cells, new Comparator<Cell>() {
            @Override
            public int compare(Cell arg0, Cell arg1) {
                return java.lang.Double.compare(arg0.getTop(), arg1.getTop());
            }
        });
        
        
        Iterator<Cell> iter = cells.iterator();
        c = iter.next();
        lastTop = c.getTop();
        lastRow = new ArrayList<>();
        lastRow.add(c);
        rv.add(lastRow);
        
        while (iter.hasNext()) {
            c = iter.next();
            if (!Utils.feq(c.getTop(), lastTop)) {
                lastRow = new ArrayList<>();
                rv.add(lastRow);
            }
            lastRow.add(c);
            lastTop = c.getTop();
        }
        return rv;
    }

}
