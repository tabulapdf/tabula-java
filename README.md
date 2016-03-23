tabula-java [![Build Status](https://travis-ci.org/tabulapdf/tabula-java.svg?branch=master)](https://travis-ci.org/tabulapdf/tabula-java) [![Join the chat at https://gitter.im/tabulapdf/tabula-java](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tabulapdf/tabula-java?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===========

`tabula-java` is a library for extracting tables from PDF files. It is a Java rewrite of [`tabula-extractor`](http://github.com/tabulapdf/tabula-extractor), that is a thin wrapper around this library.

## Download

Download a version of the tabula-java's jar, with all dependencies included, that works on Mac, Windows and Linux from our [releases page](../../releases).

## Build instructions

Clone this repo and run:

```
mvn clean compile assembly:single
```

## Examples

`tabula-java` provides a command line application:

```
$ java -jar ./target/tabula-0.8.0-jar-with-dependencies.jar --help

usage: tabula [-a <AREA>] [-c <COLUMNS>] [-d] [-f <FORMAT>] [-g] [-h] [-i]
       [-n] [-o <OUTFILE>] [-p <PAGES>] [-r] [-s <PASSWORD>] [-u] [-v]

Tabula helps you extract tables from PDFs
 -a,--area <AREA>           Portion of the page to analyze
                            (top,left,bottom,right). Example: --area
                            269.875,12.75,790.5,561. Default is entire
                            page
 -c,--columns <COLUMNS>     X coordinates of column boundaries. Example
                            --columns 10.1,20.2,30.3
 -d,--debug                 Print detected table areas instead of
                            processing.
 -f,--format <FORMAT>       Output format: (CSV,TSV,JSON). Default: CSV
 -g,--guess                 Guess the portion of the page to analyze per
                            page.
 -h,--help                  Print this help text.
 -i,--silent                Suppress all stderr output.
 -n,--no-spreadsheet        Force PDF not to be extracted using
                            spreadsheet-style extraction (if there are
                            ruling lines separating each cell, as in a PDF
                            of an Excel spreadsheet)
 -o,--outfile <OUTFILE>     Write output to <file> instead of STDOUT.
                            Default: -
 -p,--pages <PAGES>         Comma separated list of ranges, or all.
                            Examples: --pages 1-3,5-7, --pages 3 or
                            --pages all. Default is --pages 1
 -r,--spreadsheet           Force PDF to be extracted using
                            spreadsheet-style extraction (if there are
                            ruling lines separating each cell, as in a PDF
                            of an Excel spreadsheet)
 -s,--password <PASSWORD>   Password to decrypt document. Default is empty
 -u,--use-line-returns      Use embedded line returns in cells. (Only in
                            spreadsheet mode.)
 -v,--version               Print version and exit.

```

It also includes a debugging tool, run `java -cp ./target/tabula-0.8.0-jar-with-dependencies.jar technology.tabula.debug.Debug -h` for the available options.

You can also integrate `tabula-java` with any JVM language. For Java examples, see the [`tests`](src/test/java/technology/tabula/) folder.

© 2014 Manuel Aristarán. Available under MIT License. See [`LICENSE`](LICENSE).
