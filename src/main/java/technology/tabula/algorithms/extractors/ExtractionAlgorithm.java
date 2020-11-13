package technology.tabula.algorithms.extractors;

import technology.tabula.page.Page;
import technology.tabula.table.Table;

import java.util.List;

public interface ExtractionAlgorithm {

    List<? extends Table> extract(Page page);

    String toString();
    
}
