package core.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.CatchClause;
import core.Violation;
import enums.Severity;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        // no violation is done
        log.info("Catch clause variable is ignored, no violations found.");
        continue;
      } else if (!catchBodyIsEmpty) {
        // body exists and no variable is named as ignore, so no violations found
        log.info("Catch clause has a body and variable is not ignored, no violations found.");
        continue;
      }

      // create a new Violation
      Violation violation =
              new Violation(
                      "CATCH_CLAUSE_EMPTY_BODY",
                      "Catch clause has an empty body.",
                      Severity.WARNING,
                      catches.getBegin().map(p -> p.line).orElse(-1),
                      catches.getBegin().map(p -> p.column).orElse(-1),
                      "Add some code to the catch clause body.");
      violations.add(violation);
    }
    return violations;
  }
}
