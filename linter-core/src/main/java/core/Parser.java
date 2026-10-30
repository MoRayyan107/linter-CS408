package core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Parser class is where we will parse the code and return a list of violations
 * the parser converts the source code int an AST (Abstract Syntax Tree) and then traverses the AST to find any violations of the rules defined in the linter
 *
 * The main usage here is the javaParser library, which is a library that can parse Java code and return an AST.
 * The AST can then be traversed to find any violations of the rules defined in the linter.
 */
public class Parser {

    private final JavaParser javaParser;

    public Parser() {
        this.javaParser = new JavaParser();
    }

    /**
     * Parses the given Java source code and returns a ParseResult containing the CompilationUnit (AST).
     *
     * @param fileDataInput The Java source code as a string.
     * @return A ParseResult containing the CompilationUnit (AST) or errors if parsing fails.
     */
    public ParseResult<CompilationUnit> parse(String fileDataInput) {
        return javaParser.parse(fileDataInput);
    }

}
