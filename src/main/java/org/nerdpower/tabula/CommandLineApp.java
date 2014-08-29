package org.nerdpower.tabula;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.GnuParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.nerdpower.tabula.extractors.BasicExtractionAlgorithm;
import org.nerdpower.tabula.extractors.SpreadsheetExtractionAlgorithm;
import org.nerdpower.tabula.writers.CSVWriter;
import org.nerdpower.tabula.writers.JSONWriter;
import org.nerdpower.tabula.writers.TSVWriter;
import org.nerdpower.tabula.writers.Writer;


public class CommandLineApp {

    private static String VERSION = "0.8.0";
    private static String VERSION_STRING = String.format("tabula %s (c) 2012-2014 Manuel Aristarán", VERSION);
    private static String BANNER = "\nTabula helps you extract tables from PDFs\n\n";

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(buildOptions(), args );
            
            if (line.hasOption('h')) {
                printHelp();
                System.exit(0);
            }
            
            if (line.hasOption('v')) {
                System.out.println(VERSION_STRING);
                System.exit(0);
            }
            
            if (line.getArgs().length != 1) {
                throw new ParseException("Need one filename\nTry --help for help");
            }
                        
            extractTables(line);
            
        }
        catch( ParseException exp ) {
            System.err.println("Error: " + exp.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
    
    static void extractTables(CommandLine line) throws ParseException {
        File pdfFile = new File(line.getArgs()[0]);
        if (!pdfFile.exists()) {
            throw new ParseException("File does not exist");
        }
        
        OutputFormat of = OutputFormat.CSV;
        if (line.hasOption('f')) {
            try {
                of = OutputFormat.valueOf(line.getOptionValue('f'));    
            }
            catch (IllegalArgumentException e) {
                throw new ParseException(String.format(
                        "format %s is illegal. Available formats: %s",
                        line.getOptionValue('f'),
                        Utils.join(",", OutputFormat.formatNames())));
            }
            
        }
        
        Appendable outFile = System.out;
        if (line.hasOption('o')) {
            File file = new File(line.getOptionValue('o'));
            
            try {
                file.createNewFile();
                outFile = new BufferedWriter(new FileWriter(
                        file.getAbsoluteFile()));
            } catch (IOException e) {
                throw new ParseException("Cannot create file "
                        + line.getOptionValue('o'));
            }
        }
        
        Rectangle area = null;
        if (line.hasOption('a')) {
            List<Float> f = parseFloatList(line.getOptionValue('a'));
            if (f.size() != 4) {
                throw new ParseException("area parameters must be top,left,bottom,right");
            }
            area = new Rectangle(f.get(0), f.get(1), f.get(3) - f.get(1), f.get(2) - f.get(0));
        }
        
        List<Float> verticalRulingPositions = null;
        if (line.hasOption('c')) {
            verticalRulingPositions = parseFloatList(line.getOptionValue('c'));
        }
        
        List<Integer> pages = parsePagesOption(line.getOptionValue('p'));
        ExtractionMethod method = whichExtractionMethod(line);
        boolean useLineReturns = line.hasOption('u');
        
        try {
     
            ObjectExtractor oe = line.hasOption('s') ? 
                    new ObjectExtractor(PDDocument.load(pdfFile), line.getOptionValue('s')) : 
                    new ObjectExtractor(PDDocument.load(pdfFile));
            BasicExtractionAlgorithm basicExtractor = new BasicExtractionAlgorithm();
            SpreadsheetExtractionAlgorithm spreadsheetExtractor = new SpreadsheetExtractionAlgorithm();
                    
            PageIterator pageIterator = pages == null ? oe.extract() : oe.extract(pages);
            Page page;
            List<Table> tables = new ArrayList<Table>();

            while (pageIterator.hasNext()) {
                page = pageIterator.next();
                
                if (area != null) {
                    page = page.getArea(area);
                }

                if (method == ExtractionMethod.DECIDE) {
                    method = spreadsheetExtractor.isTabular(page) ? ExtractionMethod.SPREADSHEET : ExtractionMethod.BASIC;
                }
                
                switch(method) {
                case BASIC:
                    if (line.hasOption('g')) {
                        
                    }
                    tables.addAll(verticalRulingPositions == null ? basicExtractor.extract(page) : basicExtractor.extract(page, verticalRulingPositions));
                    
                    break;
                case SPREADSHEET:
                    // TODO add useLineReturns
                    tables.addAll(spreadsheetExtractor.extract(page));
                default:
                    break;
                }
                for (Table t: tables) {
                    writeTables(of, tables, outFile);
                }
                tables.clear();
            }

        } catch (IOException e) {
            throw new ParseException(e.getMessage());
        }

    }
    
    static void writeTables(OutputFormat format, Iterable<Table> tables, Appendable out) throws IOException {
        Writer writer = null;
        switch (format) {
        case CSV:
            writer = new CSVWriter();
            break;
        case JSON:
            writer = new JSONWriter();
            break;
        case TSV:
            writer = new TSVWriter();
            break;
        }
        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            writer.write(out, iter.next());
        }
    }
    
    static ExtractionMethod whichExtractionMethod(CommandLine line) {
        ExtractionMethod rv = ExtractionMethod.DECIDE;
        if (line.hasOption('r')) {
            rv = ExtractionMethod.SPREADSHEET;
        }
        else if (line.hasOption('n') || line.hasOption('c') || line.hasOption('a') || line.hasOption('g')) {
            rv = ExtractionMethod.BASIC;
        }
        return rv;
    }
    
    public static List<Integer> parsePagesOption(String pagesSpec) throws ParseException {
        if (pagesSpec.equals("all")) {
            return null;
        }
        
        List<Integer> rv = new ArrayList<Integer>();
        
        String[] ranges = pagesSpec.split(",");
        for (int i = 0; i < ranges.length; i++) {
            String[] r = ranges[i].split("-");
            if (r.length == 0 || !Utils.isNumeric(r[0]) || r.length > 1 && !Utils.isNumeric(r[1])) {
                throw new ParseException("Syntax error in page range specification");
            }
            
            if (r.length < 2) {
                rv.add(Integer.parseInt(r[0]));
            }
            else {
                int t = Integer.parseInt(r[0]);
                int f = Integer.parseInt(r[1]); 
                if (t > f) {
                    throw new ParseException("Syntax error in page range specification");
                }
                rv.addAll(Utils.range(t, f+1));       
            }
        }
        
        Collections.sort(rv);
        return rv;
    }
    
    public static List<Float> parseFloatList(String option) throws ParseException {
        String[] f = option.split(",");
        List<Float> rv = new ArrayList<Float>();
        try {
            for (int i = 0; i < f.length; i++) {
                rv.add(Float.parseFloat(f[i]));
            }
            return rv;
        } catch (NumberFormatException e) {
            throw new ParseException("Wrong number syntax");
        }
    }
    
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("tabula", BANNER, buildOptions(), "", true);
    }
    
    @SuppressWarnings("static-access")
    static Options buildOptions() {
        Options o = new Options();
        
        o.addOption("v", "version", false, "Print version and exit.");
        o.addOption("h", "help", false, "Print this help text.");
        o.addOption("g", "guess", false, "Guess the portion of the page to analyze per page.");
        o.addOption("d", "debug", false, "Print detected table areas instead of processing");
        o.addOption("r", "spreadsheet", false, "Force PDF to be extracted using spreadsheet-style extraction (if there are ruling lines separating each cell, as in a PDF of an Excel spreadsheet)");
        o.addOption("n", "no-spreadsheet", false, "Force PDF not to be extracted using spreadsheet-style extraction (if there are ruling lines separating each cell, as in a PDF of an Excel spreadsheet)");
        o.addOption("i", "silent", false, "Suppress all stderr output.");
        o.addOption("u", "use-line-returns", false, "Use embedded line returns in cells. (Only in spreadsheet mode.)");
        o.addOption("d", "debug", false, "Print detected table areas instead of processing.");
        o.addOption(OptionBuilder.withLongOpt("outfile")
                                 .withDescription("Write output to <file> instead of STDOUT. Default: -")
                                 .hasArg()
                                 .withArgName("OUTFILE")
                                 .create("o"));
        o.addOption(OptionBuilder.withLongOpt("format")
                                 .withDescription("Output format: (" + Utils.join(",", OutputFormat.formatNames()) + "). Default: CSV")
                                 .hasArg()
                                 .withArgName("FORMAT")
                                 .create("f"));
        o.addOption(OptionBuilder.withLongOpt("password")
                                 .withDescription("Password to decrypt document. Default is empty")
                                 .hasArg()
                                 .withArgName("PASSWORD")
                                 .create("s"));
        o.addOption(OptionBuilder.withLongOpt("columns")
                                 .withDescription("X coordinates of column boundaries. Example --columns 10.1,20.2,30.3")
                                 .hasArg()
                                 .withArgName("COLUMNS")
                                 .create("c"));
        o.addOption(OptionBuilder.withLongOpt("area")
                                 .withDescription("Portion of the page to analyze (top,left,bottom,right). Example: --area 269.875,12.75,790.5,561. Default is entire page")
                                 .hasArg()
                                 .withArgName("AREA")
                                 .create("a"));
        o.addOption(OptionBuilder.withLongOpt("pages")
                                 .withDescription("Comma separated list of ranges, or all. Examples: --pages 1-3,5-7, --pages 3 or --pages all. Default is --pages 1")
                                 .hasArg()
                                 .withArgName("PAGES")
                                 .create("p"));
        
        return o;
    }
    
    private enum OutputFormat {
        CSV,
        TSV,
        JSON;
        
        static String[] formatNames() {
            OutputFormat[] values = OutputFormat.values();
            String[] rv = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                rv[i] = values[i].name();
            }
            return rv;
        }
        
    }
        
    private enum ExtractionMethod {
        BASIC,
        SPREADSHEET,
        DECIDE
    }
    
    private class DebugOutput {
        private boolean debugEnabled;

        public DebugOutput(boolean debug) {
            this.debugEnabled = debug;
        }
        
        public void debug(String msg) {
            if (this.debugEnabled) {
                System.err.println(msg);    
            }
        }
    }
}
