package technology.tabula.detectors;

import technology.tabula.Cell;
import technology.tabula.Page;
import technology.tabula.Rectangle;
import technology.tabula.Ruling;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.util.List;

/**
 * Created by matt on 2015-12-14.
 */
public class SpreadsheetDetectionAlgorithm implements DetectionAlgorithm {
    @Override
    public List<Rectangle> detect(Page page) {
        List<Cell> cells = SpreadsheetExtractionAlgorithm.findCells(page.getHorizontalRulings(), page.getVerticalRulings());

        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        return sea.findSpreadsheetsFromCells(cells);
    }
}
