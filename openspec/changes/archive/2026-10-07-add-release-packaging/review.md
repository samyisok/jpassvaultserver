# Review

## Code review — 2026-10-07

**Scope:** release packaging (build config, `Dockerfile`, `.dockerignore`, `release.yml`). No domain/application code changed.

### Checks
- DDD: not applicable.
- GRASP: not applicable.
- Readability: `Dockerfile` documents the required secret/TLS posture; workflow steps are named.
- Cop: the default build now produces only the executable jar (`jar { enabled = false }`); the image runs non-root and declares a health check.

### Verified locally
- `./gradlew clean build` produces only `build/libs/jpassvaultserver-2.0.0.jar` (no `-plain.jar`).
- `docker build` succeeds; the container runs as `uid=10001(app)`, the health check reaches `healthy`, `GET /check` returns `{"check":"ok"}`, and `/app/data` is `0700` with `maindb.mv.db` `0600`.
- `release.yml` parses as valid YAML and the changelog notes extraction works (missing section → minimal note).

### Residuals (not verifiable on this host)
- The SBOM step, checksums, GHCR push, and the real tag-triggered release require GitHub/network access; they are syntax-validated only.
- The workflow tags `:latest` on every release; the spec says only the newest stable release should move `latest`. Pre-release handling is deferred (see `backlog.md`).
- Image signing / build provenance is out of scope (recorded as an Open Question in `design.md`).
