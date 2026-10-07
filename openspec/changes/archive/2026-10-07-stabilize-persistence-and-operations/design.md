# Design

## Context

- Current state: `spring.datasource.url=jdbc:h2:file:./maindb`; `DatabasePermissions.applyOwnerOnlyPermissions` runs in a `CommandLineRunner` (after Hibernate has already created the database) and chmods `databaseFile.getParent()`, which for the default URL is the process working directory. `DatabasePermissions.validateCredentials` only rejects *explicitly* configured blank/default credentials, so H2's implicit `sa`/empty is accepted — the security spec's "reject default database credentials" is unmet. `AppProperties.validateTransport` passes only when in-process TLS is configured or `allow-plain-http=true` (a development flag). The three servlet filters have no explicit order. Third-party Actions use floating major tags. `release.yml` always tags the image `:latest`.
- Constraint: the project must remain a simple, single-instance, embedded H2 file service that works out of the box.
- Constraint: the HTTP contract and the H2 schema must not change.

## Goals / Non-Goals

**Goals:**

- A persistent H2 database that works from the first start with the simplest setup.
- The database directory is dedicated and owner-only, never the working directory.
- A clear, defensible answer to the database-credentials requirement.
- Deterministic security-filter ordering.
- A proxy-terminated TLS path that does not use the development flag.
- Web-layer tests, immutable action pins, an OpenAPI lint, and stable-only `latest`.

**Non-Goals:**

- Replacing H2 or adding a database migration framework.
- Database credentials or an OS keychain.
- A distributed/shared throttle.
- Image signing beyond the existing buildx provenance attestation.

## Decisions

### D1: Dedicated data directory, created before startup

Default URL becomes `jdbc:h2:file:./data/maindb`. `JpassvaultserverApplication.main` resolves the effective datasource URL (env `SPRING_DATASOURCE_URL`, else a `--spring.datasource.url=` argument, else the default), derives its parent directory, and creates it owner-only (`0700`) **before** `SpringApplication.run`. A `DatabasePermissions` helper does the creation and also tightens the directory and the database files (`.mv.db`, `.lock.db`, `.trace.db`) after the database opens. The directory is never the process working directory for the default path.

- Why before startup: guarantees the dedicated directory exists owner-only so H2 does not have to create it and the database never lands in the process working directory. The database files themselves are tightened again after H2 creates them (they are created by H2 under the process umask).
- Why the default is `./data/maindb`: a stable, dedicated, obviously-persistent location that the deployment changes (`/var/lib/jpassvaultserver/data` for systemd, `/app/data` for the container) override via `SPRING_DATASOURCE_URL`.

### D2: No database credentials — the file is the security boundary

Remove the `validateCredentials` check and the corresponding requirement. Rationale:

- H2 here is an **embedded, single-writer** file database. There is no network database listener and no second application user; the only reader/writer is the service process.
- The protection that matters is filesystem access: the directory is `0700` and the files `0600`, owned by the service account. A database password would sit in the same environment file as the API secret and add no protection an attacker with file access could not also read.
- Requiring credentials would force extra configuration and contradict "works from the start".

This is a deliberate spec change, not a silent omission: the owner-only requirement is kept and strengthened (dedicated directory), and the credential sentence is removed.

### D3: A distinct proxy-TLS acknowledgment

Add `app-properties.tls-terminated-at-proxy` (default `false`). `validateTransport` passes when in-process TLS is configured, **or** `tls-terminated-at-proxy=true`, **or** the development `allow-plain-http=true`. The proxy flag documents intent and keeps the development flag for local use; the operator remains responsible for actually terminating TLS at the proxy.

- Alternative considered: require a loopback bind when the proxy flag is set. Rejected: the container binds `0.0.0.0` internally and maps only `127.0.0.1` on the host, so a loopback requirement would break the shipped deployment.

### D4: Deterministic filter order

Assign `@Order`: `SecurityHeadersFilter` (1), `AuthKeyFilter` (2), `RequestSizeLimitFilter` (3). Authentication runs before the body-size limit, so an oversized request with an invalid token is `401`, not `411`/`413`. Each filter still sets its own response headers, so ordering does not affect header coverage.

### D5: Web-layer tests with MockMvc

Add a `@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` test that exercises the real filter chain: valid `/check` `200`, missing token `401` with `Invalid API KEY`, blank upload `400`, oversized upload `413`, and repeated failures `429`. This complements the existing unit tests, which call methods directly and never exercise ordering.

### D6: Pin third-party actions to commit SHAs

Pin every third-party Action (and the GitHub-owned ones) to a full commit SHA with a trailing `# vX.Y.Z` comment; Dependabot keeps them current. Rationale: a floating tag/branch is attacker-mutable and executes with the workflow token.

### D7: Lint the OpenAPI document in CI

Add a lint step for `docs/openapi.yaml` using a pinned linter version. Start with the linter's default/`minimal` ruleset and fix what it reports so the gate is meaningful from day one.

### D8: `latest` only for the newest stable release

In `release.yml`, compute the image tag list from the version: include `:latest` only when the version has no pre-release suffix (`-`). A pre-release tag publishes `:<version>` only.

### D9: `File` equality by persistent identity

`File.equals`/`hashCode` use the `id` (and identity for unsaved instances) instead of the payload string, so comparisons do not hash up to the payload limit. JPA entities have no natural business key here; the generated id is the identity. Tests that asserted payload-based equality are updated.

## Risks / Trade-offs

- [Moving the default database path could orphan an existing `./maindb`] → documented; operators set `SPRING_DATASOURCE_URL` to keep an existing file, and the previous default never had a dedicated directory.
- [Creating the directory in `main` duplicates URL parsing] → reuse the same `DatabasePermissions` helper; the deployment modes still override the URL explicitly.
- [Removing DB credentials weakens defense in depth] → accepted for an embedded single-writer file; the owner-only permission boundary is kept and the rationale is recorded.
- [Proxy-TLS flag can be set without a real proxy] → it is an acknowledgment, like `allow-plain-http`; documentation states the operator's responsibility.
- [SHA pinning adds churn] → Dependabot updates the pins.
- [OpenAPI linter may be noisy] → start with a minimal ruleset and fix findings.

## Migration Plan

1. Default URL → `./data/maindb`; create the data directory owner-only before startup; stop chmod'ing the working directory.
2. Remove credential validation; update the spec.
3. Add the proxy-TLS flag; add filter `@Order`; update `File` equality.
4. Add MockMvc tests; pin actions; add the OpenAPI lint; make `latest` stable-only.
5. Update docs (`README.md`, `docs/operations.md`, `docs/ci.md`, `docs/openapi.yaml`, `deploy/*`).

Rollback: revert the change; an operator who adopted `./data/maindb` can point `SPRING_DATASOURCE_URL` back at `./maindb`.

## Open Questions

- Whether to add an unauthenticated readiness endpoint so the container health check does not pass the token as a process argument (backlog).
- Whether to adopt a distributed throttle if the service ever runs multi-instance (backlog).
