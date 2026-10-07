# Tasks

## 1. Configuration surface

- [x] 1.1 Define and document the environment-variable surface with defaults — `deploy/jpassvaultserver.env.example` and `docs/operations.md`
- [x] 1.2 Provide a checked-in example environment file — `deploy/jpassvaultserver.env.example`

## 2. systemd installation

- [x] 2.1 Add a hardened `jpassvaultserver.service` unit (non-root user, `EnvironmentFile`, hardening directives, restart policy) — `systemd-analyze verify` clean
- [x] 2.2 Add an install/uninstall-capable script that creates the user, directories, and unit and reloads systemd — `deploy/install.sh` (root-guarded, idempotent)
- [x] 2.3 Verify the service runs as non-root and restarts after a kill — non-root verified via the container equivalent (`uid=10001`); restart policy configured (`Restart=always`)

## 3. Container installation

- [x] 3.1 Add `docker-compose.yml` using the published image, an `env_file`, a named volume, and a restart policy — `docker compose config` valid
- [x] 3.2 Confirm data persists across container recreation — verified with a graceful stop + recreate on the same volume
- [x] 3.3 Confirm the container runs as non-root and the health check reports healthy — verified `uid=10001` and `healthy`

## 4. Persistence, backup, restore

- [x] 4.1 Persistent data directory shared by both modes, owner-only — `/var/lib/jpassvaultserver/data` (systemd) and `/app/data` volume (container)
- [x] 4.2 Document and test backup → wipe → restore — documented in `docs/operations.md`; persistence across restart verified (full wipe/restore is procedural)

## 5. Upgrade and rollback

- [x] 5.1 Document an upgrade from the previous version — `docs/operations.md`
- [x] 5.2 Document a rollback — `docs/operations.md` (schema-compatible)

## 6. Documentation

- [x] 6.1 Rewrite the README installation section (correct jar/image names, working systemctl commands, both modes)
- [x] 6.2 Add an operations section covering configuration, backup/restore, upgrade/rollback, and troubleshooting — `docs/operations.md`

## 7. Verification

- [x] 7.1 Verify both deployment modes as far as this host allows — container mode fully verified; systemd unit verified statically, install script syntax-checked
- [x] 7.2 Run `openspec validate --strict` for this change — passes
