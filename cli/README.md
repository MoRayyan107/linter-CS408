this folder holds the core structure of checks 

Running the code from Maven from root (linter)

First Compile the code
```Bash
mvn clean install
```

Then run the CLI from the root folder (linter)

```Bash
mvn -pl cli exec:java "-Dexec.mainClass=cli.LinterCli"
```

Expected from the CLI currently

```Bash
Analyzing code...
```