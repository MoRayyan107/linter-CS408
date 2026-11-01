package core;

import enums.Severity;
import lombok.Builder;

/**
 * Violation class is where we will store any issues that the linter has found in the code
 * @author: Rayyan
 * @version: 0.1
 */

@Builder
public class Violation {

    private final String ruleId;
    private final String message;
    private final Severity severity;
    private final int line;
    private final int column;
    private final String suggestion;

    public Violation(String ruleId, String message, Severity severity, int line, int column, String suggestion) {
        if (line < 1) { throw new IllegalArgumentException("line must be at least 1"); }
        if (column < 1) { throw new IllegalArgumentException("column must be at least 1"); }

        this.ruleId = ruleId;
        this.message = message;
        this.severity = severity;
        this.line = line;
        this.column = column;
        this.suggestion = suggestion;
    }

    public int getColumn() { return column; }
    public int getLine() { return line; }
    public String getMessage() { return message; }
    public String getRuleId() { return ruleId; }
    public Severity getSeverity() { return severity; }
    public String getSuggestion() { return suggestion; }

    @Override
    public String toString() {
        return String.format("Violation [ruleId=%s, message=%s, severity=%s, line=%d, column=%d, suggestion=%s]\n",
            ruleId, message, severity, line, column, suggestion);
    }

}
