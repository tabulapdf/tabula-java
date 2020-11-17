package technology.tabula.algorithms.extractors;

import java.util.List;

import technology.tabula.pages.Page;
import technology.tabula.tables.Table;

public interface ExtractionAlgorithm {

    List<? extends Table> extract(Page page);

    String toString();
    
}
