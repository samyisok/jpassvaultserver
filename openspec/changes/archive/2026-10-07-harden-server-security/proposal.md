# Proposal

## Why

A review of the sync server found several weaknesses that put stored vaults and the API token at risk. A working secret key is committed to `src/main/resources/application.properties` (`app-properties.secret-key=555505424923a833fe77cfa68c497bf2`); the bearer token is compared with `String.equals`, giving a timing side channel; the invalid `token` value is written to the log in clear text; the client IP is read from an unconditionally trusted `X-FORWARDED-FOR` header; the service binds `0.0.0.0` with no TLS requirement; the sync checksum is an unkeyed MD5; the H2 database and its directory are created with default permissions; request bodies are unbounded; and errors can expose internals. These must be fixed before the service is trusted with real vaults.

## What Changes

- **BREAKING**: remove the committed secret from `application.properties`; the server SHALL refuse to start unless a secret is supplied through the environment (or an external config), and SHALL reject the old committed value as a placeholder.
- Compare the presented token to the configured secret in constant time, independent of the matching prefix length.
- Never write the presented token to logs; log only that an invalid credential was presented, with a redacted marker.
- Derive the client IP for logs from the connection, and trust proxy headers (`X-Forwarded-For`/`Forwarded`) only when explicitly enabled for a known reverse proxy.
- Require encrypted transport: the deployed server SHALL serve over TLS (terminated at the edge or via `server.ssl.*`), and SHALL refuse ordinary HTTP requests unless explicitly opted in for local development.
- Bound the size of an uploaded vault payload and reject oversized requests with a clear error rather than reading them into memory.
- Replace the unkeyed MD5 checksum returned by `GET /files/last/checksum` with a client-supplied integrity value that the server stores and returns verbatim; only when a client supplies none does the server compute a SHA-256 fallback. MD5 SHALL NOT be used.
- Create the H2 database directory and file with owner-only permissions on POSIX systems, and disallow blank or default database credentials.
- Keep secrets out of diagnostic output (`AppProperties.toString`) and out of error responses; disable stack traces and the server banner in responses.
- Fail fast on unsafe configuration (missing secret, default secret, missing TLS in a non-development profile).
- **Out of scope:** changing the HTTP paths, headers, or JSON field names; multi-user accounts/RBAC; a WAF; idle session management (there are no sessions); moving off the H2 file store.

## Capabilities

### New Capabilities

- `server-security`: how the sync service protects the vault payloads and its own credential — credential handling and comparison, transport protection, request bounds, integrity-value handling, data-at-rest permissions, and diagnostic/error hygiene.

### Modified Capabilities

None. `server-runtime` covers the build/runtime baseline and API shapes; it does not define security behavior.

## Impact

- Code: `auth/AuthCheck` (constant-time comparison, fail-fast configuration), `auth/AuthKeyFilter` (no token in logs, trusted-proxy client IP, bounded/blocked bodies), `controllers/FileController` (no MD5, client-supplied checksum, payload bound), `AppProperties` (secret sourcing, masked `toString`), `application.properties` (secret removed, safe defaults), `LoadDatabase`/startup (database directory permissions).
- API: paths, headers, and JSON field names unchanged. `GET /files/last/checksum` now returns the client-supplied value (or a SHA-256 fallback) instead of an MD5 of the stored payload. The jpassvault client already computes a keyed HMAC-SHA256 for change detection, so it must upload that value in the `POST /files` payload for exact matching; until it does, the server's fallback differs from the client value and sync degrades to always-upload rather than corrupting data. This cross-repo follow-up is called out as a compatibility note and an Open Question.
- Data: existing `maindb.mv.db` files and stored payloads are unchanged; permissions are tightened on the next create/start.
- Configuration: deployments must now supply `JPASSVAULT_SECRET`; the committed default stops working.
- Tests: new tests cover constant-time comparison, missing/default-secret startup failure, token redaction, proxy-header trust, payload bounds, checksum handling, and file permissions.
