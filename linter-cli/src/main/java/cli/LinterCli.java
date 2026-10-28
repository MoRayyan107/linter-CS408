package cli;

import core.LinterEngine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/***
 * LinterCli, this class contains the main entry point for the command-line interface of the linter
 * 
 */
public class LinterCli {

  public static void main(String[] args) {
    // get the file based on the struct "java -jar linter-cli.jar Example.java"
    if (args.length == 0) {
      System.err.println("Usage: java -jar linter-cli.jar <file.java>");
      System.exit(1);
    }

    Path path = Paths.get(args[0]);

    // DEBUG: Print the path to the file being analyzed
//    System.out.println("Analyzing file: " + path);

    String sourceCode = null;
    try{
      // if file does not end with .java, print error and exit
      if (!path.toString().endsWith(".java")) {
        System.err.println("File is not a Java file: " + path);
        System.exit(1);
      }

      // check if file exists, if not print error and exit
      if (!Files.exists(path)) {
        System.err.println("File not found: " + path);
        System.exit(1);
      }


      sourceCode = Files.readString(path);

      // DEBUG: Print the first 100 characters of the source code
//      System.out.println("Source code (first 100 chars): " + sourceCode.substring(0, Math.min(100, sourceCode.length())));

    } catch (Exception e) {
      System.err.println("Error checking file existence: " + e.getMessage());
      System.exit(1);
    }
    // Create an instance of the LinterEngine
    LinterEngine linterEngine = new LinterEngine();

    // Call the run method of the LinterEngine to start the linter
    System.out.println(linterEngine.analyse(sourceCode));
  }

}
