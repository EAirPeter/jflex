/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex 1.7.1-SNAPSHOT                                                    *
 * Copyright (C) 1998-2018  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package jflex;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jflex.unicode.UnicodeProperties;

/**
 * This is the command-line interface.
 *
 * <p>It is responsible for parsing the commandline, getting input files, starting up the GUI if
 * necessary, etc. and invokes {@link LexGenerator} accordingly.
 *
 * @author Gerwin Klein
 * @author Régis Décamps
 * @version JFlex 1.7.1-SNAPSHOT
 */
public class Main {

  /**
   * parseOptions.
   *
   * @param argv an array of cli argument values.
   * @param options A {@link Options} builder.
   * @return a {@link java.util.List} object.
   * @throws SilentExit if any.
   */
  private static List<File> parseOptions(String argv[], Options.Builder options) throws SilentExit {
    List<File> files = new ArrayList<>();

    for (int i = 0; i < argv.length; i++) {

      if (Objects.equals(argv[i], "-d")
          || Objects.equals(argv[i], "--outdir")) { // $NON-NLS-1$ //$NON-NLS-2$
        if (++i >= argv.length) {
          throw new GeneratorException(ErrorMessages.NO_DIRECTORY);
        }
        options.setOutputDirectory(new File(argv[i]));
        continue;
      }

      if (Objects.equals(argv[i], "--skel")
          || Objects.equals(argv[i], "-skel")) { // $NON-NLS-1$ //$NON-NLS-2$
        if (++i >= argv.length) {
          throw new GeneratorException(ErrorMessages.NO_SKEL_FILE);
        }

        options.setSkeleton(new File(argv[i]));
        continue;
      }

      if (Objects.equals(argv[i], "--encoding")) {
        if (++i >= argv.length) {
          throw new GeneratorException(ErrorMessages.NO_ENCODING);
        }
        String encodingName = argv[i];
        try {
          options.setEncoding(encodingName);
        } catch (UnsupportedCharsetException e) {
          System.out.println(ErrorMessages.get(ErrorMessages.CHARSET_NOT_SUPPORTED, encodingName));
        }
        continue;
      }

      if (Objects.equals(argv[i], "-jlex")
          || Objects.equals(argv[i], "--jlex")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setStrictJlex(true);
        continue;
      }

      if (Objects.equals(argv[i], "-v")
          || Objects.equals(argv[i], "--verbose")
          || Objects.equals(argv[i], "-verbose")) { // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        options.setVerbose(true);
        options.setShowProgress(true);
        options.setUnusedWarnings(true);
        continue;
      }

      if (Objects.equals(argv[i], "-q")
          || Objects.equals(argv[i], "--quiet")
          || Objects.equals(argv[i], "-quiet")) { // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        options.setVerbose(false);
        options.setShowProgress(false);
        options.setUnusedWarnings(false);
        continue;
      }

      if (Objects.equals(argv[i], "--warn-unused")) { // $NON-NLS-1$
        options.setUnusedWarnings(true);
        continue;
      }

      if (Objects.equals(argv[i], "--no-warn-unused")) { // $NON-NLS-1$
        options.setUnusedWarnings(false);
        continue;
      }

      if (Objects.equals(argv[i], "--dump")
          || Objects.equals(argv[i], "-dump")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setDump(true);
        continue;
      }

      if (Objects.equals(argv[i], "--time")
          || Objects.equals(argv[i], "-time")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setTiming(true);
        continue;
      }

      if (Objects.equals(argv[i], "--version")
          || Objects.equals(argv[i], "-version")) { // $NON-NLS-1$ //$NON-NLS-2$
        System.out.println(ErrorMessages.get(ErrorMessages.THIS_IS_JFLEX, LexGenerator.VERSION));
        throw new SilentExit(0);
      }

      if (Objects.equals(argv[i], "--dot")
          || Objects.equals(argv[i], "-dot")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setGenerateDotFile(true);
        continue;
      }

      if (Objects.equals(argv[i], "--help")
          || Objects.equals(argv[i], "-h")
          || Objects.equals(argv[i], "/h")) { // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        printUsage();
        throw new SilentExit(0);
      }

      if (Objects.equals(argv[i], "--info")
          || Objects.equals(argv[i], "-info")) { // $NON-NLS-1$ //$NON-NLS-2$
        // TODO(regisd) Out.printSystemInfo();
        throw new SilentExit(0);
      }

      if (Objects.equals(argv[i], "--nomin")
          || Objects.equals(argv[i], "-nomin")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setMinimize(false);
        continue;
      }

      if (Objects.equals(argv[i], "--pack")
          || Objects.equals(argv[i], "-pack")) { // $NON-NLS-1$ //$NON-NLS-2$
        /* no-op - pack is the only generation method */
        continue;
      }

      if (Objects.equals(argv[i], "--nobak")
          || Objects.equals(argv[i], "-nobak")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setBackup(false);
        continue;
      }

      if (Objects.equals(argv[i], "--legacydot")
          || Objects.equals(argv[i], "-legacydot")) { // $NON-NLS-1$ //$NON-NLS-2$
        options.setLegacyDot(true);
        continue;
      }

      if (Objects.equals(argv[i], "--uniprops")
          || Objects.equals(argv[i], "-uniprops")) { // $NON-NLS-1$ //$NON-NLS-2$
        if (++i >= argv.length) {
          throw new GeneratorException(
              ErrorMessages.PROPS_ARG_REQUIRES_UNICODE_VERSION, UnicodeProperties.UNICODE_VERSIONS);
        }
        String unicodeVersion = argv[i];
        printUnicodeProperties(unicodeVersion, new Out(options.build()));
        throw new SilentExit(); // TODO(regisd): Shouldn't it be exit 0?
      }

      if (argv[i].startsWith("-")) { // $NON-NLS-1$
        printUsage();
        throw new SilentExit(ErrorMessages.UNKNOWN_COMMANDLINE, argv[i]);
      }

      files.add(new File(argv[i]));
    }

    return files;
  }

  /** JFlex version */
  public static final String version = "1.7.0-SNAPSHOT"; // $NON-NLS-1$

  private static void printUnicodeProperties(String unicodeVersion, Out out) {
    try {
      printUnicodePropertyValuesAndAliases(unicodeVersion, out);
    } catch (UnicodeProperties.UnsupportedUnicodeVersionException e) {
      throw new GeneratorException(
          ErrorMessages.UNSUPPORTED_UNICODE_VERSION_SUPPORTED_ARE,
          UnicodeProperties.UNICODE_VERSIONS);
    }
  }

  /**
   * Prints one Unicode property value per line, along with its aliases, if any, for the given
   * unicodeVersion.
   *
   * @param unicodeVersion The Unicode version to print property values and aliases for
   * @throws UnicodeProperties.UnsupportedUnicodeVersionException if unicodeVersion is not supported
   */
  private static void printUnicodePropertyValuesAndAliases(String unicodeVersion, Out log)
      throws UnicodeProperties.UnsupportedUnicodeVersionException {
    Pattern versionPattern = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.\\d+)?");
    Matcher matcher = versionPattern.matcher(unicodeVersion);
    if (!matcher.matches()) {
      throw new UnicodeProperties.UnsupportedUnicodeVersionException();
    }
    String underscoreVersion =
        matcher.group(1) + (null == matcher.group(2) ? "_0" : "_" + matcher.group(2));

    String[] propertyValues;
    String[] propertyValueAliases;
    try {
      Class<?> clazz = Class.forName("jflex.unicode.data.Unicode_" + underscoreVersion);
      Field field = clazz.getField("propertyValues");
      propertyValues = (String[]) field.get(null);
      field = clazz.getField("propertyValueAliases");
      propertyValueAliases = (String[]) field.get(null);
    } catch (Exception e) {
      throw new UnicodeProperties.UnsupportedUnicodeVersionException();
    }
    SortedMap<String, SortedSet<String>> propertyValuesToAliases = new TreeMap<>();
    for (String value : propertyValues) {
      propertyValuesToAliases.put(value, new TreeSet<String>());
    }
    for (int i = 0; i < propertyValueAliases.length - 1; i += 2) {
      String alias = propertyValueAliases[i];
      String value = propertyValueAliases[i + 1];
      SortedSet<String> aliases = propertyValuesToAliases.get(value);
      if (null == aliases) {
        aliases = new TreeSet<>();
        propertyValuesToAliases.put(value, aliases);
      }
      aliases.add(alias);
    }
    for (Map.Entry<String, SortedSet<String>> entry : propertyValuesToAliases.entrySet()) {
      String value = entry.getKey();
      SortedSet<String> aliases = entry.getValue();
      log.print(value);
      if (aliases.size() > 0) {
        for (String alias : aliases) {
          log.print(", " + alias);
        }
      }
      log.println("");
    }
  }

  /** Prints the cli usage on stdout. */
  public static void printUsage() {
    System.out.println(""); // $NON-NLS-1$
    System.out.println("Usage: jflex <options> <input-files>");
    System.out.println("");
    System.out.println("Where <options> can be one or more of");
    System.out.println("-d <directory>    write generated file to <directory>");
    System.out.println("--skel <file>     use external skeleton <file>");
    System.out.println("--encoding <name> use <name> as input/output encoding");
    System.out.println("--pack            set default code generation method (default)");
    System.out.println("--jlex            strict JLex compatibility");
    System.out.println("--legacydot       dot (.) metachar matches [^\\n] instead of");
    System.out.println("                  [^\\n\\r\\u000B\\u000C\\u0085\\u2028\\u2029]");
    System.out.println("--nomin           skip minimization step");
    System.out.println("--nobak           don't create backup files");
    System.out.println("--dump            display transition tables");
    System.out.println(
        "--dot             write graphviz .dot files for the generated automata (alpha)");
    System.out.println("--verbose");
    System.out.println("-v                display generation progress messages (default)");
    System.out.println("--quiet");
    System.out.println("-q                display errors only");
    System.out.println("--time            display generation time printStatistics");
    System.out.println("--version         print the version number of this copy of jflex");
    System.out.println("--info            print system + JDK information");
    System.out.println(
        "--uniprops <ver>  print all supported properties for Unicode version <ver>");
    System.out.println("--help");
    System.out.println("-h                print this message");
    System.out.println("");
    System.out.println(ErrorMessages.get(ErrorMessages.THIS_IS_JFLEX, version));
    System.out.println("Have a nice day!");
  }

  private static void generate(String[] argv) throws SilentExit, GeneratorException {
    Options.Builder opts = Options.builder();
    List<File> files = parseOptions(argv, opts);
    Options options = opts.build();

    if (options.verbose()) {
      System.out.println("JFlex " + options);
    }
    if (files.isEmpty()) {
      // No file was provided. Start GUI.
      // TODO(regisd): new MainFrame(options.buildUpon());
    } else {
      for (File file : files) {
        LexGenerator lexGenerator = new LexGenerator(options);
        try {
          lexGenerator.generateFromFile(file);
        } catch (GeneratorException e) {
          lexGenerator.printStatistics();
          throw e;
        }
      }
    }
  }

  /**
   * Starts the generation process with the files in {@code argv} or pops up a window to choose a
   * file, when {@code argv} doesn't have any file entries.
   *
   * @param argv the commandline argument values.
   */
  public static void main(String argv[]) {
    try {
      generate(argv);
    } catch (SilentExit e) {
      if (e.getCause() != null) {
        System.err.println(e.getCause().getLocalizedMessage());
      }
      System.exit(e.exitCode());
    } catch (GeneratorException e) {
      // statistics printed on Out.
      // Even if we don't print the fulls tack, an error message can always help
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  // Only CLI, not meant for instanciation.
  private Main() {}
}
