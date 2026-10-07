# Code review — jpassvault sync server

Date: 2026-10-07 · Version: 2.1.0 · Reviewers: java-cop, security-reviewer
(two independent passes), plus a DDD/GRASP/readability pass.

## Scope

Whole application: `src/main/java/com/samyisok/jpassvaultserver/**`,
`src/main/resources/application.properties`, `Dockerfile`, `docker-compose.yml`,
`deploy/*`, and `.github/**`. Spring Boot 4.1.1 / Java 25 / Gradle 9.8.1; H2
file database; shared-secret `token` header auth.

## Verdict

**APPROVE-WITH-CHANGES.** No authentication bypass, no hardcoded live secret, no
MD5, and no code path that can overwrite or delete a stored vault payload through
the API. All production files are ≤350 lines. The findings below were fixed in
this review; the remainder are tracked in `backlog.md`.

## Fixed

### Security
- **Weak/default secret accepted.** `validateSecret` now rejects well-known
  defaults and secrets shorter than 16 characters; the env template ships an
  empty secret so an unedited install fails closed.
- **Mutable CI action.** The dependency-scan action is pinned to the v1.1.0
  commit instead of `@main`.
- **Release had no verification gate.** `release.yml` now runs
  `./gradlew clean build` (tests, coverage, Checkstyle) before building/pushing;
  image build emits provenance.
- **CodeQL analyzed without a build.** Now compiles the project before analysis.
- **Throttle keyed on an attacker-influenceable forwarded header.** Throttling
  now keys on the connection address; the forwarded address is used only for
  logging and is sanitized (control characters stripped, length capped).
- **Container template disabled the TLS guard by default.** The hardcoded
  `allow-plain-http` was removed from `docker-compose.yml`; it must come from the
  operator's env file.
- **Permissions applied late / H2 auxiliary files uncovered.** The systemd unit
  sets `UMask=0077`; the image creates `/app/data` as `0700`; `.lock.db` and
  `.trace.db` are now tightened alongside the database file.
- **Filter error responses lacked a content type.** `401`/`429`/`411`/`413`
  bodies are now `text/plain;charset=UTF-8`.

### Correctness / robustness
- **Blank or null upload accepted and persisted.** `POST /files` now returns
  `400` for a blank/missing `file` (the OpenAPI already marked it required).
- **Unbounded storage growth.** The service now keeps only the newest payload,
  bounding the database.
- **`id` could bind from a request.** `File.getId()` is now
  `@JsonProperty(access = READ_ONLY)`, preventing a client-supplied `id` from
  triggering a merge-overwrite.
- **Misc.** `AuthCheck` uses constructor injection and throws
  `IllegalStateException`; `FileNotFoundAdvice` class/method renamed to match the
  file; `serialVersionUID` `1l` → `1L`.

## Verified

- `./gradlew clean build` green (80 tests, coverage gate, Checkstyle).
- Runtime: `401` with `text/plain`, blank upload `400`, checksum echo, retention
  keeps only the newest row, weak/short secret fails startup.
- Container: runs as `uid=10001`, health check healthy, `/app/data` mode `0700`.
- Client compatibility: the jpassvault client now uploads its keyed
  HMAC-SHA256 checksum, so change detection matches the server's stored value.

## Residual (see `backlog.md`)

- H2 implicit `sa`/empty credentials are still accepted when no credentials are
  configured — the spec's "reject default DB credentials" is only partially met.
- The data directory is the process working directory for the default H2 URL.
- The auth throttle is in-memory, per-instance, and resets on restart.
- Edge-terminated TLS still requires the plain-HTTP acknowledgment.
- Not all third-party actions are pinned to full commit SHAs.
- Image signing/provenance beyond the buildx attestation; `:latest` moves on
  every tag.
- No strict OpenAPI linter; a hard `SIGKILL` can lose recent H2 writes.
