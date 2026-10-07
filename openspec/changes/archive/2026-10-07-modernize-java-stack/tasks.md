# Tasks

## 1. Build tooling

- [x] 1.1 Back up the existing database file — N/A: no database file existed in the workspace or repository at migration time
- [x] 1.2 Regenerate the Gradle wrapper to the current supported 8.x line — wrapper set to Gradle 9.8.1 (current stable, required for JDK 25); `./gradlew --version` reports 9.8.1 on JDK 25
- [x] 1.3 Update `build.gradle`: Java 25 toolchain, current Spring Boot plugin and dependency-management versions, remove `jcenter()`, drop `mockito-inline`, bump Mockito — Spring Boot 4.1.1, dependency-management 1.1.7, Mockito provided by `spring-boot-starter-test`

## 2. Framework and namespace migration

- [x] 2.1 Migrate `javax.persistence` → `jakarta.persistence` in `domains/File.java` and `domains/FileRepository.java` — done; `compileJava` green
- [x] 2.2 Migrate `javax.servlet` → `jakarta.servlet` in `auth/AuthKeyFilter.java` and tests — done; `compileJava` green
- [x] 2.3 Replace `javax.xml.bind.DatatypeConverter` with `java.util.HexFormat` in `controllers/FileController.java`, keeping uppercase hex output — done; `grep -rn "javax\." src` returns nothing

## 3. Test-stack upgrade

- [x] 3.1 Update tests to JUnit 5 Jupiter-only APIs and the current Mockito line; re-verify `@SpringBootTest` / spy usage — `@SpyBean`/`@MockBean` migrated to `@MockitoSpyBean`/`@MockitoBean`; plain `@Mock` fields now initialized via `@ExtendWith(MockitoExtension.class)` + lenient settings (Spring 7 no longer auto-opens them)
- [x] 3.2 Confirm the full suite is discovered — 42 tests, all pass

## 4. Data and API compatibility

- [x] 4.1 Start the upgraded server against the backed-up H2 file — N/A: no pre-existing database; started with a fresh file successfully
- [x] 4.2 Verify the API contract end to end: `GET /check`, `POST /files`, `GET /files/last`, `GET /files/last/checksum`, and `401` on a bad token — verified against the built jar
- [x] 4.3 Verify a pre-upgrade record is still returned — N/A: no pre-upgrade database existed

## 5. Version and CI

- [x] 5.1 Bump the application version `1.0.1` → `2.0.0` — done
- [x] 5.2 Update `.github/workflows/gradle.yml` to JDK 25 (`temurin`), `actions/checkout@v4`, `actions/setup-java@v4` — done (full pipeline rework is `add-ci-pipeline`)

## 6. Verification

- [x] 6.1 Run the full build on JDK 25 (`./gradlew clean build`) — BUILD SUCCESSFUL
- [x] 6.2 Run `openspec validate --strict` for this change — passes
- [x] 6.3 Confirm no touched source file exceeds 350 lines — confirmed
