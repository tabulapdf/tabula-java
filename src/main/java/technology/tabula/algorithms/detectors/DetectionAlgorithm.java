package technology.tabula.algorithms.detectors;

import technology.tabula.Rectangle;
import technology.tabula.page.Page;

import java.util.List;

/**
 * Created by matt on 2015-12-14.
 */
public interface DetectionAlgorithm {

    List<Rectangle> detect(Page page);

}
