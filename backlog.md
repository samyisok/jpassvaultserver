# Backlog

Deferred items found during autonomous implementation. Not blocking the current
workflow; address later.

## Pre-existing design smells

- `src/main/java/com/samyisok/jpassvaultserver/domains/File.java` — JPA
  (`@Entity`, `@Column`, `@Lob`) annotations and an anemic model inside the
  `domains` package. Consider separating a persistence entity from a domain
  object or renaming the package to reflect it is persistence-only. Surfaced by
  the DDD review during `modernize-java-stack`.
- `FileController` computed the integrity hash itself; resolved by
  `harden-server-security` (checksum handling + MD5 removal). Re-check after
  that change lands.

## Deferred from harden-server-security (security review residuals)

- H2 database default credentials (`sa`/empty) remain in effect when no
  `spring.datasource.username`/`password` are configured. The change rejects only
  explicitly-configured blank/default credentials. Fail-closed enforcement needs
  a credential bootstrap that runs before the DataSource initialises. Tracked by
  `add-installation` (configuration surface) and this item.
- `jdbc:h2:file:./maindb` makes the process working directory the "database
  directory", which is chmod'd `0700`; use a dedicated data directory (for example
  `./data/maindb`) and scope permission tightening to it. Also cover H2 auxiliary
  files (`.lock.db`, `.trace.db`).
- Edge-terminated TLS currently requires `allow-plain-http=true`, which also
  disables the TLS guard. Add a distinct edge-TLS acknowledgment.
- Auth throttle is in-memory, per-instance, and resets on restart; document and
  rely on edge-level rate limiting for global protection.
- Filter ordering (`AuthKeyFilter`, `SecurityHeadersFilter`, `RequestSizeLimitFilter`)
  is not explicitly ordered; add `@Order` if interactions become significant.

