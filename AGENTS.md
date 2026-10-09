# AGENTS.md

Guidance for contributors and coding agents working on the jpassvault sync
server.

## Build and run

- Always use the Gradle wrapper: `./gradlew <task>` (no system Gradle).
- Build: `./gradlew build`
- Tests + coverage + static analysis: `./gradlew check`
- Run locally:

  ```sh
  JPASSVAULT_SECRET=change-me APP_PROPERTIES_ALLOW_PLAIN_HTTP=true \
    ./gradlew bootRun
  ```

- Java 25 toolchain; Spring Boot 4.1.1 (Jakarta namespace). Do not reintroduce
  `javax.*` APIs or `jcenter()`.

## Testing

- JUnit 5 (Jupiter) only; Mockito for mocks.
- Plain `@Mock` fields need `@ExtendWith(MockitoExtension.class)` (Spring 7 no
  longer opens them); use `@MockitoSettings(strictness = Strictness.LENIENT)`
  only when a class genuinely needs it.
- Prefer the suffixes `*UnitTest` (no Spring context) and `*IntegrationTest`
  (Spring context / real collaborators).
- Tests run against `src/test/resources/application.properties`
  (in-memory H2, a test secret, plain HTTP acknowledged) so they never touch
  the real database or require environment secrets.

## Verification gates

`./gradlew check` enforces:

- all tests pass;
- JaCoCo coverage ≥ 85% line and ≥ 70% branch (`build.gradle`);
- Checkstyle (`config/checkstyle/checkstyle.xml`).

CI (`.github/workflows/gradle.yml`) additionally runs an OpenAPI lint, an
installer smoke test, and (when an `NVD_API_KEY` repository secret is configured)
an OWASP dependency scan; vulnerability alerts without a key come from
Dependabot. See `docs/ci.md`.

## Architecture

- Spring MVC `@RestController` (`FileController`) is a thin HTTP adapter over the
  `services/VaultService` application layer, which owns payload validation, the
  "keep only the newest payload" retention rule, and the checksum lookup.
- `VaultService` uses a Spring Data JPA repository (`persistence/FileRepository`)
  backed by an H2 file database; the JPA entity `persistence/File` is also the
  API request/response model (its serialization matches the documented schema).
- A servlet filter (`AuthKeyFilter`) authenticates every request with the shared
  `token` header; supporting components live in `auth/`, `security/`, and
  `persistence/`.
- The service stores encrypted payloads only; it never handles the master
  password. The API contract is documented in `docs/openapi.yaml`.

## Rules for new code

- Keep every Java file under 350 lines; split by responsibility if it grows.
- Prefer small, cohesive, single-purpose classes (see `security/`, `persistence/`).
- Write tests first where practical; a change that alters behavior ships with a
  test that would fail without it.
- Do not log secrets or tokens.
- Do not commit secrets; the API secret comes from the environment only.

## Documentation

A change that alters endpoints, request/response fields, configuration, or
deployment must update the matching document in the same change:

- endpoints/fields → `docs/openapi.yaml`
- configuration/deployment → `docs/operations.md` and `README.md`
- user-visible changes → `CHANGELOG.md`

## Gotchas

- The database is a single H2 file; stop the service gracefully before backups
  (a hard kill can lose recent writes).
- `spring.jpa.hibernate.ddl-auto=update` evolves the schema in place; there is
  no migration framework.
- Startup fails closed without a secret and without TLS (unless the plain-HTTP
  acknowledgment is set).
