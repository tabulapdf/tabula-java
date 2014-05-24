package org.nerdpower.tabula;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMainApp {

    @Test
    public void testParsePagesOption() throws ParseException {
        
        List<Integer> rv = Main.parsePagesOption("1");
        assertEquals(new Integer[] { 1 }, rv.toArray());
        
        rv = Main.parsePagesOption("1-4");
        assertEquals(new Integer[] { 1,2,3,4 }, rv.toArray());
        
        rv = Main.parsePagesOption("1-4,20-24");
        assertEquals(new Integer[] { 1,2,3,4,20,21,22,23,24 }, rv.toArray());
        
        rv = Main.parsePagesOption("all");
        assertNull(rv);
    }
    
    @Test(expected=ParseException.class)
    public void testExceptionInParsePages() throws ParseException {
        Main.parsePagesOption("1-4,24-22");
    }

    @Test(expected=ParseException.class)
    public void testAnotherExceptionInParsePages() throws ParseException {
        Main.parsePagesOption("quuxor");
    }


}
