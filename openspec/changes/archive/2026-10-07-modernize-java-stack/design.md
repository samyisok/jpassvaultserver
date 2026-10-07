# Design

## Context

- See proposal.md — Why. Current state: Java 11, Spring Boot 2.4.4 with `spring-boot-starter-data-jpa`, `-web`, `-validation`, H2 file database, Gradle 6.8.3 wrapper, JUnit 5 (`useJUnitPlatform()`), Mockito 3.9 (`mockito-inline`/`mockito-core`/`mockito-junit-jupiter`), CI on JDK 11 (AdoptOpenJDK) and `checkout@v2`/`setup-java@v2`.
- The code touches three legacy namespaces: `javax.persistence` (`domains/File`, `domains/FileRepository`), `javax.servlet` (`auth/AuthKeyFilter`), and `javax.xml.bind.DatatypeConverter` (`controllers/FileController`). The JAXB import currently compiles only because Hibernate 5.x drags in `jakarta.xml.bind-api` 2.3.x, which still ships the `javax.xml.bind` package. That transitive crutch disappears on Hibernate 6.
- Contract constraints: the jpassvault client calls fixed paths and reads fixed JSON fields; the server stores vault payloads in a single H2 table and must keep existing `maindb.mv.db` files readable.
- Constraint: the sync client speaks `http`/`https` to `/check`, `/files`, `/files/last`, `/files/last/checksum` with a `token` header. Headers, paths, and JSON field names are part of the contract; the checksum value is not (see `harden-server-security`).

## Goals / Non-Goals

**Goals:**

- Build and run on JDK 25 with a supported Spring Boot and Gradle.
- Remove JCenter and all EOL build dependencies.
- Keep the HTTP API (paths, headers, JSON field names) and stored H2 data compatible.
- Keep the test suite meaningful on the current test stack.

**Non-Goals:**

- No new endpoints, no API redesign.
- No security hardening (secret handling, TLS, token comparison, checksum algorithm) — `harden-server-security`.
- No packaging, image building, installation, or documentation — separate changes.
- No schema redesign or migration framework.

## Decisions

### D1: Java 25 LTS via a Gradle Java toolchain

Pin the toolchain to Java 25 and set `sourceCompatibility`/`targetCompatibility` through the toolchain rather than a bare version string. Java 25 is the current LTS and matches the sibling jpassvault client, so one JDK line covers both projects.

**Alternative:** an interim Java 17/21 target — rejected; it leaves a second upgrade soon after and diverges from the client.

### D2: Current stable Spring Boot line with the Jakarta namespace

Move to the newest stable Spring Boot that supports JDK 25 (Spring Boot 3.5+/4.x; exact version pinned at implementation time). This pulls Spring Framework 6/7, Hibernate 6, Jakarta EE 10/11, and Tomcat 10+. It is the only supported path off Boot 2.4.4.

**Risk:** namespace and API churn. Mitigation: compile-driven migration of the three namespace imports, plus an explicit H2 compatibility check (D5).

### D3: Gradle 8.x wrapper, Maven Central only

Regenerate the wrapper to the current Gradle 8.x line and delete `jcenter()` (shut down 2022, now a read-only/redirect endpoint that can disappear). Keep `mavenCentral()`.

**Alternative:** Gradle 9 — pin the newest line that is known to work with the chosen Spring Boot plugin; exact version chosen at implementation time.

### D4: `HexFormat` replaces `DatatypeConverter`

The checksum endpoint hex-encodes a digest. `javax.xml.bind.DatatypeConverter` is gone on modern JAXB; `java.util.HexFormat.of().formatHex(bytes)` (Java 17+) is the JDK replacement. Hex output stays uppercase so the wire value's shape is unchanged.

### D5: H2/Hibernate 6 migration is verified, not assumed

`spring.jpa.hibernate.ddl-auto=update` lets Hibernate 6 evolve the existing H2 file schema. Hibernate 6 changed some type mappings, so the migration plan requires: copy `maindb.mv.db` to a backup, start the upgraded server against the copy, confirm `/files/last` returns a previously stored record, and only then roll the backup out of the way. If `update` cannot reconcile the schema, the fallback is an explicit `ALTER TABLE`/export-import step documented at implementation time.

**Non-goal:** switching to a migration framework (Flyway/Liquibase) — valuable but out of scope; recorded as an Open Question.

### D6: JUnit 5 only, current Mockito

The suite already runs on `useJUnitPlatform()`. Standardize on Jupiter-only imports and upgrade Mockito to the current line; the inline mock maker is the default, so `mockito-inline` is dropped and `mockito-junit-jupiter` is kept only if still needed. `@SpringBootTest`/`@SpyBean` behavior is re-verified under the new Boot line.

### D7: CI toolchain bump

`.github/workflows/gradle.yml` moves to `actions/checkout@v4`, `actions/setup-java@v4`, distribution `temurin`, `java-version: '25'`, and a Gradle cache. The gate design (coverage, scanning, badges) is owned by `add-ci-pipeline`; this decision only replaces the retired actions and JDK.

## Risks / Trade-offs

- [Hibernate 6 cannot reconcile an existing `maindb.mv.db`] → back up before first start (D5); verify with a stored record; fall back to an explicit schema/data migration.
- [Bean-validation / JPA annotations change behavior under Jakarta] → compile and run the existing test suite; the entity is small (`File`) and the repository is a single derived query.
- [Gradle 8 plugin incompatibility with the chosen Boot version] → pin the wrapper and Spring dependency-management plugin together at implementation time; the wrapper's exact version is a one-line change.
- [`ddl-auto=update` is unsafe in general] → unchanged from today's behavior; migration framework is out of scope (Open Question).
- [Action version drift] → the `add-ci-pipeline` change owns ongoing workflow policy; this change only stops using retired actions.

## Migration Plan

1. Confirm a working JDK 25 and Gradle toolchain; back up `maindb.mv.db`.
2. Regenerate the wrapper (Gradle 8.x) and update `build.gradle`/`settings.gradle` to Java 25 and the new Spring Boot line; remove `jcenter()`.
3. Migrate namespaces (`javax.*` → `jakarta.*`) and `DatatypeConverter` → `HexFormat`; compile until green.
4. Run `./gradlew test`; fix JUnit 5 / Mockito API drift.
5. Start against the backed-up H2 file; confirm `/check`, `/files/last`, `POST /files`, and `/files/last/checksum` behave as before.
6. Bump version to `2.0.0` and update CI to JDK 25 and current actions.

Rollback: restore the pre-upgrade jar and the backed-up database. The HTTP contract is unchanged, so a rollback needs no client change.

## Open Questions

- Whether to adopt Flyway/Liquibase for the H2 schema instead of `ddl-auto=update` (deferred; not required by this change).
- Exact Spring Boot, Gradle, and Mockito versions are pinned at implementation time against JDK 25.
