# Design

## Context

- See proposal.md — Why. Current README instructions are manual and partly incorrect (jar name `1.0.0` vs current `1.0.1`, `enable example.service` typo, no data-directory guidance). The service uses an embedded H2 file database (`./maindb`) and a `/check` endpoint, and is protected by a shared secret (`JPASSVAULT_SECRET`) after `harden-server-security`.
- Constraint: the database is a single writable file location; both deployment modes must mount it persistently and keep it owner-only.
- Constraint: `add-release-packaging` publishes a versioned jar and a GHCR image; installation consumes those.
- Constraint: the server must not run as root.

## Goals / Non-Goals

**Goals:**

- Two supported deployment modes (systemd bare-jar, container) with equivalent configuration.
- A documented, minimal configuration surface with safe defaults.
- Data that survives restarts and upgrades, with backup/restore and rollback procedures.
- Readiness based on the health endpoint.

**Non-Goals:**

- Orchestration beyond a single instance (Kubernetes manifests, HA, clustering).
- A database migration framework.
- Reverse-proxy/TLS certificate provisioning (documented, not automated).
- Client installation (that is the jpassvault desktop app).

## Decisions

### D1: systemd unit with hardening and an environment file

A checked-in `jpassvaultserver.service` runs a dedicated unprivileged user with `EnvironmentFile=/etc/jpassvaultserver.env` for `JPASSVAULT_SECRET` and other settings, `WorkingDirectory` on the data directory, `Restart=always`, and hardening directives (`NoNewPrivileges=true`, `ProtectSystem=strict`, `ProtectHome=true`, `PrivateTmp=true`, `ReadWritePaths=<data dir>`). The install script creates the user, directories, and unit, and reloads systemd.

**Rationale:** The current manual instructions are error-prone and unhardened; a checked-in unit makes deployment reproducible and reviewable.

### D2: Container deployment with a named volume

`docker-compose.yml` runs the published image as a non-root user, injects the secret from an `env_file` (not baked into the image), mounts a named volume at the database path, sets a `restart: unless-stopped` policy, and relies on the image's `HEALTHCHECK`. The Compose file is a deployment template, not a build file.

**Rationale:** Operators increasingly prefer a single `docker compose up`; a named volume avoids losing the database on container replacement.

### D3: One configuration surface for both modes

Both modes use the same environment variables: `JPASSVAULT_SECRET` (required), `SERVER_ADDRESS`/`SERVER_PORT`, the database path, TLS/plain-HTTP settings, and the trusted-proxy flag. Spring Boot's relaxed binding maps environment variables to the equivalent properties, so the same values work in the unit file and in Compose.

**Rationale:** One documented surface avoids drift between deployment modes.

### D4: Data directory is the unit of persistence

The database directory (default `./data`, holding `maindb.mv.db`) is the single persistent artifact. It is created owner-only and is the only writable path the service needs. Backup = a consistent copy of that directory; restore = replacing it while the service is stopped.

**Rationale:** A single directory makes backup, restore, and volume mounting trivial and safe.

### D5: Backup/restore and upgrade/rollback are documented procedures

- **Backup:** stop the service (or use H2's online backup), copy the data directory, verify the copy.
- **Restore:** stop the service, replace the data directory, start.
- **Upgrade:** back up, install the new jar/image, start, verify `/check` and `GET /files/last`; if it fails, roll back to the previous version and the backup.
- **Rollback:** the previous version reads the same schema; a schema downgrade is not attempted.

### D6: Readiness from the health endpoint

The systemd unit uses `ExecStartPost`/a `curl` health probe or documents one; Compose uses the image `HEALTHCHECK`. The exact authentication approach for `/check` is resolved with `add-release-packaging` (token in the probe or a dedicated readiness endpoint).

## Risks / Trade-offs

- [The health endpoint may require a token] → document supplying it from the environment; align with the packaging decision.
- [H2 file database is not safe for concurrent multi-writer access] → single-instance, one service process; documented as a constraint.
- [data directory path differs between modes] → one default and one configurable path, documented identically.
- [systemd hardening breaks the service if paths are wrong] → the install script sets `ReadWritePaths` to the data directory; documented troubleshooting.
- [Compose `latest` drift] → pin an explicit version tag in the template, with a note to update deliberately.

## Migration Plan

1. Add the systemd unit, install script, and example environment file; test on a clean VM.
2. Add `docker-compose.yml` and test start/backup/restore/upgrade.
3. Rewrite the README installation section and add the configuration reference and runbook.
4. Existing manual deployments can adopt the unit file without moving data if the data directory is pointed at the existing `maindb` location.

## Open Questions

- Default data directory name (`./data` vs the current working-directory-relative `./maindb`); the current deployments use the working directory, so the migration must not move existing data implicitly.
- Whether to ship a TLS reverse-proxy example (for example Caddy/nginx) in addition to documentation.
- Whether `/check` stays token-protected for health probes or a dedicated unauthenticated readiness endpoint is added.
