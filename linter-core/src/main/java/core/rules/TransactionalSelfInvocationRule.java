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
import com.github.javaparser.resolution.UnsolvedSymbolException;
import core.Violation;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Rule is based for Spring Boot Transactional Self Invocation, where a method thats annotated
 * with @Transactional is called from within the same class, which effects the Spring Proxy hence
 * the annotation is ignored
 */

// TODO: SOFTEN UP THIS CLASS AND KIDNA REMOVE THE LOGS
@Slf4j
public class TransactionalSelfInvocationRule implements Rule {

  @Override
  public List<Violation> check(CompilationUnit ast) {
    List<Violation> violations = new ArrayList<>();

    // get all the method calls and imports from the AST
    List<MethodCallExpr> methodCalls = ast.findAll(MethodCallExpr.class);

    for (MethodCallExpr calls : methodCalls) {
      // hold the Method Name, Scope and if this ccall has a (this.methodName) scope
      String methodName = calls.getNameAsString();
      boolean hasScope = calls.getScope().isPresent();
      boolean hasThisExpr = calls.getScope().map(Expression::isThisExpr).orElse(false);

      if (hasScope && !hasThisExpr) {
        // if the method call has a scope and the scope is not "this", then we can ignore it
        continue;
      }

      log.info(
          "Possible this.methodName() call found: {}. Checking for @Transactional annotation.",
          methodName);
      log.info(
          "Checking for Transactional Annotation for the method call {} with scope {} and does it have a 'this' scope? {}",
          methodName,
          calls.getScope().orElse(null),
          hasThisExpr);

      try {
        // resolve the method call to get the target method declaration
        var targetMethod = calls.resolve();

        // skip the check if the target method is static, as static methods cannot be annotated with
        // @Transactional
        if (targetMethod.isStatic()) {
          log.info("Method call {} is static, skipping @Transactional check.", methodName);
          continue;
        }

        // store the target class name
        var targetClassName = targetMethod.declaringType().getQualifiedName();

        var callerClass = calls.findAncestor(ClassOrInterfaceDeclaration.class);
        if (callerClass.isEmpty()) {
          log.info("Caller class not found for method call {}, skipping check.", methodName);
          continue;
        }

        var callerClassName = callerClass.get().resolve().getQualifiedName();

        // chek if the caller/target are empty and if they are not equal, then we can ignore it
        if (!targetClassName.equals(callerClassName)) {
          log.info("Either target class or caller class is empty, skipping check.");
          continue;
        }

        log.info(
            "Checking @Transactional annotation for method {} in class {}",
            methodName,
            targetClassName);

        // get the TargetDeclaration of the Method
        var targetDeclaration = targetMethod.toAst(MethodDeclaration.class);
        if (targetDeclaration.isEmpty()) continue;

        // check if the target method has the @Transactional annotation
        boolean hasTransactionalAnnotation =
            targetDeclaration.get().getAnnotations().stream()
                .anyMatch(annotationExpr -> isTransactionalAnnotation(annotationExpr, ast));

        if (!hasTransactionalAnnotation) continue;

      } catch (UnsolvedSymbolException e) {
        log.error("Error occurred while resolving method call: {}", calls, e);
        continue;
      }

      // if reached till herer we definetly havea  violation
      violations.add(createSelfInvocationViolation(calls));
    }
    return violations;
  }

  // helper method
  private boolean isTransactionalAnnotation(AnnotationExpr annotationExpr, CompilationUnit ast) {

    // DOES NOT INCLUDE THE '@' SIGN
    String annotationName = annotationExpr.getNameAsString();

    // get all the imports and type declarations from the AST
    List<ImportDeclaration> imports = ast.findAll(ImportDeclaration.class);
    var typeDeclarations = ast.findAll(TypeDeclaration.class);

    if (annotationName.contains(".")) {
      return SUPPORTED_TRANSACTIONAL_ANNOTATION.contains(annotationName);
    }

    if (!annotationName.equals("Transactional")) return false;

    // get the Type declarations from the AST
    for (TypeDeclaration<?> t : typeDeclarations) {
      // if th typeDeclaration is Transaction then return false
      if (t.getNameAsString().equals("Transactional")) return false;
    }

    // step 4loop into the imports to check for non wildcard imports
    for (ImportDeclaration imp : imports) {
      // this is for NON-WILDCARD imports, if static and asterisk skip it
      if (imp.isAsterisk() || imp.isStatic()) continue;

      String importName = imp.getNameAsString();
      if (importName.endsWith(".Transactional"))
        return SUPPORTED_TRANSACTIONAL_ANNOTATION.contains(importName);
    }

    // Step 5 loop into the imports to check for wildcard imports
    for (ImportDeclaration imp : imports) {
      // chekl for WILDCARD imports only
      if (!imp.isAsterisk() || imp.isStatic()) continue;

      if (SUPPORTED_TRANSACTIONAL_IMPORTS_ASTERISK.contains(imp.getNameAsString())) return true;
    }

    return false;
  }

  private Violation createSelfInvocationViolation(MethodCallExpr expr) {
    return new Violation(
        TRANSACTIONAL_SELF_INVOCATION_RULE_ID,
        "Method call to a @Transactional method from within the same class.",
        enums.Severity.WARNING,
        expr.getBegin().map(p -> p.line).orElse(-1),
        expr.getBegin().map(p -> p.column).orElse(-1),
        "Consider refactoring the code to avoid self-invocation of @Transactional methods.");
  }
}
