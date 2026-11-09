import static core.Constants.CATCH_CLAUSE_RULE_ID;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import core.Parser;
import core.Violation;
import core.rules.EmptyCatchRule;
import core.rules.Rule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LinterEngineTest {

  private final Parser parser = new Parser();

  @Nested
  class CatchClauseTests {
    // TODO: in future, once more rules are added the test cases needs to extract ONLY FOR CATCH
    // CLAUSE RULES,
    // and not all rules, so that the test cases are more focused and easier to maintain.
    private String sourceCode;
    private final EmptyCatchRule empCatch = new EmptyCatchRule();

    @BeforeEach
    public void setUp() {
      sourceCode =
          """
          public class Test {
            void testMethod() {
              try {
                Integer.parseInt("abc");
              } catch (Exception e) {
                // this is a comment (this will be updated each test)
              }
            }
          }
          """;
    }

    @Test
    void test_linterEngine_reportsOneViolationForEmptyCatchBody() {
      // Test case where the catch variable is not ignored and the catch block is empty
      var violations = analyseWithRule(empCatch, sourceCode);

      // should have a violation since there's no "ignore" or "ignored" variable name and the catch
      // block is empty
      assertEquals(1, violations.size());
      assertEquals(5, violations.getFirst().getLine());
      assertViolation(violations, CATCH_CLAUSE_RULE_ID);
    }

    @Test
    void test_linterEngine_reportsOneViolationForWhitespaceOnlyCatchBody() {
      // Test case where the catch variable is not ignored and the catch block has only whitespace
      sourceCode =
          sourceCode.replace("// this is a comment (this will be updated each test)", "       ");
      var violations = analyseWithRule(empCatch, sourceCode);

      // should have a violation since there's no "ignore" or "ignored" variable name and the catch
      // block is effectively empty
      assertEquals(1, violations.size());
      assertViolation(violations, CATCH_CLAUSE_RULE_ID);
    }

    @Test
    void test_LinterEngine_allowsEmptyCatchWithIgnoreOrIgnoredName() {
      // First, test case where the catch variable is "ignore" and the catch block is empty
      sourceCode = sourceCode.replace("Exception e", "Exception ignore");
      var firstCaseViolation = analyseWithRule(empCatch, sourceCode);

      // second, test case where the catch variable is "ignored" and the catch block is empty
      sourceCode = sourceCode.replace("Exception ignore", "Exception ignored");
      var secondCaseViolation = analyseWithRule(empCatch, sourceCode);

      // should have no violations since the variable name is "ignore"
      assertEquals(0, firstCaseViolation.size());
      assertNoViolation(firstCaseViolation);
      // should have no violations since the variable name is "ignored"
      assertEquals(0, secondCaseViolation.size());
      assertNoViolation(secondCaseViolation);
    }

    @Test
    void test_linterEngine_allowsEmptyCatchWithMixedCaseIgnoreName() {
      // Test case where the catch variable is "ignore" and the catch block has some code
      sourceCode = sourceCode.replace("Exception e", "Exception IgNoRe");
      var violations = analyseWithRule(empCatch, sourceCode);

      // should have no violations since the variable name is "ignore" and the catch block has code
      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_LinterEngine_reportsNoViolationForCatchWithBodyContainingCode() {
      // Test case where the catch variable is not ignored and the catch block has some code
      sourceCode =
          sourceCode.replace(
              "// this is a comment (this will be updated each test)",
              "System.out.println(e.getMessage());");
      var violations = analyseWithRule(empCatch, sourceCode);

      // should have no violations since the catch block has code
      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_LinterEngine_reportsTwoViolationsForMultipleEmptyCatchClauses() {
      // Test case with multiple catch clauses, some empty and some with code
      sourceCode =
          """
            public class Test {
              void testMethod() {
                try {
                  Integer.parseInt("abc");
                } catch (Exception e) {
                  // this is a comment (this will be updated each test)
                } catch (IOException io) {
                  // another comment
                } catch (RuntimeException re) {
                  System.out.println(re.getMessage());
                }
              }
            }
            """;
      var violations = analyseWithRule(empCatch, sourceCode);

      // should have two violations for the first two empty catch blocks
      assertEquals(2, violations.size());
      assertViolation(violations, CATCH_CLAUSE_RULE_ID);
      assertEquals(List.of(5, 7), violations.stream().map(Violation::getLine).sorted().toList());
    }

    @Test
    void test_LinterEngine_reportsNoViolationForNoCatchClauses() {
      // Test case with no catch clauses
      sourceCode =
          """
                        public class Test {
                            void testMethod() {
                                System.out.println("No try-catch here!");
                            }
                        }
                    """;
      var violations = analyseWithRule(empCatch, sourceCode);

      // should have no violations since there are no catch clauses
      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }
  }

  private void assertViolation(List<Violation> violations, String expectedViolation) {
    boolean isViolation =
        violations.stream().anyMatch(v -> v.getRuleId().equals(expectedViolation));
    assertTrue(isViolation);
  }

  private void assertNoViolation(List<Violation> violations) {
    assertEquals(List.of(), getRuleId(violations));
  }

  private List<String> getRuleId(List<Violation> violations) {
    return violations.stream().map(Violation::getRuleId).toList();
  }

  private List<Violation> analyseWithRule(Rule ruleUsage, String sourceCode) {
    var compilationUnit = parser.parse(sourceCode).getResult().orElseThrow();
    return ruleUsage.check(compilationUnit);
  }
}
