# Review

## Code review — 2026-10-07

**Scope:** working tree vs `HEAD` (modernize-java-stack)
**Files changed:** build/wrapper/workflow + 3 main sources + 10 test sources

### Findings

- No new readability, DDD, GRASP, or cop issues introduced by this change; the
  edits are mechanical (namespace move, `HexFormat`, Mockito/Spring-7 test
  extension wiring, Gradle/Boot version bumps).

### Pre-existing (out of scope, backlogged)

- `domains/File.java` — JPA annotations (`@Entity`, `@Column`, `@Lob`) in the
  `domains` package and an anemic model (fields + getters/setters only). Not
  introduced here. Tracked in `backlog.md`; not fixed to keep this change a
  pure stack migration.
- `controllers/FileController.java` — computes the integrity hash directly in
  the controller (Information Expert). Addressed by `harden-server-security`
  (moves checksum handling and removes MD5).

### Verification

- `./gradlew clean build` — BUILD SUCCESSFUL (42 tests).
- Runtime contract verified against the built jar on JDK 25 / Spring Boot 4.1.1:
  `GET /check` → `{"check":"ok"}`, bad token → `401`, `POST /files` →
  `GET /files/last` round-trip, `GET /files/last/checksum` returns a hash.
- `openspec validate --strict modernize-java-stack` — valid.
