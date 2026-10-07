# Design

## Context

- See proposal.md — Why. Current code: `AuthCheck` reads either the env secret or the `app-properties.secret-key` value and compares with `key.equals(token)`; `AuthKeyFilter` logs `"Invalid Token: " + token`, reads the client IP from `X-FORWARDED-FOR` unconditionally, and writes a fixed `Invalid API KEY` body on failure; `FileController` computes an uppercase MD5 over the stored payload for `GET /files/last/checksum`; `AppProperties.toString` prints the secret; `application.properties` ships a real-looking secret and binds `0.0.0.0:9393`; the H2 database lives at `./maindb` in the working directory.
- Constraints: the jpassvault client contract is fixed paths/headers/JSON field names, and the client's checksum is now a keyed HMAC-SHA256 over the vault plaintext — a value the server cannot compute (it never sees the master password). Existing databases must keep working.
- Constraint: no new heavyweight framework for auth — the token model is a single shared secret.

## Goals / Non-Goals

**Goals:**

- No secret material in the repository or in logs.
- Constant-time credential comparison.
- Encrypted transport required by default.
- Bounded request handling.
- Owner-only data-at-rest permissions.
- A checksum value that is not an unkeyed hash and stays usable by the client.

**Non-Goals:**

- Multi-user accounts, RBAC, or per-user tokens.
- A database migration framework.
- Client-side changes beyond the documented follow-up (checksum upload).
- Replacing H2 with an external database.

## Decisions

### D1: The secret comes only from the environment (or an external config), and startup fails closed

`app-properties.secret-key` is removed from the packaged `application.properties`. `use-secret-key-from-env` defaults to `true`. On startup the server SHALL fail if no secret is configured, or if the configured secret equals the previously committed placeholder (`555505424923a833fe77cfa68c497bf2`), so a forgotten deployment cannot silently keep the known value.

**Rationale:** A committed bearer secret is a full authentication bypass. Failing closed makes misconfiguration loud.

### D2: Constant-time token comparison

Compare the presented token and the configured secret using `MessageDigest.isEqual` over UTF-8 bytes (or compare fixed-length digests of both), with equal-time handling for different lengths.

**Rationale:** `String.equals` short-circuits on the first differing byte, leaking the matching prefix length. The shared-secret model deserves constant-time comparison even though network jitter partially masks timing.

### D3: Never log the credential

`AuthKeyFilter` logs a constant event (`"Invalid credential presented"`) plus the derived client IP; it never interpolates the token. The `Wrong token` value is discarded immediately and not retained.

**Rationale:** Logs are widely readable and often shipped off-host; a token in a log is a credential leak.

### D4: Trust proxy headers only when explicitly enabled

Add a configuration flag (for example `app-properties.trust-proxy-headers`, default `false`). When false, the logged client IP is `request.getRemoteAddr()`. When true (deployment behind a known reverse proxy), read the right-most entry of `X-Forwarded-For`/`Forwarded` — the hop appended by the nearest trusted proxy — rather than the left-most, which a client can forge. This mirrors Spring's `server.forward-headers-strategy` intent without blindly trusting client-supplied headers.

**Rationale:** An arbitrary client can set `X-FORWARDED-FOR`, so the current log entry is attacker-controlled and useless for rate limiting or forensics.

### D5: TLS required in production; plain HTTP only by explicit opt-in

The server is expected to run behind a TLS-terminating reverse proxy (recommended) or with `server.ssl.*` configured. In a non-development profile, startup fails if neither TLS nor an explicit `app-properties.allow-plain-http=true` acknowledgment is present. Documented deployment (in `add-installation`) terminates TLS at the edge and forwards only loopback traffic.

**Rationale:** The client already refuses non-`https` sync URLs (except localhost), so the server should not silently accept cleartext vault uploads.

### D6: Bound the request body

Set a maximum accepted payload size (`server.tomcat.max-http-form-post-size` / a servlet-level check, plus an explicit `POST /files` size guard) and reject larger bodies with HTTP `413`. The decoded vault payload also has a maximum length.

**Rationale:** `@Lob` plus an unbounded body lets any authenticated (or even unauthenticated, pre-filter) caller exhaust heap.

### D7: Client-supplied integrity value replaces MD5

`File` gains an optional `checksum` column. `POST /files` stores `{file, checksum}` when a checksum is supplied. `GET /files/last/checksum` returns that stored value verbatim; if the newest record has no stored checksum, the server returns a SHA-256 hex of the stored payload as a deterministic fallback. MD5 is removed.

**Rationale:** The client's change detection is now a keyed HMAC-SHA256 over plaintext; the server cannot reproduce it, so it must store and echo it. SHA-256 (not MD5) is the fallback so a weak hash is never the integrity value. The fallback keeps old clients functional.

**Compatibility note:** the jpassvault client must include `checksum` in its upload payload for exact matches; until then the fallback SHA-256 will differ from the client HMAC and sync will always upload (safe, but noisy). The JSON field name stays `hash` in the response, so no wire shape breaks.

### D8: Owner-only data at rest

On POSIX systems, the H2 database directory is created/forced to `0700` and `maindb.mv.db` to `0600` at startup. On non-POSIX systems this is a no-op with a documented residual. Blank/default database credentials are disallowed by validation.

**Rationale:** The file holds every stored vault payload; default umask can leave it group- or world-readable. This mirrors the client's owner-only requirement for its local vault.

### D9: Diagnostic and error hygiene

`AppProperties.toString` masks the secret (for example `****`). Error responses use Spring's `server.error.include-message=never`-style settings and no stack traces; `X-Content-Type-Options: nosniff` and `Cache-Control: no-store` are set on API responses.

**Rationale:** The current `toString` prints the full secret, and default error handling can echo internals.

### D10: Rate-limit failed authentication

Track failed attempts per derived client IP in a small bounded map and reject/delay beyond a threshold (for example HTTP `429` after N failures in a window). Keep it in-memory and bounded; no external store.

**Rationale:** With a shared static secret and no lockout, an attacker can brute-force it at line rate. A bounded per-IP limiter raises the cost.

## Risks / Trade-offs

- [Removing the committed secret breaks existing deployments that relied on it] → deliberate; the change is `BREAKING` and the README/installation guide documents `JPASSVAULT_SECRET`.
- [Requiring HTTPS can lock out plain-HTTP internal deployments] → `allow-plain-http` escape hatch plus documentation; the client already refuses non-https non-local URLs.
- [Client/server checksum mismatch until the client uploads its value] → safe fallback (always upload, never data loss); Open Question tracks the client follow-up.
- [In-memory rate limiter resets on restart and is per-instance] → accepted for a single-instance deployment; a reverse proxy is the robust place for global limits.
- [POSIX permissions are a no-op on Windows] → documented residual; the process account scoping still applies.
- [Constant-time comparison of different-length inputs] → compare fixed-length digests so length is not itself the leak.

## Migration Plan

1. Deployment supplies `JPASSVAULT_SECRET` (the old committed value is rejected as a placeholder).
2. Existing `maindb.mv.db` opens unchanged; the optional `checksum` column is added by the existing `ddl-auto=update`.
3. Old clients that do not send `checksum` still work via the SHA-256 fallback.
4. Rollback restores the previous jar; the data file remains readable.

## Open Questions

- Whether the client will be updated to upload its HMAC checksum as part of this work or as a follow-up jpassvault change (needed for exact change detection).
- Whether TLS should be terminated in-process (`server.ssl.*`) or always recommended at the edge — the installation change will pick one as the documented default.
- Whether to add an explicit `checksum` field name in the client payload or reuse an existing field; the server side is agnostic as long as the response field stays `hash`.
