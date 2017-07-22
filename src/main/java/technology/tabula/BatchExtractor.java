package technology.tabula;

import java.util.List;
import java.util.Map;

import technology.tabula.extractors.BatchExtractionAlgorithm;
import technology.tabula.writers.BatchWriter;

public class BatchExtractor {

	public boolean performExtraction(String inputPath, String outputPath, String jsonPath, String processType,
			boolean ocrAllowed, int overlapThreshold) {
		BatchExtractionAlgorithm bea = new BatchExtractionAlgorithm();
		Map<String, List<Table>> tables = bea.extract(inputPath, jsonPath, processType, ocrAllowed, overlapThreshold);
		BatchWriter bw = new BatchWriter();
		bw.write(tables, outputPath);
		return true;
	}
}
