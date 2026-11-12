# Linter Web Backend

This module contains the Spring Boot application scaffold for the web interface.
It does not yet expose a linting API or integrate the core analysis engine.
An HTTP server and linting endpoints are not currently configured.

## Requirements

JDK 21 and Maven.

## Build, test, and run

Run these commands from the repository root:

```bash
# Build, test, and check formatting
mvn -pl linter-web/backend clean verify

# Run the application entry point
mvn -pl linter-web/backend spring-boot:run
```

The run command starts the current application scaffold; it does not provide a
web API. Application configuration is in `src/main/resources/application.yaml`.

## Formatting

```bash
mvn -pl linter-web/backend spotless:apply
```

Spotless is configured directly in this module's POM because its parent is
Spring Boot's parent POM. Being listed as a module in the root POM does not
make it inherit the root's plugin configuration.

## Automated checks

The backend is included in the repository's Java build, test, and formatting
checks, and in the Java CodeQL configuration. See the
[main README](../../README.md) for workflow triggers and test reports.
