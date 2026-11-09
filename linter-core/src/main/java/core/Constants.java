package core;

import java.util.Set;

public final class Constants {

  public Constants() throws RuntimeException {
    throw new RuntimeException("Constants class should not be instantiated");
  }

  public static final Set<String> SUPPORTED_TRANSACTIONAL_ANNOTATION =
      Set.of(
          "org.springframework.transaction.annotation.Transactional",
          "jakarta.transaction.Transactional");

  public static final Set<String> SUPPORTED_TRANSACTIONAL_IMPORTS_ASTERISK =
      Set.of("org.springframework.transaction.annotation", "jakarta.transaction");

  // RULE IDS
  public static final String CATCH_CLAUSE_RULE_ID = "CATCH_CLAUSE_EMPTY_BODY";
  public static final String TRANSACTIONAL_SELF_INVOCATION_RULE_ID =
      "TRANSACTIONAL_SELF_INVOCATION";
}
