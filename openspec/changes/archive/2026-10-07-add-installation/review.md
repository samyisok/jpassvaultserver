# Review

## Code review — 2026-10-07

**Scope:** deployment assets (`deploy/*`, `docker-compose.yml`, `docs/operations.md`, README install content). No domain/application code changed.

### Checks
- DDD: not applicable.
- GRASP: not applicable.
- Readability: unit file, env example, and operations guide document the required secret and TLS posture; install script is idempotent and guarded by a root check.
- Cop: scripts fail on error (`set -euo pipefail`); the service account is a system user with no shell; data paths are owner-only.

### Verified locally
- `systemd-analyze verify deploy/jpassvaultserver.service` — clean.
- `docker compose config` — valid (with the env file present; the real env file is git-ignored).
- Persistence: a record stored in the container survived a graceful `docker stop` + recreate with the same named volume, and was returned by `GET /files/last`.
- Container runs as `uid=10001(app)`; data dir `0700`, DB file `0600`.

### Residuals
- `deploy/install.sh` was syntax-checked and the unit verified, but not executed as root on this host (it would create a system user and install under `/opt` and `/var/lib`). First real install is the true check.
- A hard kill (`SIGKILL`) can lose recent H2 MVStore writes; the documented backup procedure requires a graceful stop (noted in `docs/operations.md`).
- README links to `docs/openapi.yaml` and `AGENTS.md`, which `add-project-documentation` creates next.
