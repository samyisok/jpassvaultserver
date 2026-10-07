# Review

## java-cop — 2026-10-07

**Scope:** working tree vs `HEAD` (harden-server-security)
**Verdict before fixes:** BLOCK (1 build-red test, 1 unbounded-body bypass)

### Fixed in this change

- `FileControllerPayloadSizeUnitTest` asserted `HttpStatus.PAYLOAD_TOO_LARGE` while
  the controller threw `CONTENT_TOO_LARGE` (distinct Spring 7 constants). Test
  updated; build is green.
- Payload bound was bypassable by a chunked/unknown-length body.
  `RequestSizeLimitFilter` now rejects a body of unknown length with `411` and
  keeps `413` for known oversized bodies.
- `AppProperties.isTlsConfigured` counted `server.ssl.key-store-type` alone as TLS;
  now requires `key-store`/`certificate` with `server.ssl.enabled != false`.
- `AuthKeyFilter` trusted the left-most `X-Forwarded-For` value; now uses the
  right-most hop (the one appended by the nearest trusted proxy).
- `SecurityHeadersFilter` skipped ERROR dispatch; `shouldNotFilterErrorDispatch()`
  now returns false.
- Stored `checksum` is length-bounded; `maxPayloadSize` cannot be null.
- `AuthThrottle.isBlocked` reads under the same lock as `recordFailure`.
- Replaced deprecated `HttpStatus.PAYLOAD_TOO_LARGE` with `CONTENT_TOO_LARGE`.
- Removed a vacuous MD5 assertion in `FileControllerChecksumTest`.

## Security review — 2026-10-07

### Fixed
- High: chunked-body payload bypass (above).
- High: spoofable proxy trust / throttle-key evasion (right-most hop; residual below).
- Medium: fail-open TLS check (above).
- Medium: unbounded/unvalidated `checksum` (above).
- Medium: security headers absent on ERROR dispatch (above).
- Low: unsynchronized `isBlocked` read (above).

### Residuals (accepted / backlogged)

- **DB default credentials.** H2's implicit `sa`/empty credentials are still in
  effect when no `spring.datasource.username`/`password` are configured; the
  validation only rejects *explicitly* configured blank/default values. Making
  this fail closed requires a credential-bootstrap change and would break the
  single-user H2 file setup. Deferred (see `backlog.md`).
- **Owner-only directory scope.** For `jdbc:h2:file:./maindb` the "database
  directory" is the process working directory, which is set to `0700`. A
  dedicated data directory is recommended and tracked in `backlog.md` /
  `add-installation`.
- **H2 auxiliary files** (`.lock.db`, `.trace.db`) are not chmod'd.
- **Throttle** is in-memory, per-instance, resets on restart; with proxy trust
  enabled it keys on a forwarded value. Documented for edge-level rate limiting.
- **Edge-terminated TLS** currently requires `allow-plain-http=true`, which also
  disables the guard. A distinct edge-TLS acknowledgment is deferred.
- Client-supplied `checksum` is stored/echoed unvalidated beyond length (by
  design: the server cannot compute the client's keyed HMAC).

### Verification
- `./gradlew clean build` — BUILD SUCCESSFUL.
- Runtime: `/check` ok, bad token `401`, 5 failures → `429`, `X-Content-Type-Options`/`Cache-Control` present, checksum echo + SHA-256 fallback, DB dir `0700` / file `0600`, token absent from logs.
