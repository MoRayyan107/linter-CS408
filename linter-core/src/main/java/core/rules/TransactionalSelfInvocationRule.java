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

    // hold the Method Name, Scope and if this ccall has a (this.methodName) scope
    for (MethodCallExpr call : methodCalls) {
      String methodName = call.getNameAsString();
      boolean hasScope = call.getScope().isPresent();
      boolean hasThisExpr = call.getScope().map(Expression::isThisExpr).orElse(false);

      if (hasScope && !hasThisExpr) continue;

      //      log.info(
      //          "Possible this.methodName() call found: {}. Checking for @Transactional
      // annotation.",
      //          methodName);
      //      log.info(
      //          "Checking for Transactional Annotation for the method call {} with scope {} and
      // does it have a 'this' scope? {}",
      //          methodName,
      //          call.getScope().orElse(null),
      //          hasThisExpr);

      // gte the caller class
      var callerClass = call.findAncestor(ClassOrInterfaceDeclaration.class);
      if (callerClass.isEmpty()) {
        //        log.info("Caller class not found for method call {}, skipping check.",
        // methodName);
        continue;
      }

      // Sine Interfaces cant have a body like physical logic code we do not need to check for any
      // interface classes
      if (callerClass.get().isInterface()) {
        log.info("Caller class is an interface for method call {}, skipping check.", methodName);
        continue;
      }

      // get the List of MethodDeclarations from the calls
      var arguments = call.getArguments().size();
      //      log.info(
      //          "Method call {} has {} arguments. Checking for matching method declarations.",
      //          methodName,
      //          arguments);
      List<MethodDeclaration> declarationList =
          callerClass.get().getMethods().stream()
              .filter(methodParams -> methodParams.getParameters().size() == arguments)
              .filter(nameOfMethod -> nameOfMethod.getNameAsString().equals(methodName))
              .filter(checkStatic -> !checkStatic.isStatic())
              .toList();

      if (declarationList.isEmpty()) {
        log.info(
            "No matching method declarations found for method call {}, skipping check.",
            methodName);
        continue;
      }

      boolean hasTransactionalAnnotation =
          declarationList.stream()
              .anyMatch(
                  methods ->
                      methods.getAnnotations().stream()
                          .anyMatch(annotation -> isTransactionalAnnotation(annotation, ast)));

      if (!hasTransactionalAnnotation) continue;

      // if reached till herer we definetly havea  violation
      violations.add(createSelfInvocationViolation(call));
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

    if (annotationName.contains("."))
      return SUPPORTED_TRANSACTIONAL_ANNOTATION.contains(annotationName);

    if (!annotationName.equals("Transactional")) return false;

    // get the Type declarations from the AST
    for (TypeDeclaration<?> t : typeDeclarations) {
      // if th typeDeclaration is Transaction then return false
      if (t.getNameAsString().equals("Transactional")) return false;
    }

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

    // if nothing matches then return false
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
