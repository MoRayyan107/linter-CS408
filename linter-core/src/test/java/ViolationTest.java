import static org.junit.jupiter.api.Assertions.assertEquals;

import core.Violation;
import enums.Severity;
import org.junit.jupiter.api.*;

class ViolationTest {

  private static Violation violation;

  @BeforeAll
  static void setup() {
    violation =
        new Violation(
            "Test violation",
            "This is a test message",
            Severity.WARNING,
            7,
            8,
            "A recomended Suggestion");
  }

  @Test
  void testGetters() {
    assertEquals("Test violation", violation.getRuleId());
    assertEquals(7, violation.getLine());
    assertEquals(8, violation.getColumn());
    assertEquals("This is a test message", violation.getMessage());
    assertEquals(Severity.WARNING, violation.getSeverity());
    assertEquals("A recomended Suggestion", violation.getSuggestion());
  }
}
