import static org.junit.jupiter.api.Assertions.assertEquals;

import core.LinterEngine;
import core.Violation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LinterEngineTest {

  private final LinterEngine linterEngine = new LinterEngine();

  @Nested
  class CatchClauseTests {
    // TODO: in future, once more rules are added the test cases needs to extract ONLY FOR CATCH
    // CLAUSE RULES,
    // and not all rules, so that the test cases are more focused and easier to maintain.
    private String sourceCode;
    private static final String VIOLATION_RULE_ID = "CATCH_CLAUSE_EMPTY_BODY";

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
                    }""";
    }

    @Test
    void test_linterEngine_reportsOneViolationForEmptyCatchBody() {
      // Test case where the catch variable is not ignored and the catch block is empty
      var violations = linterEngine.analyse(sourceCode);

      // should have a violation since there's no "ignore" or "ignored" variable name and the catch
      // block is empty
      assertEquals(1, violations.size());
      assertEquals(5, violations.getFirst().getLine());
      assertEquals(VIOLATION_RULE_ID, violations.getFirst().getRuleId());
    }

    @Test
    void test_linterEngine_reportsOneViolationForWhitespaceOnlyCatchBody() {
      // Test case where the catch variable is not ignored and the catch block has only whitespace
      sourceCode =
          sourceCode.replace("// this is a comment (this will be updated each test)", "       ");
      var violations = linterEngine.analyse(sourceCode);

      // should have a violation since there's no "ignore" or "ignored" variable name and the catch
      // block is effectively empty
      assertEquals(1, violations.size());
      assertEquals(VIOLATION_RULE_ID, violations.getFirst().getRuleId());
    }

    @Test
    void test_LinterEngine_allowsEmptyCatchWithIgnoreOrIgnoredName() {
      // First, test case where the catch variable is "ignore" and the catch block is empty
      sourceCode = sourceCode.replace("Exception e", "Exception ignore");
      var firstCaseViolation = linterEngine.analyse(sourceCode);

      // second, test case where the catch variable is "ignored" and the catch block is empty
      sourceCode = sourceCode.replace("Exception ignore", "Exception ignored");
      var secondCaseViolation = linterEngine.analyse(sourceCode);

      // should have no violations since the variable name is "ignore"
      assertEquals(0, firstCaseViolation.size());
      // should have no violations since the variable name is "ignored"
      assertEquals(0, secondCaseViolation.size());
    }

    @Test
    void test_linterEngine_allowsEmptyCatchWithMixedCaseIgnoreName() {
      // Test case where the catch variable is "ignore" and the catch block has some code
      sourceCode = sourceCode.replace("Exception e", "Exception IgNoRe");
      var violations = linterEngine.analyse(sourceCode);

      // should have no violations since the variable name is "ignore" and the catch block has code
      assertEquals(0, violations.size());
    }

    @Test
    void test_LinterEngine_reportsNoViolationForCatchWithBodyContainingCode() {
      // Test case where the catch variable is not ignored and the catch block has some code
      sourceCode =
          sourceCode.replace(
              "// this is a comment (this will be updated each test)",
              "System.out.println(e.getMessage());");
      var violations = linterEngine.analyse(sourceCode);

      // should have no violations since the catch block has code
      assertEquals(0, violations.size());
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
      var violations = linterEngine.analyse(sourceCode);

      // should have two violations for the first two empty catch blocks
      assertEquals(2, violations.size());
      assertEquals(VIOLATION_RULE_ID, violations.get(0).getRuleId());
      assertEquals(VIOLATION_RULE_ID, violations.get(1).getRuleId());
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
      var violations = linterEngine.analyse(sourceCode);

      // should have no violations since there are no catch clauses
      assertEquals(0, violations.size());
    }
  }
}
