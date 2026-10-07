# Review

## java-cop review — 2026-10-07

**Scope:** working tree vs `HEAD` (stabilize-persistence-and-operations)
**Verdict before fixes:** BLOCK (one false comment; two major issues)

### Fixed

- **False comment (Critical).** `JpassvaultserverApplication` claimed the database
  file never exists under the process umask; only the *directory* is created
  before startup. Comment reworded and `design.md` D1 corrected.
- **Datasource-URL precedence (Major).** The pre-start directory resolution read
  the environment before command-line arguments, and ignored system properties,
  so the directory created could differ from the one H2 opens. Now resolves with
  Spring's precedence (arguments → system property → environment → default), and
  a unit test covers it.
- **Missing changelog / migration note (Major).** Added an `[Unreleased]` entry
  and an upgrade note that the default database moved from `./maindb` to
  `./data/maindb`, so existing deployments set `SPRING_DATASOURCE_URL`.
- Stale guidance that referenced the development `allow-plain-http` flag for
  proxy deployments (`docs/operations.md`, the systemd unit, the `Dockerfile`)
  now references `tls-terminated-at-proxy`.
- Pruned `backlog.md` items implemented by this change.
- `DatabasePermissionsUnitTest` uses `assertTrue` instead of `assertEquals(true, …)`.
- Added `JpassvaultserverApplicationUnitTest` for URL resolution.

### DDD / GRASP / readability

- No new domain logic; the persistence helper stays in `persistence/`. The new
  `File` identity (id-based `equals`/`hashCode`) is the standard JPA approach.
- `JpassvaultserverApplication` indentation aligned to two spaces.

### Verified

- `./gradlew clean build` green (tests, coverage gate, Checkstyle).
- `SecurityFilterChainIntegrationTest` (6 cases) passes, including the
  `401`-before-`413` ordering.
- `docs/openapi.yaml` passes `@redocly/cli lint` (recommended ruleset).
- Runtime: with the default configuration the service creates `./data` owner-only
  (`0700`) with `maindb.mv.db` `0600`, serves over the proxy-TLS acknowledgment
  (no plain-HTTP flag), and the stored record survives a restart.
- Workflows parse as valid YAML; all third-party actions pinned to commit SHAs.

### Residual

- Database files are created by H2 under the process umask and tightened
  immediately after; only the directory is owner-only before startup. Documented.
- Other residuals remain in `backlog.md` (in-memory throttle, health-check token
  in `/proc`, image signing, install script not run as root, design smells).
