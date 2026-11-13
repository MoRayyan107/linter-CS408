package core;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import core.rules.*;
import java.util.ArrayList;
import java.util.List;

/***
 * LinterEngine, this cclass contains teh main logicc for linter engine
 */
public class LinterEngine {

  private final Parser parser;
  private final Rule catchClauseRule;
  private final Rule TransactionalSelfInvocationRule;

  public LinterEngine() {
    this.parser = new Parser();
    this.catchClauseRule = new EmptyCatchRule();
    this.TransactionalSelfInvocationRule = new TransactionalSelfInvocationRule();
  }

  /**
   * Analyzes the code and returns a list of violations found.
   *
   * @param input The Java source code as a string.
   * @return list of violations found. (a non-null object)
   */

  // TODO: Users can select which rule they can use so, there should be a way to mark which is selected and
  //  which is not and then pass through the selected rules and then add to violations
  public List<Violation> analyse(String input) {

    ParseResult<CompilationUnit> parsedRes = parser.parse(input);
    List<Violation> violations = new ArrayList<>();

    // check if teh the parsed result is successful
    if (!parsedRes.isSuccessful() || parsedRes.getResult().isEmpty()) {
      System.err.println("Parsing failed with problems: " + parsedRes.getProblems());
      return violations;
    }

    parsedRes
        .getResult()
        .ifPresent(
            ast -> {
              // call for catchClauses check
              violations.addAll(catchClauseRule.check(ast));
              // call for TransactionalSelfInvocationRule check
              violations.addAll(TransactionalSelfInvocationRule.check(ast));
            });

    return violations;
  }
}
