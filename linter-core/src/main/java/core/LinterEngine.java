package core;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import core.rules.EmptyCatchRule;
import core.rules.Rule;
import java.util.ArrayList;
import java.util.List;

/***
 * LinterEngine, this cclass contains teh main logicc for linter engine
 */
public class LinterEngine {

  private final Parser parser;
  private final Rule catchClauseRule;

  public LinterEngine() {
    this.parser = new Parser();
    this.catchClauseRule = new EmptyCatchRule();
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
            });

    return violations;
  }
}
