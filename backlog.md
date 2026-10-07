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

- `deploy/install.sh` is now verified end to end in a container by
  `deploy/install-verify.sh` (file layout plus the service starting as the
  service user). The systemd unit is checked with `systemd-analyze verify`; a
  real systemd boot is still the final check.

## Pre-existing design smells

- `persistence/File.java` is a JPA entity that is also the API request/response
  model. Acceptable while the schema is 1:1; introduce a response DTO only if the
  API shape ever diverges from the stored row.
