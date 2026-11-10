package cli;

import core.LinterEngine;
import core.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/***
 * LinterCli, this class contains the main entry point for the command-line interface of the linter
 */
public class LinterCli {

  public static void main(String[] args) {

    if (args.length == 0) {
      System.err.println("Not a Valid command, use --help for more information");
      return; // exit the program without killing the thread
    }

    // if given is --help
    if (args[0].equals("--help")) {
      printHelpCommand();
      return; // exit the program without killing the thread
    }

    // ccheck if the args have a java file or not
    boolean hasJavaFile = Arrays.stream(args).anyMatch(arg -> arg.contains(".java"));
    if (hasJavaFile) runEngine(args);
  }

  private static void printHelpCommand() {
    String printHelp =
        """
        Usage: java -jar linter-cli.jar <file.java>
        Options:
          --help      Show this help message
        """;
    System.out.println(printHelp);
  }

  private static void runEngine(String[] args) {
    if (args.length == 0) {
      System.err.println("Usage: java -jar linter-cli.jar <file.java>");
      return; // exit the program without killing the thread
    }

    Path path = Paths.get(args[0]);

    // DEBUG: Print the path to the file being analyzed
    //    System.out.println("Analyzing file: " + path);

    String sourceCode = null;
    try {
      // if file does not end with .java, print error and exit
      if (!path.toString().endsWith(".java")) {
        System.err.println("File is not a Java file: " + path);
        return; // exit the program without killing the thread
      }

      // check if file exists, if not print error and exit
      if (!Files.exists(path)) {
        System.err.println("File not found: " + path);
        return;
      }

      sourceCode = Files.readString(path);

      // DEBUG: Print the first 100 characters of the source code
      //      System.out.println("Source code (first 100 chars): " + sourceCode.substring(0,
      // Math.min(100, sourceCode.length())));

    } catch (Exception e) {
      System.err.println("Error checking file existence: " + e.getMessage());
      return;
    }
    // Create an instance of the LinterEngine
    // TODO: later make this loose coupled by using a factory pattern to create the LinterEngine
    LinterEngine linterEngine = new LinterEngine();

    // Call the run method of the LinterEngine to start the linter
    List<Violation> violations = linterEngine.analyse(sourceCode);
    System.out.println("Violations found: " + violations.size());
    for (Violation violation : violations) System.out.println(violation.toString());
  }
}
