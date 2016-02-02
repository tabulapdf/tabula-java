package technology.tabula.detectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.Page;
import technology.tabula.Rectangle;

import java.io.File;
import java.util.List;

/**
 * Created by matt on 2015-12-14.
 */
public interface DetectionAlgorithm {
    List<Rectangle> detect(Page page);
}
