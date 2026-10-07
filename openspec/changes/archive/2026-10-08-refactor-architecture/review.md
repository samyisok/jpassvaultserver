# Review

## java-cop review — 2026-10-08

**Scope:** working tree vs `HEAD` (refactor-architecture)
**Verdict before fixes:** APPROVE WITH MINOR CHANGES (no Critical/Major)

### Fixed

- Added a `store` test for an oversized checksum (the moved `413` branch was
  otherwise uncovered).
- Extracted a private `newestOrNull()` in `VaultService` so `last()` and
  `lastChecksum()` share one lookup.
- Gave the checksum-length violation its own message instead of reusing the
  payload message.
- Repository now returns `Optional<File> findFirstByOrderByIdDesc()` instead of a
  `List` unwrapped at each call site.
- `FileNotFoundException` lost its unused `(Long id)` constructor;
  `FileNotFoundExceptionTest` became a plain unit test (no Spring context).
- Renamed the misspelled `IntergrationFileTest` → `IntegrationFileTest`.
- `deploy/install-verify.sh` now runs `systemd-analyze verify` itself (the claim
  is no longer external), and the CI workflow runs the installer verification.

### Accepted / no action

- `VaultService` throws `ResponseStatusException` (Spring Web) for `400`/`413` —
  documented in `design.md` D3; revisit only if a second consumer appears.
- The JPA entity remains the API model (1:1 with the documented schema).

### DDD / GRASP

- `VaultService` is a correct application service: Information Expert for
  validation (holds payload + `AppProperties`), policy owner for retention,
  thin controller. The `domains` → `persistence` rename removes the misleading
  package; no infrastructure remains in a "domain" package.

### Verified

- `./gradlew clean build` green (tests, coverage gate, Checkstyle).
- HTTP contract unchanged: `SecurityFilterChainIntegrationTest` (6 cases) passes;
  `docs/openapi.yaml` unchanged.
- `deploy/install-verify.sh` PASS: installer file layout (`/var/lib/.../data`
  `0700`, jar `0644`, env `0600`, unit present) and the service starting as the
  `jpassvaultserver` user and answering `/check` `200`; `systemd-analyze verify`
  clean.
- Retention integration test proves only the newest payload survives.
