package core;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import enums.Severity;

import java.util.List;

/***
 * LinterEngine, this cclass contains teh main logicc for linter engine
 */
public class LinterEngine {

    private final Parser parser;

    public LinterEngine() {
        this.parser = new Parser();
    }

    /**
     * Analyzes the code and returns a string describing the analysis.
     * @return A string describing the analysis.
     */
    public List<Violation> analyse(String input) {
        // Implement the logic for analyzing code here
        // For now, we will return a dummy list of violations
        ParseResult<CompilationUnit> parsedRes = parser.parse(input);

        // TODO: THIS IS A PART OF EXAMPLE THIS CODE WILL BE PUSHED FOR REF SNAPSHOT
        // PRINTING THE JAVA SOURCE FILE, NOT AN AST, FOR DEBUGGING PURPOSES
        if (parsedRes.isSuccessful() && parsedRes.getResult().isPresent()) {
            // print the AST for debugging purposes
            parsedRes.getResult().ifPresent(compilationUnit -> {
                System.out.println("Parsed AST: " + compilationUnit.toString());

                var catchBlock = compilationUnit.findAll(CatchClause.class); // finds any ccatcch clases

                for (var catches : catchBlock) {

                    if (!catches.getBody().getStatements().isEmpty()) {
                        System.out.println("Catch block iis : " + catches.getBody().getStatements().toString());
                    } else if (catches.getParameter().getNameAsString().equalsIgnoreCase("ignore") ||
                            catches.getParameter().getNameAsString().equalsIgnoreCase("ignored"))
                    {
                        System.out.println("Catch block is ignored");
                    }
                    else {
                        System.out.println("Potential empty-catch violation detected at line: " + catches.getBegin().map(pos -> pos.line).orElse(-1));
                    }
                }
            });
        } else {
            // if any problem or issues occurred during parsing, print the problems
            parsedRes.getProblems().forEach(problem -> {
                System.err.println("Parsing problem: " + problem.getMessage());
            });
        }

        return List.of(
            new Violation("RULE001", "This is a warning message.", Severity.WARNING, 10, 5, "Consider refactoring this code."),
            new Violation("RULE002", "This is an error message.", Severity.ERROR, 20, 15, "This code is not valid."),
            new Violation("RULE003", "This is an info message.", Severity.INFO, 30, 25, "This is just for your information.")
        );
    }
}