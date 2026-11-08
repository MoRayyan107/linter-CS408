package core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * Parser class is where we will parse the code and return a list of violations the parser converts
 * the source code int an AST (Abstract Syntax Tree) and then traverses the AST to find any
 * violations of the rules defined in the linter
 *
 * <p>The main usage here is the javaParser library, which is a library that can parse Java code and
 * return an AST. The AST can then be traversed to find any violations of the rules defined in the
 * linter.
 */
public class Parser {

  private final JavaParser javaParser;

  public Parser() {
    ParserConfiguration parserConfiguration = new ParserConfiguration();
    parserConfiguration.setSymbolResolver(
        new JavaSymbolSolver(new CombinedTypeSolver(new ReflectionTypeSolver())));

    this.javaParser = new JavaParser(parserConfiguration);
  }

  /**
   * Parses the given Java source code and returns a ParseResult containing the CompilationUnit
   * (AST). this function returns a NON-NULL ParseResult<T> object.
   *
   * @param sourceCode The Java source code as a string.
   * @return A {@code ParseResult<CompilationUnit>} containing the CompilationUnit (AST) or errors
   *     if parsing fails.
   */
  public ParseResult<CompilationUnit> parse(String sourceCode) {
    return javaParser.parse(sourceCode);
  }
}
