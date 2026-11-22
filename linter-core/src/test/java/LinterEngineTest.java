import static core.Constants.CATCH_CLAUSE_RULE_ID;
import static core.Constants.TRANSACTIONAL_SELF_INVOCATION_RULE_ID;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import core.Parser;
import core.Violation;
import core.rules.EmptyCatchRule;
import core.rules.Rule;
import core.rules.TransactionalSelfInvocationRule;
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

  @Nested
  class TransactionalSelfInvocationTests {

    private String sourceCode;
    private String nestedClassCode;
    private final TransactionalSelfInvocationRule rule = new TransactionalSelfInvocationRule();

    @BeforeEach
    public void setUp() {
      sourceCode =
          """
              // IMPORTS
              public class OrderService {
                void process() {
                  validate();
                }
                // ANNOTATION
                void validate() {}

              }
              // EXTRA_TYPES
              """;

      nestedClassCode =
          """
                  import org.springframework.transaction.annotation.*;
                  public class OuterClass {

                    // OUTER ANNOTATION
                    public void foo() {
                      // ANY CALLS
                    }

                    class InnerClass {
                      public void bar() {
                        // ADD INNER CODE
                      }

                      // ADD INNER METHODS
                    }
                  }
              """;
    }

    @Test
    void test_rule_reportsSelfCallToSpringTransactional() {
      sourceCode =
          sourceCode
              .replace(
                  "// IMPORTS", "import org.springframework.transaction.annotation.Transactional;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(1, violations.size());
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(4, violations.getFirst().getLine());
    }

    @Test
    void test_rule_reportsSelfCallToJakartaTransactional() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import jakarta.transaction.Transactional;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(1, violations.size());
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(4, violations.getFirst().getLine());
    }

    @Test
    void test_rule_reportsSelfCallToFullyQualifiedSpringTransactional() {
      sourceCode =
          sourceCode.replace(
              "// ANNOTATION", "@org.springframework.transaction.annotation.Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(1, violations.size());
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(4, violations.getFirst().getLine());
    }

    @Test
    void test_rule_reportsSelfCallToTransactionalViaSpringWildcardImport() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import org.springframework.transaction.annotation.*;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(1, violations.size());
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(4, violations.getFirst().getLine());
    }

    @Test
    void test_rule_reportsSelfCallToTransactionalViaJakartaWildcardImport() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import jakarta.transaction.*;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(1, violations.size());
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(4, violations.getFirst().getLine());
    }

    @Test
    void test_rule_ignoresSelfCallToDeprecatedMethodWhenSpringImported() {
      sourceCode =
          sourceCode
              .replace(
                  "// IMPORTS", "import org.springframework.transaction.annotation.Transactional;")
              .replace("// ANNOTATION", "@Deprecated");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallToTransactionalWithoutImport() {
      sourceCode = sourceCode.replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallToCustomTransactional() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import com.mycompany.Transactional;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallWhenCustomSingleTypeImportBeatsSpringWildcard() {
      sourceCode =
          sourceCode
              .replace(
                  "// IMPORTS",
                  "import com.mycompany.Transactional; "
                      + "import org.springframework.transaction.annotation.*;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallWhenFileDeclaresItsOwnTransactionalType() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import org.springframework.transaction.annotation.*;")
              .replace("// ANNOTATION", "@Transactional")
              .replace("// EXTRA_TYPES", "@interface Transactional {}");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallToTransactionalFromStaticImport() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import static com.mycompany.Util.Transactional;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallToFullyQualifiedUnsupportedTransactional() {
      sourceCode = sourceCode.replace("// ANNOTATION", "@com.mycompany.Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresSelfCallToJavaxTransactional() {
      sourceCode =
          sourceCode
              .replace("// IMPORTS", "import javax.transaction.Transactional;")
              .replace("// ANNOTATION", "@Transactional");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_ignoresTransactionalMethodWhenThereIsNoSelfCall() {
      sourceCode =
          sourceCode
              .replace(
                  "// IMPORTS", "import org.springframework.transaction.annotation.Transactional;")
              .replace("// ANNOTATION", "@Transactional")
              .replace("validate();", "");
      var violations = analyseWithRule(rule, sourceCode);

      assertEquals(0, violations.size());
      assertNoViolation(violations);
    }

    // TODO: fix this test case, FAILED ON PURPOSE, but should be allowed
    @Test
    void test_rule_reportViolationForOverloadedMethods() {
      int expectedViolationSize = 1;
      String overloadedCode =
          """
              import org.springframework.transaction.annotation.*;
              public class OrderService {

                public void process() {
                  validate(23);    // Should NOT Violate
                  validate("abc"); // Should Violate
                }

                @Transactional
                public void validate(String ref) {}

                public void validate(int refNum) {}
              }
              """;

      var violations = analyseWithRule(rule, overloadedCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(expectedViolationSize, violations.size());
    }

    // TODO: fix this test case, FAILED ON PURPOSE, but should be allowed
    @Test
    void test_rule_ignoresThisCallInsideAnonymousClass() {
      int expectedViolationSize = 0;
      String anonymousClassCode =
          """
              import org.springframework.transaction.annotation.*;
              public class OrderService {

                public void process() {
                  Runnable r = new Runnable() {
                    @Override
                    public void run() {
                      validate(); // Should NOT Violate
                    }

                    private void validate() {
                        System.out.println("Inside anonymous class");
                    }
                  };
                  r.run();
                }

                @Transactional
                public void validate() {}
              }
              """;

      var violations = analyseWithRule(rule, anonymousClassCode);
      assertEquals(expectedViolationSize, violations.size());
      assertNoViolation(violations);
    }

    @Test
    void test_rule_reportsOuterThisCallFromInnerClass() {
      int violationsExpected = 1;
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace("// ADD INNER CODE", "OuterClass.this.foo(); // Should Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(violationsExpected, violations.size());
    }

    @Test
    void test_innerHasNonTransactionalFoo_outerTransactional_flags() {
      int violationsExpected = 1;
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace("// ADD INNER CODE", "OuterClass.this.foo(); // Should Violate")
              .replace("// ADD INNER METHODS", "public void foo() {} ");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(violationsExpected, violations.size());
    }

    // TODO: FAILED ON PURPOSE until Outer.this resolves to the named class
    @Test
    void test_innerHasTransactionalFoo_outerNotTransactional_noFlag() {
      nestedClassCode =
          nestedClassCode
              .replace("// ADD INNER CODE", "OuterClass.this.foo(); // Should NOT Violate")
              .replace("// ADD INNER METHODS", "@Transactional public void foo() {}");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertNoViolation(violations);
    }

    // TODO: FAILED ON PURPOSE until Outer.this resolves to the named class
    @Test
    void test_lambdaInInner_outerThis_flags() {
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace(
                  "// ADD INNER CODE",
                  "Runnable r = () -> OuterClass.this.foo(); // Should Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(1, violations.size());
    }

    // TODO: FAILED ON PURPOSE until Outer.this resolves to the named class
    @Test
    void test_anonymousClassInInner_outerThis_flags() {
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace(
                  "// ADD INNER CODE",
                  "new Runnable() { public void run() { OuterClass.this.foo(); } }; // Should"
                      + " Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(1, violations.size());
    }

    // TODO: FAILED ON PURPOSE until Outer.this resolves to the named class
    @Test
    void test_localClass_outerThis_flags() {
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace(
                  "// ANY CALLS",
                  "class Local { void run() { OuterClass.this.foo(); } } // Should Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(1, violations.size());
    }

    // TODO: FAILED ON PURPOSE until Outer.this resolves to the named class
    @Test
    void test_twoLevelNesting_outerThis_flags() {
      String code =
          """
          import org.springframework.transaction.annotation.*;
          public class OuterClass {
            @Transactional
            public void foo() {}

            class Mid {
              class Inner {
                void bar() { OuterClass.this.foo(); } // Should Violate
              }
            }
          }
          """;
      var violations = analyseWithRule(rule, code);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(1, violations.size());
    }

    // TODO: FAILED ON PURPOSE until Outer.this resolves to the named class
    @Test
    void test_twoLevelNesting_midThis_flags() {
      String code =
          """
          import org.springframework.transaction.annotation.*;
          public class OuterClass {
            class Mid {
              @Transactional
              public void foo() {}

              class Inner {
                void bar() { Mid.this.foo(); } // Should Violate
              }
            }
          }
          """;
      var violations = analyseWithRule(rule, code);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(1, violations.size());
    }

    // ---- should already pass ----

    @Test
    void test_outerThisFromInner_outerNotTransactional_noFlag() {
      nestedClassCode =
          nestedClassCode.replace(
              "// ADD INNER CODE", "OuterClass.this.foo(); // Should NOT Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertNoViolation(violations);
    }

    @Test
    void test_outerThisDirectlyInOuter_flags() {
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace("// ANY CALLS", "OuterClass.this.foo(); // Should Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertViolation(violations, TRANSACTIONAL_SELF_INVOCATION_RULE_ID);
      assertEquals(1, violations.size());
    }

    @Test
    void test_outerThisFromInner_arityMismatch_noFlag() {
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional public void foo(int x) {}")
              .replace("// ADD INNER CODE", "OuterClass.this.foo(); // Should NOT Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
      assertNoViolation(violations);
    }

    @Test
    void test_outerThisFieldReceiver_noFlag() {
      nestedClassCode =
          nestedClassCode
              .replace("// OUTER ANNOTATION", "@Transactional")
              .replace("// ADD INNER CODE", "OuterClass.this.helper.foo(); // Should NOT Violate");
      var violations = analyseWithRule(rule, nestedClassCode);
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
