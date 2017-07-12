package technology.tabula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

import technology.tabula.extractors.BatchSelectionExtractor;

public class TestBatchExtractor {
	// parameters
	String batchPath = "src/test/resources/technology/tabula/batch";
	String inputPath = batchPath + "/input";
	String outputPath = batchPath + "/output";
	String jsonPath = batchPath + "/strings.json";
	String processType = "string";
	int overlapThreshold = 100;
	
	// expected values
	String[] ocr_names = {"well_image_a.csv", "well_text_a.csv", "well_text_b.csv", "well_text_c.csv"};
	String[] no_ocr_names = {"well_text_a.csv", "well_text_b.csv", "well_text_c.csv"};
	
	// empty a directory in the workspace
	public void emptyFolder(String path) {
		File[] outputFolder = new File(path).listFiles();
		if (outputFolder != null) {
			for (File f : outputFolder) {
				f.delete();
			}
		} else {
			try {
				Files.createDirectory(Paths.get(outputPath));
			} catch (IOException e) {
				fail("Could not create output directory.");
			}
		}
	}
	
	// check two files are equal, in name and content
	public void checkEquality(File expectedFile, File outputFile) {
		assertEquals(expectedFile.getName(), outputFile.getName());
		
		try {
			byte[] expectedBytes = Files.readAllBytes(FileSystems.getDefault().getPath(expectedFile.getAbsolutePath()));
			byte[] outputBytes = Files.readAllBytes(FileSystems.getDefault().getPath(outputFile.getAbsolutePath()));
			assertTrue(Arrays.equals(expectedBytes, outputBytes));
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testBatchExtractionWithOCR() {
		boolean ocrAllowed = true;
		
		// empty output directory
		emptyFolder(outputPath);
		
		// run extraction
		BatchSelectionExtractor batch = new BatchSelectionExtractor();
		batch.extract(inputPath, outputPath, jsonPath, processType, ocrAllowed, overlapThreshold);
		
		// verify output folders
		File outputDir = new File(outputPath);
		assertNotNull(outputDir);
		
		File[] files = outputDir.listFiles();
		assertNotNull(files);
		assertEquals(4, files.length);
		Arrays.sort(files);
		for (int i = 0; i < 4; i++) {
			checkEquality(new File(batchPath + "/expected/" + ocr_names[i]), files[i]);
		}
		
		// empty output directory
		emptyFolder(outputPath);
	}
	
	@Test
	public void testBatchExtractionWithoutOCR() {
		boolean ocrAllowed = false;
		
		// empty output directory
		emptyFolder(outputPath);
		
		// run extraction
		BatchSelectionExtractor batch = new BatchSelectionExtractor();
		batch.extract(inputPath, outputPath, jsonPath, processType, ocrAllowed, overlapThreshold);
		
		// verify output folders
		File outputDir = new File(outputPath);
		assertNotNull(outputDir);
		
		File[] files = outputDir.listFiles();
		assertNotNull(files);
		assertEquals(4, files.length);
		Arrays.sort(files);
		// special case
		assertEquals("well_image_a.csv", files[0].getName());
		try {
			assertEquals(0, Files.readAllBytes(FileSystems.getDefault().getPath(files[0].getAbsolutePath())).length);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		for (int i = 0; i < 3; i++) {
			checkEquality(new File(batchPath + "/expected/" + no_ocr_names[i]), files[i+1]);
		}
		
		// empty output directory
		emptyFolder(outputPath);
	}
}
