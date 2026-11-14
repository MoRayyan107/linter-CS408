package core.rules;

import static core.Constants.*;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import core.Violation;
import enums.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Flags a call to a {@code @Transactional} method of the same class. The call bypasses the Spring
 * proxy, so the method's transaction settings are ignored.
 *
 * <p>Matching is by AST structure only (no symbol solver). See {@code
 * docs/transaction-self-invocation.md}.
 */
public class TransactionalSelfInvocationRule implements Rule {

  @Override
  public List<Violation> check(CompilationUnit ast) {
    List<Violation> violations = new ArrayList<>();

    for (MethodCallExpr call : ast.findAll(MethodCallExpr.class)) {
      if (!isSelfCall(call)) continue;

      Optional<ClassOrInterfaceDeclaration> callerClass =
          call.findAncestor(ClassOrInterfaceDeclaration.class);

      // interfaces have no instance bodies to proxy
      if (callerClass.isEmpty() || callerClass.get().isInterface()) continue;

      List<MethodDeclaration> candidates = findCandidates(callerClass.get(), call);
      if (candidates.isEmpty()) continue;

      if (candidates.stream().allMatch(method -> isTransactional(method, ast))) {
        violations.add(createSelfInvocationViolation(call));
      }
    }
    return violations;
  }

  /**
   * Checks if a method call is a self-call.
   *
   * <p>A self-call is a call to a method of the same class, either as {@code this.method()} or
   * {@code method()} <br>
   * NOT on {@code ClassName.method()}
   *
   * @param call the check on a method call expression
   * @return true if the method call is a self-call, false otherwise
   */
  private boolean isSelfCall(MethodCallExpr call) {
    return call.getScope().map(Expression::isThisExpr).orElse(true);
  }

  /**
   * Find methods on the same class on the basis on its name and number of arguments, and that are
   * non-static
   *
   * @param type the class to search the methods
   * @param call the method call expression to match the methods
   * @return List of methods that match the filter criteria (name, number of arguments, non-static)
   */
  private List<MethodDeclaration> findCandidates(
      ClassOrInterfaceDeclaration type, MethodCallExpr call) {
    return type.getMethods().stream()
        .filter(method -> method.getNameAsString().equals(call.getNameAsString()))
        .filter(method -> method.getParameters().size() == call.getArguments().size())
        .filter(method -> !method.isStatic())
        .toList();
  }

  private boolean isTransactional(MethodDeclaration method, CompilationUnit ast) {
    return method.getAnnotations().stream()
        .anyMatch(annotation -> isTransactionalAnnotation(annotation, ast));
  }

  private boolean isTransactionalAnnotation(AnnotationExpr annotationExpr, CompilationUnit ast) {
    // name without the '@'
    String annotationName = annotationExpr.getNameAsString();

    if (annotationName.contains(".")) {
      return SUPPORTED_TRANSACTIONAL_ANNOTATION.contains(annotationName);
    }
    if (!annotationName.equals("Transactional")) return false;

    // a Transactional type declared in this file shadows any import
    for (TypeDeclaration<?> type : ast.findAll(TypeDeclaration.class)) {
      if (type.getNameAsString().equals("Transactional")) return false;
    }

    List<ImportDeclaration> imports = ast.findAll(ImportDeclaration.class);

    // a single-type import wins over a wildcard import
    for (ImportDeclaration imp : imports) {
      if (imp.isAsterisk() || imp.isStatic()) continue;

      String importName = imp.getNameAsString();
      if (importName.endsWith(".Transactional")) {
        return SUPPORTED_TRANSACTIONAL_ANNOTATION.contains(importName);
      }
    }

    for (ImportDeclaration imp : imports) {
      if (!imp.isAsterisk() || imp.isStatic()) continue;

      if (SUPPORTED_TRANSACTIONAL_IMPORTS_ASTERISK.contains(imp.getNameAsString())) return true;
    }

    return false;
  }

  private Violation createSelfInvocationViolation(MethodCallExpr expr) {
    return new Violation(
        TRANSACTIONAL_SELF_INVOCATION_RULE_ID,
        "Method call to a @Transactional method from within the same class.",
        Severity.WARNING,
        expr.getBegin().map(p -> p.line).orElse(-1),
        expr.getBegin().map(p -> p.column).orElse(-1),
        "Consider refactoring the code to avoid self-invocation of @Transactional methods.");
  }
}
