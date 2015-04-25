package technology.tabula.extractors;

import java.util.List;

import technology.tabula.Page;
import technology.tabula.Table;

public interface ExtractionAlgorithm {

    List<? extends Table> extract(Page page);
    String toString();
    
}
