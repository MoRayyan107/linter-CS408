# Linter

>Description of the project will be added later.

## Overview

<!-- Add a brief explanation of what the project does, who it is for, and the
     problem it solves. -->

## Features
> Yet to be added.

## Project Structure

```text
.
├── linter-cli/   # Command-line interface
├── linter-core/  # Core linter functionality
├── linter-web/   # Web interface
└── pom.xml       # Maven project configuration
```

## Requirements

- Java 21 or later
- Maven

## Getting Started

### Build the project

```bash
mvn clean install
```

### Run the CLI

```bash
mvn -pl linter-cli exec:java "-Dexec.mainClass=cli.LinterCli" "-Dexec.args='linter-web/src/main/java/com/cs408/linter/LinterApplication.java'"
```

## Usage

<!-- Add command examples and explain the available arguments/options. -->

```bash
 Usage will be update one the jar is fully ready
```

## Configuration

<!-- Document configuration files, environment variables, and default values. -->

## Development

### Build

```bash
mvn clean install
```

### Tests

```bash
mvn test
```

## Contributing

<!-- Add contribution guidelines, branch naming conventions, and pull request
     requirements. -->

## License

<!-- Add the project license and a link to the license file. -->

## Authors

<!-- Add contributors or project maintainer information. -->
