package core;

import enums.Severity;

import java.util.List;

/***
 * LinterEngine, this cclass contains teh main logicc for linter engine
 */
public class LinterEngine {

    public LinterEngine() {}

    /**
     * Analyzes the code and returns a string describing the analysis.
     * @return A string describing the analysis.
     */
    public List<Violation> analyse(String input) {
        // Implement the logic for analyzing code here
        // For now, we will return a dummy list of violations
        return List.of(
            new Violation("RULE001", "This is a warning message.", Severity.WARNING, 10, 5, "Consider refactoring this code."),
            new Violation("RULE002", "This is an error message.", Severity.ERROR, 20, 15, "This code is not valid."),
            new Violation("RULE003", "This is an info message.", Severity.INFO, 30, 25, "This is just for your information.")
        );
    }
}