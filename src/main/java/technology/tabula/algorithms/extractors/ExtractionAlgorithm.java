package technology.tabula.algorithms.extractors;

import java.util.List;

import technology.tabula.page.Page;
import technology.tabula.table.Table;

public interface ExtractionAlgorithm {

    List<? extends Table> extract(Page page);

    String toString();
    
}
