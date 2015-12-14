package technology.tabula.detectors;

import technology.tabula.Page;
import technology.tabula.Rectangle;

import java.util.List;

/**
 * Created by matt on 2015-12-14.
 */
public interface DetectionAlgorithm {
    List<Rectangle> detect(Page page);
}
