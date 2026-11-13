package core.rules;

import static core.Constants.CATCH_CLAUSE_RULE_ID;

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

    return checkCatchClause(violations, ast);
  }

  /**
   * This check is for the catch clause rule, where it checks if the catch clause has an empty body and if the variable name is not ignored
   *
   * @param violations a list that will be added to if a violation is found
   * @param ast the source code tree (AST) that will be worked through to find the catch clauses
   * @return a list of {@link Violation} objects representing the violations found in the catch clauses
   */
  private List<Violation> checkCatchClause(List<Violation> violations, CompilationUnit ast) {
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
      violations.add(createCatchClauseViolation(catches));
    }
    return violations;
  }

  /**
   * This method creates a Violation object for an empty catch clause.
   *
   * @param catches the catch clause that is being checked for violations
   * @return a {@link Violation} object representing the empty catch clause violation
   */
  private Violation createCatchClauseViolation(CatchClause catches) {
    return new Violation(
        CATCH_CLAUSE_RULE_ID,
        "Catch clause has an empty body.",
        Severity.WARNING,
        catches.getBegin().map(p -> p.line).orElse(-1),
        catches.getBegin().map(p -> p.column).orElse(-1),
        "Add some code to the catch clause body.");
  }
}
