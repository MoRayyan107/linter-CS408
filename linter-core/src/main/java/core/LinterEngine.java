package core;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import enums.Severity;

import java.util.ArrayList;
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
     * Analyzes the code and returns a list of violations found.
     *
     * @param input The Java source code as a string.
     * @return list of violations found.
     */
    public List<Violation> analyse(String input) {
        // returns a non-null object of type ParseResult<CompilationUnit> (AST)

        ParseResult<CompilationUnit> parsedRes = parser.parse(input);
        List<Violation> violations = new ArrayList<>();

        // check for catch clauses
        checkCatchClauses(parsedRes, violations);

        return violations;
    }

    private void checkCatchClauses(ParseResult<CompilationUnit> parsed_ast, List<Violation> violations) {
        // check if the result is successful
        if (!parsed_ast.isSuccessful() || parsed_ast.getResult().isEmpty()) {
            System.err.println("Parsing failed with problems: " + parsed_ast.getProblems());
            return;
        }

        parsed_ast.getResult().ifPresent(ast -> {
            // first get the name of the variable, and body of the catch clause
            var catchClause = ast.findAll(CatchClause.class);

            for (var catches : catchClause) {
                String variableName = catches.getParameter().getNameAsString();
                // true if the catch clause has a body, false if it is empty
                boolean catchBody = catches.getBody().getStatements().isNonEmpty();

                // first check if the variable is ignored
                if (variableName.equalsIgnoreCase("ignore") || variableName.equalsIgnoreCase("ignored")) {
                    // nO VIOLATIONS ACCCORDING TO THE RULE SET
                    System.out.println("Catch clause variable is ignored, no violations found.");
                }
                else if (!catchBody){
                    // if the body is empty, then we have a violation
                    Violation violation = Violation.buildViolation(
                            "CATCH_CLAUSE_EMPTY_BODY",
                            "Catch clause has an empty body.",
                            Severity.WARNING,
                            catches.getBegin().map(p -> p.line).orElse(-1),
                            catches.getBegin().map(p -> p.column).orElse(-1),
                            "Add some code to the catch clause body."
                    );
                    violations.add(violation);
                } else {
                    // body exists and no variable is named as ignore, so no violations found
                    System.out.println("Catch clause has a body and variable is not ignored, no violations found.");
                }

            }
        });
    }
}