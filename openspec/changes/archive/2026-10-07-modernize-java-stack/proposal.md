# Proposal

## Why

The sync server runs on an end-of-life stack: Java 11, Spring Boot 2.4.4 (EOL since 2021), Gradle 6.8.3, the retired JCenter repository, Mockito 3.9 on the deprecated `mockito-inline` artifact, and GitHub Actions pinned to `actions/checkout@v2` / `actions/setup-java@v2` with the retired AdoptOpenJDK distribution. Its code also compiles against `javax.*` (Servlet, Persistence, JAXB) APIs that no longer receive security fixes. Modernizing to Java 25 LTS with a current Spring Boot and Gradle removes EOL software, restores the security-update path, and keeps the existing HTTP API and stored data working.

## What Changes

- Migrate Java 11 → Java 25 (LTS): Gradle Java toolchain, compile/release target, and CI JDK.
- Migrate Spring Boot 2.4.4 → the current stable Spring Boot line that supports JDK 25. This is **BREAKING** at the framework level: `javax.persistence.*`, `javax.servlet.*`, and `javax.xml.bind.*` move to their `jakarta.*` successors.
- Replace `javax.xml.bind.DatatypeConverter` (removed from the JDK and from modern JAXB) with `java.util.HexFormat` in the checksum endpoint.
- Upgrade the Gradle wrapper from 6.8.3 to the current 8.x line and remove the retired `jcenter()` repository; resolve everything from `mavenCentral()`.
- Standardize on JUnit 5 (Jupiter) only and upgrade Mockito to the current line (the inline mock maker is the default; drop the separate `mockito-inline` dependency).
- Keep the H2 file database and the existing endpoints, headers, and JSON shapes unchanged: `GET /check`, `POST /files`, `GET /files/last`, `GET /files/last/checksum`, the `token` request header, and the `check` / `file` / `hash` JSON fields. Existing `maindb.mv.db` files SHALL remain openable after the upgrade.
- Update the CI workflow to the current action versions and JDK 25. The pipeline's gates are owned by `add-ci-pipeline`; this change only moves the toolchain it targets.
- Bump the application version `1.0.1` → `2.0.0` to mark the breaking runtime-stack change.

Security hardening of the running service is out of scope here (see `harden-server-security`), as are packaging, installation, and documentation (`add-release-packaging`, `add-installation`, `add-project-documentation`).

## Capabilities

### New Capabilities

- `server-runtime`: the build-and-runtime contract for the sync server — JDK baseline, Spring Boot baseline and namespace, build tooling, and compatibility of the HTTP API and stored H2 data across the upgrade.

### Modified Capabilities

None. The project has no existing specs; this change introduces the first capability spec.

## Impact

- Build: `build.gradle`, `settings.gradle`, and the Gradle wrapper (`gradle/wrapper/gradle-wrapper.properties`) move to Java 25 and Gradle 8.x; `jcenter()` is removed.
- Code: `javax.persistence` → `jakarta.persistence` in `domains/File.java` and `domains/FileRepository.java`; `javax.servlet` → `jakarta.servlet` in `auth/AuthKeyFilter.java`; `javax.xml.bind.DatatypeConverter` → `java.util.HexFormat` in `controllers/FileController.java`.
- Data: the H2 schema is managed by `spring.jpa.hibernate.ddl-auto=update`; the Hibernate 6 upgrade must be verified against an existing `maindb.mv.db` and a backup is required before first start on the new stack.
- Tests: JUnit 5 / Mockito upgrade; existing tests under `src/test` are updated to the current APIs.
- CI: `.github/workflows/gradle.yml` moves to JDK 25 and current action versions.
- API: no endpoint, header, or JSON shape change; the checksum *algorithm* is explicitly out of scope here and addressed by `harden-server-security`.
