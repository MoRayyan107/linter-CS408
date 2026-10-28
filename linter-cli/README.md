# CLI

This folder contains the command-line interface for running the linter.

## Running from Maven

Run the following commands from the root folder (`linter`).

First, compile the code:
```Bash
mvn clean install  
```

Then, run the CLI:

```Bash
mvn -pl linter-cli exec:java "-Dexec.mainClass=cli.LinterCli"
```

The CLI prints the linter result for the input file. The result depends on the
contents of the file provided.

## Testing with a specific file

### Pass case

Use the path to an existing Java file as the input. The path can be relative
or absolute.

> **IMPORTANT:** The file path must be enclosed in single quotes (`' '`). The
> file must also exist and be readable, otherwise the linter will fail.

```bash
mvn -pl linter-cli exec:java "-Dexec.mainClass=cli.LinterCli" -Dexec.args="'missing/Example.java'"
```

### Fail cases

```bash
mvn -pl linter-cli exec:java "-Dexec.mainClass=cli.LinterCli" -Dexec.args="'linter-web/src/main/java/com/cs408/linter/LinterApplication.java'"
```
