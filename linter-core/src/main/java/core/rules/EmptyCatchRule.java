package core.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import core.Violation;
import enums.Severity;

import java.util.ArrayList;
import java.util.List;

public class EmptyCatchRule implements Rule {

    @Override
    public List<Violation> check(CompilationUnit ast) {
        List<Violation> violations = new ArrayList<>();

        // first get the name of the variable, and body of the catch clause
        var catchClause = ast.findAll(CatchClause.class);

        for (var catches : catchClause) {
            String variableName = catches.getParameter().getNameAsString();
            // true if the catch clause has a body, false if it is empty
            boolean catchBodyIsEmpty = catches.getBody().getStatements().isEmpty();

            // first check if the variable is ignored
            if (variableName.equalsIgnoreCase("ignore") || variableName.equalsIgnoreCase("ignored")) {
                // nO VIOLATIONS ACCCORDING TO THE RULE SET
                System.out.println("Catch clause variable is ignored, no violations found.");
            }
            else if (catchBodyIsEmpty){
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
        return violations;
    }
}
