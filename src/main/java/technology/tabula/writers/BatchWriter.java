package technology.tabula.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import technology.tabula.Table;

public class BatchWriter {

	public void write(Map<String, List<Table>> tables, String outputDirectory) {
		Logger log = LoggerFactory.getLogger(BatchWriter.class);
		try {
			if (!new File(outputDirectory).isDirectory()) {
				throw new FileNotFoundException("Output path does not point to a directory");
			} else {
				Set<String> fileKeys = tables.keySet();
				for (String sCurrentFile : fileKeys) {
					String tempFileName = sCurrentFile.replaceFirst("(\\.pdf|)$", ".csv");
					File outputFile = new File(outputDirectory, new File(tempFileName).getName());
					outputFile.createNewFile();
					FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					CSVWriter csvW = new CSVWriter();
					csvW.write(bufferedWriter, tables.get(sCurrentFile));
					bufferedWriter.close();
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
