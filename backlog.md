# Backlog

Deferred items. Not blocking; address later.

## Data-at-rest / database

- H2 implicit `sa`/empty credentials are still in effect when no
  `spring.datasource.username`/`password` are configured. The change rejects only
  explicitly-configured blank/default credentials; the server-security spec says
  default credentials should be rejected. Fail-closed enforcement needs a
  credential bootstrap that runs before the DataSource initialises.
- `jdbc:h2:file:./maindb` makes the process working directory the "database
  directory", which is chmod'd `0700`; use a dedicated data directory (for
  example `./data/maindb`) and scope permission tightening to it.
- A hard kill (SIGKILL) can lose recent H2 MVStore writes; backups require a
  graceful stop (documented in `docs/operations.md`).

## Security / transport

- Edge-terminated TLS still requires `allow-plain-http=true`, which also disables
  the TLS guard. Add a distinct edge-TLS acknowledgment.
- Auth throttle is in-memory, per-instance, and resets on restart; document and
  rely on edge-level rate limiting for global protection.
- Filter ordering (`AuthKeyFilter`, `SecurityHeadersFilter`,
  `RequestSizeLimitFilter`) is not explicitly ordered; add `@Order` if
  interactions become significant.
- Health check passes the token via curl arguments (visible in `/proc` inside the
  container); consider a token file or a dedicated readiness path.

## Release / supply chain

- Not all third-party GitHub Actions are pinned to full commit SHAs (the
  dependency-scan action is; others use major tags).
- The release workflow moves `:latest` on every tag; only the newest stable
  release should. Add pre-release handling.
- Consider image signing (cosign) beyond the buildx provenance attestation.
- `deploy/install.sh` was syntax-checked and the systemd unit verified, but not
  executed as root on a dev host. First real install is the true check.

## Documentation / tooling

- No strict OpenAPI linter (Redocly/Spectral) is installed; the spec is
  YAML-parsed and structurally checked only. Consider adding one to CI.
- Consider a MockMvc/web-layer test that exercises the real filter chain
  (401/400/411/413/429 and filter ordering) end to end.

## Pre-existing design smells

- `domains/File.java` is a JPA entity with `@Entity`/`@Column`/`@Lob` and an
  anemic model inside the `domains` package. Consider separating persistence from
  the domain or renaming the package.
- `FileController` holds a payload-size business rule and talks directly to the
  repository; no application-service boundary.
- `File.equals`/`hashCode` hash the full payload; base them on `id` if these
  entities ever land in hash collections.
