package core.rules;

import com.github.javaparser.ast.CompilationUnit;
import core.Violation;
import java.util.List;

public interface Rule {
  List<Violation> check(CompilationUnit ast);
}
