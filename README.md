tabula-java [![Build Status](https://travis-ci.org/tabulapdf/tabula-java.svg?branch=master)](https://travis-ci.org/tabulapdf/tabula-java) [![Join the chat at https://gitter.im/tabulapdf/tabula-java](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tabulapdf/tabula-java?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===========

`tabula-java` is a library for extracting tables from PDF files — it is the table extraction engine that powers [Tabula](http://tabula.technology/) ([repo](http://github.com/tabulapdf/tabula)). You can use `tabula-java` as a command-line tool to programmatically extract tables from PDFs.

(This is the new version of the extraction engine; the previous code can be found at [`tabula-extractor`](http://github.com/tabulapdf/tabula-extractor).)

© 2014-2016 Manuel Aristarán. Available under MIT License. See [`LICENSE`](LICENSE).

## Download

Download a version of the tabula-java's jar, with all dependencies included, that works on Mac, Windows and Linux from our [releases page](../../releases).

## Usage Examples

`tabula-java` provides a command line application:

```
$ java -jar ./target/tabula-0.9.2-jar-with-dependencies.jar --help

usage: tabula [-a <AREA>] [-b <DIRECTORY>] [-c <COLUMNS>] [-d] [-f <FORMAT>] [-g] [-h] [-i]
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
 -b,--batch <DIRECTORY>     Convert all .pdfs in the provided directory

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

It also includes a debugging tool, run `java -cp ./target/tabula-0.9.1-jar-with-dependencies.jar technology.tabula.debug.Debug -h` for the available options.

You can also integrate `tabula-java` with any JVM language. For Java examples, see the [`tests`](src/test/java/technology/tabula/) folder.

JVM start-up time is a lot of the cost of the `tabula` command, so if you're trying to extract many tables from PDFs, you have a few options for speeding it up:

 - the [drip](https://github.com/ninjudd/drip) utility
 - the [Ruby](http://github.com/tabulapdf/tabula-extractor), [Python](https://github.com/chezou/tabula-py), [R](https://github.com/leeper/tabulizer), and [Node.js](https://github.com/ezodude/tabula-js) bindings
 - writing your own program in any JVM language (Java, JRuby, Scala) that imports tabula-java.
 - waiting for us to implement an API/server-style system (it's on the [roadmap](https://github.com/tabulapdf/tabula-api))

## Building from Source

Clone this repo and run:

```
mvn clean compile assembly:single
```

## Contributing

Interested in helping out? We'd love to have your help!

You can help by:

- [Reporting a bug](https://github.com/tabulapdf/tabula-java/issues).
- Adding or editing documentation.
- Contributing code via a Pull Request.
- Spreading the word about `tabula-java` to people who might be able to benefit from using it.

### Backers

You can also support our continued work on `tabula-java` with a one-time or monthly donation [on OpenCollective](https://opencollective.com/tabulapdf#support). Organizations who use `tabula-java` can also [sponsor the project](https://opencollective.com/tabulapdf#support) for acknolwedgement on [our official site](http://tabula.technology/) and this README.

Special thanks to the following users and organizations for generously supporting Tabula with donations and grants:

<a href="https://opencollective.com/tabulapdf/backer/0/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/0/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/1/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/1/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/2/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/2/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/3/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/3/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/4/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/4/avatar"></a>
<a href="https://opencollective.com/tabulapdf/backer/5/website" target="_blank"><img src="https://opencollective.com/tabulapdf/backer/5/avatar"></a>

<a title="The John S. and James L. Knight Foundation" href="http://www.knightfoundation.org/" target="_blank"><img alt="The John S. and James L. Knight Foundation" src="http://www.knightfoundation.org/media/uploads/media_images/knight-logo-300.jpg"></a>
<a title="The Shuttleworth Foundation" href="https://shuttleworthfoundation.org/" target="_blank"><img width="200" alt="The Shuttleworth Foundation" src="https://raw.githubusercontent.com/tabulapdf/tabula/gh-pages/shuttleworth.jpg"></a>
