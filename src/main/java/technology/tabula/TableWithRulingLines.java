package technology.tabula;

import java.awt.geom.Point2D;
import java.util.*;

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

        Map<Integer, Cell> lastSuccessfulColCells = new HashMap<>();

        boolean noBorders = false;

        // sometimes there are multiple vertical rulings who are basically the same because the are on top of each other caused by row spans
        List<java.lang.Float> leftValuesOfVerticalRulings = unionLeftValuesOfVerticalRulings();
        leftValuesOfVerticalRulings.add(1000000.0F);
        List<java.lang.Float> topValuesOfHorizontalRulings = unionTopValuesOfHorizontalRulings();
        topValuesOfHorizontalRulings.add(1000000.0F);


        List<List<Cell>> rowsOfCells = rowsOfCells(cells);
        Iterator<List<Cell>> rowIterator = rowsOfCells.iterator();

        for (int rowId = 0; rowId < topValuesOfHorizontalRulings.size(); rowId++) {
            Cell currentCell = null;
            Iterator<Cell> colIterator = null;
            if (rowIterator.hasNext()) {
                List<Cell> row = rowIterator.next();

                colIterator = row.iterator();
                currentCell = colIterator.hasNext() ? colIterator.next() : null;
            }
            Cell lastSuccessfulRowCell = null;

            for (int colId = 0; colId < leftValuesOfVerticalRulings.size(); colId++) {
                Cell cellToAdd = null;
                boolean newCellFound = false;
                if (currentCell != null && currentCell.getLeft() < leftValuesOfVerticalRulings.get(colId)) {
                    if (colId == 0) {
                        noBorders = true;
                    }
                    cellToAdd = currentCell;
                    newCellFound = true;
                } else {
                    // possible cell span
                    if (lastSuccessfulRowCell != null) {
                        // check if the cell is really spaning over the current vertical ruling
                        if (leftValuesOfVerticalRulings.get(colId) <= lastSuccessfulRowCell.getRight()) {
                            cellToAdd = lastSuccessfulRowCell;
                            cellToAdd.setSpanning(true);
                        }
                    }

                    if (rowId > 0) {
                        Cell upperCell = lastSuccessfulColCells.get(colId);
                        // check if the cell is really spaning over the current horizontal ruling
                        if (upperCell != null && topValuesOfHorizontalRulings.get(rowId) < upperCell.getBottom()) {
                            cellToAdd = upperCell;
                            cellToAdd.setSpanning(true);
                        }
                    }
                }

                if (cellToAdd != null) {
                    if (noBorders) {
                        this.add(cellToAdd, rowId, colId);
                    } else {
                        this.add(cellToAdd, rowId, colId - 1);
                    }

                    lastSuccessfulRowCell = cellToAdd;
                    lastSuccessfulColCells.put(colId, cellToAdd);

                    if (newCellFound) {
                        if (colIterator != null && colIterator.hasNext()) {
                            currentCell = colIterator.next();
                        } else {
                            currentCell = null;
                        }
                    }
                }
            }
        }
    }

    private List<java.lang.Float> unionLeftValuesOfVerticalRulings() {
        List<java.lang.Float> leftValues = new ArrayList<>();

        for (Ruling verticalRuling : verticalRulings) {
            boolean found = false;

            for (float leftValue : leftValues) {
                if(Math.abs(verticalRuling.getLeft() - leftValue) < 0.1) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                leftValues.add(verticalRuling.getLeft());
            }
        }

        return leftValues;
    }

    private List<java.lang.Float> unionTopValuesOfHorizontalRulings() {
        List<java.lang.Float> topValues = new ArrayList<>();

        for (Ruling horizontalRuling : horizontalRulings) {
            boolean found = false;

            for (float topValue : topValues) {
                if(Math.abs(horizontalRuling.getTop() - topValue) < 0.1) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                topValues.add(horizontalRuling.getTop());
            }
        }

        return topValues;
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
