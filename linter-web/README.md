# Linter Web

The web interface for the linter: a Spring Boot backend (`backend/`) and a
React + Vite frontend (`frontend/`).

## Status

- **Backend:** set up. It calls the core engine (`linter-core`) on submitted
  Java source and returns the violations as JSON.
- **Output format:** the response is the raw `List<Violation>`. A clearer,
  more readable output format is planned.
- **Frontend:** only a scaffold for now. It is not connected to the backend
  yet and will be implemented later.

## API

`POST /api/code-submission/submit` (port 8080)

The request body is raw Java source. The response is a JSON array of
violations. Each violation has 1-based line and column numbers.

If the source can't be parsed, the response is an empty array, so an empty
result doesn't prove the code is valid.

Example requests are in [`docs/codeSubmissionRequest.http`](../docs/codeSubmissionRequest.http).

## Requirements

JDK 21 and Maven.

## Build, test, and run

Run these commands from the repository root. The backend depends on
`linter-core`, so install the core module first:

```bash
# Install the root and core POMs to ~/.m2
mvn -q install -pl linter-core -am -DskipTests

# Build, test, and check formatting
mvn -pl linter-web/backend clean verify

# Start the backend on port 8080
mvn -pl linter-web/backend spring-boot:run
```

Application configuration is in `backend/src/main/resources/application.yaml`.

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
[main README](../README.md) for workflow triggers and test reports.
