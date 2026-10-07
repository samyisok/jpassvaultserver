# Backlog

Deferred items. Not blocking; address later.

## Data-at-rest

- A hard kill (SIGKILL) can lose recent H2 MVStore writes; backups require a
  graceful stop (documented in `docs/operations.md`).
- Database-credential validation was removed by design: the embedded,
  single-writer H2 file is protected by owner-only filesystem permissions rather
  than a database password. Revisit only if H2 is ever replaced by a networked
  database.

## Security / transport

- Auth throttle is in-memory, per-instance, and resets on restart; document and
  rely on edge-level rate limiting for global protection.
- Health check passes the token via curl arguments (visible in `/proc` inside the
  container); consider a token file or a dedicated readiness path.

## Release / supply chain

- Consider image signing (cosign) beyond the buildx provenance attestation.
- `deploy/install.sh` was syntax-checked and the systemd unit verified, but not
  executed as root on a dev host. First real install is the true check.

## Pre-existing design smells

- `domains/File.java` is a JPA entity with `@Entity`/`@Column`/`@Lob` inside the
  `domains` package. Consider separating persistence from the domain or renaming
  the package.
- `FileController` holds a payload-size business rule and talks directly to the
  repository; no application-service boundary.
