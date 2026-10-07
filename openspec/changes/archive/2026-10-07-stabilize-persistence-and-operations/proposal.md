# Proposal

## Why

The backlog from the previous changes holds a set of persistence, security, and CI issues. The most important is persistence: the default H2 URL `jdbc:h2:file:./maindb` makes the process working directory the database directory, and the startup permission pass then chmods that directory `0700` — which can be the repository, `/opt`, or a shared volume. The database-credential requirement in the security spec is also not actually met, and several smaller gaps remain (edge-terminated TLS needs the development plain-HTTP flag, filter order is undefined, actions are not pinned, `:latest` moves on every tag).

The intent for H2 is a persistent, embedded file database that works out of the box with the simplest possible setup. This change makes that explicit: a dedicated owner-only data directory, created before the database opens, with no reliance on database credentials — the OS permission boundary protects the file.

## What Changes

- **H2 persistence (simplest working setup):** the default database URL becomes `jdbc:h2:file:./data/maindb`; the data directory is created owner-only *before* Spring starts; the startup permission pass no longer touches the process working directory and only tightens the data directory and the database files.
- **Database credentials (spec change):** drop the "blank or default database credentials SHALL be rejected" requirement and its validation. The embedded, single-writer H2 file is protected by owner-only filesystem permissions; requiring credentials would force extra configuration and contradict "works from the start".
- **Edge-terminated TLS:** add a distinct `app-properties.tls-terminated-at-proxy` acknowledgment so a proxy-terminated deployment no longer has to set the development-only `allow-plain-http` flag.
- **Deterministic filter order:** `SecurityHeadersFilter` → `AuthKeyFilter` → `RequestSizeLimitFilter`, so an invalid token is answered `401` even when the body would otherwise be rejected with `411`/`413`.
- **Web-layer tests:** a MockMvc test exercises the real filter chain (`401`, `400`, `413`, `429`, and ordering).
- **CI / release:** pin every third-party GitHub Action to an immutable commit SHA; lint `docs/openapi.yaml` in CI; move the container `:latest` tag only for stable (non-pre-release) versions.
- **`File` entity:** base `equals`/`hashCode` on the persistent id instead of hashing the whole payload (avoids hashing up to the payload limit on every comparison).

Out of scope (kept in `backlog.md`): replacing H2, OS-keychain/DB credentials, a distributed throttle, and image signing.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `server-security`: the encrypted-transport requirement gains a proxy-terminated acknowledgment; the owner-only data requirement is reworded to a dedicated data directory with no database credentials; a new requirement states that authentication is evaluated before request limits.
- `server-installation`: the persistent data requirement uses a dedicated data directory rather than the process working directory.
- `ci-pipeline`: new requirements for immutable action pinning, API-specification linting, and web-layer filter-chain tests.
- `release-packaging`: the container-image requirement states that the `latest` tag moves only for the newest stable release.

## Impact

- Code: `application.properties` (default URL), `JpassvaultserverApplication` (create the data directory before startup), `persistence/DatabasePermissions` (directory helper, drop credential validation), `LoadDatabase` (drop credential check), `AppProperties` (proxy-TLS flag; transport validation), the three servlet filters (`@Order`), `domains/File` (`equals`/`hashCode`).
- Data: the default database moves to `./data/maindb`; the directory is owner-only and is not the working directory. Existing databases keep working when pointed at by `SPRING_DATASOURCE_URL`.
- CI: `.github/workflows/*` action pins, OpenAPI lint step; `release.yml` stable-only `latest`.
- Docs: `README.md`, `docs/operations.md`, `docs/ci.md`, `docs/openapi.yaml`, `deploy/*`.
- Tests: transport validation, data-directory handling, `File` equality, and a new MockMvc filter-chain test.
