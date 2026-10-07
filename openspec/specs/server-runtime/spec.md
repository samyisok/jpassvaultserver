# server-runtime Specification

## Purpose
Defines the build-and-runtime contract for the jpassvault sync server: the JDK and framework baseline it runs on, the build tooling and dependency sources, and the compatibility of its HTTP API and stored data across the stack upgrade.

## Requirements

### Requirement: JDK 25 runtime baseline

The server SHALL build and run on Java 25 (LTS). The build SHALL target Java 25 bytecode through a Gradle Java toolchain, and continuous integration SHALL build and test with a JDK 25 toolchain.

#### Scenario: Build and run on JDK 25

- **WHEN** the server is built with `./gradlew build` on a JDK 25 toolchain
- **THEN** compilation succeeds and a runnable Spring Boot jar is produced

#### Scenario: CI uses JDK 25

- **WHEN** a push or pull request triggers the CI workflow
- **THEN** the workflow sets up a JDK 25 toolchain and runs the build and tests with it

### Requirement: Supported Spring Boot with Jakarta namespace

The server SHALL depend on a currently supported Spring Boot line that supports JDK 25 and SHALL NOT depend on `javax.servlet.*`, `javax.persistence.*`, or `javax.xml.bind.*`. It SHALL NOT run on the end-of-life Spring Boot 2.4.x line.

#### Scenario: Application starts on the new stack

- **WHEN** the upgraded application starts
- **THEN** the Spring context loads and the HTTP endpoints are served, with all Servlet and Persistence types coming from the `jakarta.*` namespace

#### Scenario: No legacy javax API remains in use

- **WHEN** the main sources are compiled
- **THEN** no `javax.servlet`, `javax.persistence`, or `javax.xml.bind` type is referenced

### Requirement: Build resolves only from supported repositories

The build SHALL resolve dependencies from Maven Central and SHALL NOT use the retired JCenter repository. The Gradle wrapper SHALL be on a currently supported Gradle line.

#### Scenario: Build has no JCenter dependency

- **WHEN** the build runs with the repository list in `build.gradle`
- **THEN** no dependency is resolved from `jcenter()` and the build completes from Maven Central

#### Scenario: Wrapper is on a supported Gradle line

- **WHEN** `./gradlew --version` is run
- **THEN** the reported Gradle version is on the current supported 8.x (or newer) line

### Requirement: Preserved HTTP API contract

The upgrade SHALL keep the existing HTTP API unchanged: the endpoints `GET /check`, `POST /files`, `GET /files/last`, and `GET /files/last/checksum`; the `token` request header; and the `check`, `file`, and `hash` JSON field names. An invalid or missing token SHALL still be answered with HTTP `401` and the body `Invalid API KEY`.

#### Scenario: Existing client endpoints keep working

- **WHEN** a client calls `GET /check` with a valid token
- **THEN** the response body is `{"check":"ok"}`

#### Scenario: Missing or invalid token is rejected as before

- **WHEN** a request is sent without a valid `token` header
- **THEN** the server responds with HTTP `401` and the body `Invalid API KEY`

#### Scenario: Stored vault round-trips through the API

- **WHEN** a client posts a vault payload to `POST /files` and then calls `GET /files/last`
- **THEN** the same payload is returned in the `file` field

### Requirement: Preserved stored data across the upgrade

An H2 database file (`maindb.mv.db`) created by the previous version SHALL remain readable after the upgrade, and previously stored vault records SHALL remain retrievable. A failure to open or migrate the database SHALL fail loudly at startup rather than silently discarding records.

#### Scenario: Existing database opens after upgrade

- **WHEN** the upgraded server starts against a database file created by the previous version
- **THEN** it starts successfully and `GET /files/last` returns the most recently stored record

#### Scenario: Migration failure is not silent data loss

- **WHEN** the existing database cannot be opened or reconciled by the upgraded stack
- **THEN** startup fails with an error and the existing file is not modified in place without a backup

### Requirement: Tests run on the current test stack

The project SHALL compile and run its tests on JUnit 5 (Jupiter) and a currently supported Mockito line, without the deprecated standalone `mockito-inline` artifact.

#### Scenario: Suite passes on the current stack

- **WHEN** `./gradlew test` is run on the upgraded stack
- **THEN** all existing tests are discovered and pass under JUnit 5
