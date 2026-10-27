package cli;

import core.LinterEngine;
/***
 * LinterCli, this class contains the main entry point for the command-line interface of the linter
 * 
 */
public class LinterCli {



  public static void main(String[] args) {
    // Create an instance of the LinterEngine
    LinterEngine linterEngine = new LinterEngine();

    // Call the run method of the LinterEngine to start the linter
    System.out.println(linterEngine.analyse("Some file Path or Code Input"));
  }

}
