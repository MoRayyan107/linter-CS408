“Prototype checking logic implemented in LinterEngine; extraction into a separate rule is planned.”

# Rule: Empty catch block
Status: Implemented — validation in progress

## Purpose:
Find exceptions that are caught and silently ignored.

## Flag when:
The catch body contains no statements and the exception
variable is neither "ignore" nor "ignored", compared
case-insensitively.

## Do not flag when:
- The exception variable is named "ignore" or "ignored",
  regardless of capitalisation.
- The catch body contains at least one statement.

## Comment handling:
Comments do not suppress this rule.
A body containing only comments still contains no statements.

## Information needed from the parser:
- Catch clauses
- Statements inside each catch body
- Exception variable names
- Line and column of each catch clause

## How the AST is inspected:
1. Search the CompilationUnit for CatchClause nodes.
2. Read each catch clause's exception variable name.
3. Access its body and check whether its statement list is empty.
4. Apply the rule conditions.
5. Create a Violation for each matching catch clause.

JavaParser performs the tree search through findAll().
The rule then inspects each catch node returned.

## Current output:
- Rule ID: CATCH_CLAUSE_EMPTY_BODY
- Message: Catch clause has an empty body.
- Severity: WARNING
- Location: Line and column where the catch clause begins
- Suggestion: Handle the exception or explicitly indicate
  intentional ignoring using the variable name "ignored".

| Examples to test: | Manual Test Result | Automated Test Result |
| --- | --- | --- |
| Empty body, variable named "e": one violation. | ✅ | ✅ |
| Whitespace-only body, variable named "e": one violation. | ✅ | ✅ |
| Empty body, variable named "ignored": no violation. | ✅ | ✅ |
| Empty body, variable named "ignore": no violation. | ✅ | ✅ |
| Empty body, variable named "IGNORE": no violation. | ✅ | ✅ |
| Comment-only body, variable named "e": one violation. | ✅ | ✅ |
| Body containing a logging statement: no violation. | ✅ | ✅ |
| Two empty catch bodies named "e": two violations. | ✅ | ✅ |
| File containing no catch clauses: no violations. | ✅ | ✅ |

## Additional checks:
- Analyse a violating file, then a clean file using the same
  engine instance. The second result must contain no old findings.
- Parsing failure must be reported and rule checking must stop.

## Limitations:
- An ignored variable name indicates intent, but does not prove
  that ignoring the exception is safe.
- A nonempty body does not prove that the exception is handled
  meaningfully. This rule only checks whether statements exist.

## Remaining work:
- Run the examples and record actual results.
- Add automated tests.
- Confirm the final severity and suggestion wording.