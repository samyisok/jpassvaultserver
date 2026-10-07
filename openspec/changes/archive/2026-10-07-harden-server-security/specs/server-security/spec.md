# Spec Delta

## Purpose

Protects the sync service and the vault payloads it stores: the API credential is never committed or logged and is compared in constant time; transport is encrypted by default; requests are bounded; the change-detection value is not an unkeyed hash; stored data is owner-only; and diagnostics and error responses expose no secrets.

## ADDED Requirements

### Requirement: The API secret is external and startup fails closed

The server SHALL obtain its API secret from the environment or an external configuration source and SHALL NOT ship a secret value in the repository or in the packaged `application.properties`. On startup the server SHALL refuse to run when no secret is configured or when the configured value equals the previously committed placeholder.

#### Scenario: Started without a configured secret

- **WHEN** the server starts with no secret configured
- **THEN** startup fails with a clear error explaining that the secret must be supplied

#### Scenario: Started with the old committed placeholder

- **WHEN** the server starts with the formerly committed placeholder value as its secret
- **THEN** startup fails rather than accepting a publicly known credential

#### Scenario: Repository contains no secret value

- **WHEN** the working tree is inspected
- **THEN** no real API secret value is present in tracked configuration files

### Requirement: Token comparison is constant time

The server SHALL compare the presented token with the configured secret using a constant-time comparison that does not depend on the length of the matching prefix.

#### Scenario: Correct token is accepted

- **WHEN** a request presents a token equal to the configured secret
- **THEN** the request is allowed

#### Scenario: Incorrect token is rejected

- **WHEN** a request presents a token that differs from the configured secret in any position
- **THEN** the request is rejected with HTTP `401`

### Requirement: Credentials are never written to logs

Logs produced by the server SHALL NOT contain the configured secret or the submitted token value. A rejected authentication SHALL be logged as an event without the credential.

#### Scenario: Invalid token is not logged

- **WHEN** a request presents an invalid token
- **THEN** the log records the rejection but does not contain the submitted token value

#### Scenario: Startup and diagnostics do not print the secret

- **WHEN** configuration objects are rendered as diagnostic text
- **THEN** the secret is masked and its plain value does not appear

### Requirement: Client address is taken from a trusted source

The client address recorded in logs SHALL be the connection address, and proxy-supplied headers (`X-Forwarded-For`/`Forwarded`) SHALL be honored only when the deployment explicitly enables proxy trust.

#### Scenario: Proxy headers ignored by default

- **WHEN** a request arrives with a forged `X-Forwarded-For` header and proxy trust is disabled
- **THEN** the logged address is the connection's remote address, not the header value

#### Scenario: Proxy headers honored when enabled

- **WHEN** proxy trust is enabled because the server is behind a known reverse proxy
- **THEN** the logged address reflects the forwarded client address

### Requirement: Encrypted transport by default

The deployed server SHALL serve requests over TLS, either terminated at a reverse proxy or configured in-process, and SHALL refuse ordinary cleartext HTTP unless plain HTTP is explicitly acknowledged for development.

#### Scenario: Cleartext refused in production configuration

- **WHEN** the server is configured for production without TLS and without the explicit plain-HTTP acknowledgment
- **THEN** startup fails with a clear error

#### Scenario: Explicit development opt-in allows plain HTTP

- **WHEN** the plain-HTTP acknowledgment is set for local development
- **THEN** the server starts and serves requests over HTTP

### Requirement: Request payloads are bounded

The server SHALL enforce a maximum size for uploaded vault payloads and SHALL reject oversized requests with a clear error instead of reading them into memory.

#### Scenario: Oversized upload is rejected

- **WHEN** a client posts a payload larger than the configured maximum
- **THEN** the server rejects the request with HTTP `413` and does not store it

#### Scenario: Normal upload is accepted

- **WHEN** a client posts a payload within the configured maximum
- **THEN** the payload is stored and returned by `GET /files/last`

### Requirement: Change detection uses a non-MD5 value

The value returned for changed-content detection SHALL NOT be an unkeyed MD5. The server SHALL store a client-supplied integrity value and return it verbatim, and SHALL use a SHA-256 fallback only when no client value was supplied. The response JSON field name SHALL remain `hash`.

#### Scenario: Client-supplied value is echoed

- **WHEN** a client uploads a payload together with an integrity value and later requests the checksum
- **THEN** the server returns that exact value in the `hash` field

#### Scenario: Fallback is a SHA-256 value

- **WHEN** the newest stored payload has no client-supplied integrity value
- **THEN** the server returns a deterministic SHA-256 hex value

#### Scenario: MD5 is not used

- **WHEN** the server computes any integrity value
- **THEN** MD5 is not part of that computation

### Requirement: Stored data is owner-only

On systems that support POSIX permissions, the database directory SHALL be owner-only (`0700`) and the database file SHALL be owner-only (`0600`). Blank or default database credentials SHALL be rejected.

#### Scenario: Database directory is owner-only

- **WHEN** the server creates or opens its database directory on a POSIX system
- **THEN** the directory permissions are `0700`

#### Scenario: Database file is owner-only

- **WHEN** the database file exists on a POSIX system
- **THEN** its permissions are `0600`

### Requirement: Error responses and headers do not leak internals

API responses SHALL NOT contain stack traces or internal messages, and SHALL set `X-Content-Type-Options: nosniff` and `Cache-Control: no-store`. Secrets SHALL NOT appear in any response body.

#### Scenario: Unexpected error hides internals

- **WHEN** an internal error occurs while handling an API request
- **THEN** the response contains no stack trace and no internal class or path details

#### Scenario: API responses are not cached

- **WHEN** an API response is produced
- **THEN** it carries `Cache-Control: no-store` and `X-Content-Type-Options: nosniff`

### Requirement: Repeated authentication failures are throttled

The server SHALL limit repeated failed authentication attempts from the same source, answering further attempts with HTTP `429` once a threshold is exceeded within a time window.

#### Scenario: Brute force is throttled

- **WHEN** a source submits many invalid tokens in quick succession
- **THEN** further attempts from that source are rejected with HTTP `429`

#### Scenario: Threshold resets over time

- **WHEN** a source stops failing and the window elapses
- **THEN** that source is allowed to authenticate again
