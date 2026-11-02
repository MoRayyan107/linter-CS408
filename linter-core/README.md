# linter-core

`linter-core` contains the reusable Java source analysis engine used by the
other linter modules. It accepts Java source code as a string, parses it into a
JavaParser abstract syntax tree (AST), applies the configured rules, and
returns the detected issues as `Violation` objects.

## Responsibilities

- Parse Java source code with [JavaParser](https://javaparser.org/).
- Traverse the resulting AST and apply lint rules.
- Represent each finding with a rule ID, message, severity, source position,
  and suggested fix.
- Provide an API that can be called by the CLI or web modules without
  duplicating analysis logic.

## Main classes

| Class | Purpose |
| --- | --- |
| `core.Parser` | Converts Java source text into a `ParseResult<CompilationUnit>`. |
| `core.LinterEngine` | Runs the core lint rules and returns a list of violations. |
| `core.Violation` | Immutable data object describing one lint finding. |
| `enums.Severity` | Defines `INFO`, `WARNING`, and `ERROR` severities. |

## Current rules

### Empty catch clauses

`LinterEngine` reports a `CATCH_CLAUSE_EMPTY_BODY` warning when a catch clause
has no statements:

```java
try {
    doSomething();
} catch (Exception exception) {
}
```

The reported violation includes the catch clause's line and column and
suggests adding code to the catch body. Catch parameters named `ignore` or
`ignored` are treated as intentionally ignored and do not produce this
violation.

## Using the API

Create a `LinterEngine` and pass it readable Java source code:

```java
import core.LinterEngine;
import core.Violation;

import java.util.List;

LinterEngine engine = new LinterEngine();
List<Violation> violations = engine.analyse(sourceCode);

for (Violation violation : violations) {
    System.out.println(violation);
}
```

`analyse` returns an empty list when no violations are found. If JavaParser
cannot parse the source, the engine reports the parsing problems to standard
error and returns no rule violations for that input.

## Building and testing

Run these commands from the repository root:

```bash
# Compile and package linter-core and its dependencies
mvn -pl linter-core clean install

# Run linter-core tests
mvn -pl linter-core test
```

The module targets Java 21. Lombok is used at compile time for the
`Violation.builder()` factory and is configured as an annotation processor in
the root Maven build.
