# Linter CLI

The CLI reads one Java source file, passes its contents to `linter-core`, and
prints the returned violations.

## Requirements

JDK 21 and Maven. Run the commands below from the repository root.

## Build and run

Install the Java modules locally so Maven can resolve the CLI's core dependency:

```bash
mvn clean install
```

Analyse an existing Java file:

```bash
mvn -pl linter-cli exec:java "-Dexec.mainClass=cli.LinterCli" "-Dexec.args='linter-web/backend/src/main/java/com/cs408/linter/LinterApplication.java'"
```

Replace the example path with your own relative or absolute path. The file must
exist, be readable, and have a `.java` extension. Keep paths containing spaces
quoted inside `exec.args`, as shown above.

## Output and limitations

- The CLI prints the list of violations returned by the core engine.
- An empty list means no rule violations were returned. Parse errors are printed
  separately, so an empty list does not necessarily mean the input was valid.
- The CLI handles a single file; it does not recursively scan directories.
- Detected violations do not currently produce a dedicated failure exit code.
  Do not use the process exit code alone as a lint quality gate.
- The documented launch method uses Maven; an executable CLI JAR is not yet configured.

See the [core README](../linter-core/README.md) for the current lint rules.

## Development

```bash
# Build and test the CLI along with its required modules
mvn -pl linter-cli -am clean verify

# Format the CLI's Java files
mvn -pl linter-cli spotless:apply
```

The CLI participates in the repository's Java CI checks and CodeQL configuration.
See the [main README](../README.md) for the full workflow description.
