# Proposal

## Why

The README describes a manual systemd setup with copy-paste commands, a jar name that no longer matches (`jpassvaultserver-1.0.0.jar`), a `systemctl enable example.service` typo, and no container deployment at all. There is no configuration reference, no backup or restore procedure, no upgrade/rollback guidance, and no hardening of the service account. Operators storing real vaults need a reliable, documented way to install, configure, back up, upgrade, and roll back the sync server.

## What Changes

- Provide a supported systemd installation: a checked-in unit file (hardened: dedicated non-root user, `NoNewPrivileges`, `ProtectSystem`, `PrivateTmp`, restricted write paths, `EnvironmentFile` for the secret, restart policy) and an install/uninstall script.
- Provide a container deployment: a `docker-compose.yml` (or equivalent) that runs the published image with a named volume for the H2 database, the secret from an environment file, a health check, and a restart policy.
- Document the complete configuration surface: `JPASSVAULT_SECRET`, listen address and port, database path, TLS/plain-HTTP settings, and trusted-proxy setting, with defaults and examples.
- Specify data persistence: the database lives on a path that survives restarts and upgrades; the directory and file are owner-only.
- Provide backup, restore, and upgrade/rollback procedures that preserve stored vaults, and a startup readiness check based on the existing health endpoint.
- Keep the HTTP API and data format unchanged; this is a deployment/configuration change only.

## Capabilities

### New Capabilities

- `server-installation`: how the sync service is installed, configured, persisted, backed up, upgraded, and rolled back — via systemd or a container — including the configuration surface and the health/readiness contract.

### Modified Capabilities

None. `release-packaging` produces the artifacts; this change defines how they are deployed and operated.

## Impact

- New files: a systemd unit template, an install/uninstall script, a `docker-compose.yml`, and an example environment file.
- Documentation: the README installation section is rewritten and gains a configuration reference and an operations runbook (shared with `add-project-documentation`).
- Operations: the secret must be supplied via the environment file (consistent with `harden-server-security`); the database must live on a persistent, owner-only path.
- No API or data-format change.
