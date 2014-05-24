package org.nerdpower.tabula;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.GnuParser;


public class Main {

    private static String VERSION = "0.8.0";
    private static String[] FORMATS = { "CSV", "TSV", "JSON" };
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
            
            if (line.getArgs().length != 1) {
                throw new ParseException("Need one filename\nTry --help for help");
            }
            
            File pdfFile = new File(line.getArgs()[0]);
            if (!pdfFile.exists()) {
                throw new ParseException("File does not exist");
            }
            
            List<Integer> pages = parsePagesOption(line.getOptionValue('p'));

            
            System.out.println(join("\n", line.getArgs()));
            
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println("Error:" + exp.getMessage());
            System.exit(1);
        }
    }
    
    static List<Integer> parsePagesOption(String pagesSpec) throws ParseException {
        if (pagesSpec == "all") {
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
    
    static List<Float> parseFloatList(String option) throws ParseException {
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
                                 .withDescription("Output format: (" + join(",", FORMATS) + "). Default: CSV")
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
    
    private static String join(String glue, String...s) {
        int k = s.length;
        if ( k == 0 )
        {
          return null;
        }
        StringBuilder out = new StringBuilder();
        out.append( s[0] );
        for ( int x=1; x < k; ++x )
        {
          out.append(glue).append(s[x]);
        }
        return out.toString();
    }

}
