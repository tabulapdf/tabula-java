package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nerdpower.tabula.Page;
import org.nerdpower.tabula.Ruling;
import org.nerdpower.tabula.Table;
import org.nerdpower.tabula.TextChunk;
import org.nerdpower.tabula.extractors.BasicExtractionAlgorithm;
import org.nerdpower.tabula.writers.CSVWriter;


public class TestBasicExtractor {

    @Test
    public void testRemoveSequentialSpaces() throws IOException {
        Page page = UtilsForTesting.getAreaFromFirstPage(
                "src/test/resources/org/nerdpower/tabula/m27.pdf", 79.2f,
                28.28f, 103.04f, 732.6f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        List<TextChunk> firstRow = table.getRows().get(0);

        assertTrue(firstRow.get(2).getText().equals("ALLEGIANT AIR"));
        assertTrue(firstRow.get(3).getText().equals("ALLEGIANT AIR LLC"));
    }
    
    @Test
    public void testColumnRecognition() throws IOException {
        // TODO add assertions
        Page page = UtilsForTesting
                .getAreaFromFirstPage(
                        "src/test/resources/org/nerdpower/tabula/argentina_diputados_voting_record.pdf",
                        269.875f, 12.75f, 790.5f, 561f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        Table table = bea.extract(page).get(0);
        (new CSVWriter()).write(System.out, table);
    }
    
    @Test
    public void testVerticalRulingsPreventMergingOfColumns() throws IOException {
        List<Ruling> rulings = new ArrayList<Ruling>();
        Float[] rulingsVerticalPositions = { 147f, 256f, 310f, 375f, 431f, 504f };
        for (int i = 0; i < 6; i++) {
            rulings.add(new Ruling(0, rulingsVerticalPositions[i], 0, 1000));
        }

        Page page = UtilsForTesting.getAreaFromFirstPage(
                "src/test/resources/org/nerdpower/tabula/campaign_donors.pdf",
                255.57f, 40.43f, 398.76f, 557.35f);
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm(rulings);
        Table table = bea.extract(page).get(0);
        List<TextChunk> sixthRow = table.getRows().get(5);

        assertTrue(sixthRow.get(0).getText().equals("VALSANGIACOMO BLANC"));
        assertTrue(sixthRow.get(1).getText().equals("OFERNANDO JORGE "));
    }    

}
