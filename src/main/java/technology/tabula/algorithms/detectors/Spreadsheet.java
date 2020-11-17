package technology.tabula.algorithms.detectors;

import technology.tabula.texts.Cell;
import technology.tabula.pages.Page;
import technology.tabula.Rectangle;

import java.util.Collections;
import java.util.List;

/**
 * Created by matt on 2015-12-14.
 *
 * This is the basic spreadsheet table detection algorithm currently implemented in tabula (web).
 *
 * It uses intersecting ruling lines to find tables.
 */
public class Spreadsheet implements DetectionAlgorithm {

    @Override
    public List<Rectangle> detect(Page page) {
        List<Cell> cells = technology.tabula.algorithms.extractors.Spreadsheet.findCells(page.getHorizontalRulings(), page.getVerticalRulings());

        List<Rectangle> tables = technology.tabula.algorithms.extractors.Spreadsheet.findSpreadsheetsFromCells(cells);

        // we want tables to be returned from top to bottom on the page
        Collections.sort(tables, Rectangle.ILL_DEFINED_ORDER);

        return tables;
    }

}
