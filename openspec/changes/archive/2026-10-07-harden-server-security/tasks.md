# Tasks

## 1. Secret handling

- [x] 1.1 Write failing test `AppPropertiesUnitTest`: startup/validation rejects a missing secret and the formerly committed placeholder; `toString` masks the secret
- [x] 1.2 Remove `app-properties.secret-key` from `application.properties`; default `use-secret-key-from-env` to `true`; add fail-fast validation for missing/placeholder secret and mask `AppProperties.toString`

## 2. Constant-time credential comparison

- [x] 2.1 Write failing test `AuthCheckConstantTimeUnitTest`: correct token accepted, any differing token rejected
- [x] 2.2 Implement constant-time comparison in `AuthCheck.verify` (via `security/Crypto.constantTimeEquals`, fixed-length digest comparison)

## 3. Log hygiene

- [x] 3.1 Write failing test `AuthKeyFilterLogSecretUnitTest`: an invalid token never appears in log output
- [x] 3.2 Stop interpolating the token in `AuthKeyFilter`; log a constant event plus the derived address

## 4. Trusted client address

- [x] 4.1 Write failing test `AuthKeyFilterLogIpUnitTest`: forged `X-Forwarded-For` is ignored when proxy trust is disabled and honored when enabled
- [x] 4.2 Add the `trust-proxy-headers` flag and derive the address accordingly — uses the right-most forwarded hop (review fix; a client can forge the left-most)

## 5. Transport and fail-fast configuration

- [x] 5.1 Write failing test `TransportConfigurationUnitTest`: config without TLS and without the plain-HTTP acknowledgment fails startup
- [x] 5.2 Add TLS/plain-HTTP configuration validation and the development escape hatch — detect TLS only from `server.ssl.key-store`/`certificate` with `server.ssl.enabled != false` (review fix; `key-store-type` alone no longer counts)

## 6. Bounded payloads

- [x] 6.1 Write failing test `FileControllerPayloadSizeUnitTest`: an oversized upload is rejected with `413` and not stored
- [x] 6.2 Enforce the maximum payload size in `RequestSizeLimitFilter` and in `POST /files`; reject a body of unknown length (`411`) so a chunked upload cannot bypass the bound (review fix)

## 7. Integrity value

- [x] 7.1 Write failing test `FileControllerChecksumUnitTest`: a client-supplied value is stored and echoed; a missing value yields a SHA-256 fallback; MD5 is absent
- [x] 7.2 Add the optional `checksum` column, store it on upload, return it verbatim, compute a SHA-256 fallback, and bound the checksum length; remove MD5 (`grep -rn "MD5" src/main` empty)

## 8. Data-at-rest permissions

- [x] 8.1 Write failing test `DatabasePermissionsUnitTest`: the database directory is `0700` and the file `0600` on POSIX
- [x] 8.2 Apply owner-only permissions at startup — explicit blank/default credentials are rejected; the H2 implicit default (`sa`/empty) remains accepted when no credentials are configured (see review.md residual)

## 9. Error and header hygiene

- [x] 9.1 Write failing test `ErrorHygieneUnitTest`: API responses carry `Cache-Control: no-store` and `X-Content-Type-Options: nosniff`, and an internal error leaks no stack trace
- [x] 9.2 Configure the error handling and response headers — also applied on ERROR dispatch (review fix)

## 10. Authentication throttling

- [x] 10.1 Write failing test `AuthThrottleUnitTest`: repeated failures from one source return `429`, and the threshold resets after the window
- [x] 10.2 Implement the bounded in-memory failure counter — reads/writes synchronized (review fix)

## 11. Verification

- [x] 11.1 Run the full build and test suite — `./gradlew clean build` BUILD SUCCESSFUL (75+ tests)
- [x] 11.2 Confirm the API contract (paths/headers/JSON field names) is unchanged — verified against the running jar: `/check` `{"check":"ok"}`, `401 Invalid API KEY`, checksum echo + SHA-256 fallback, `429` after repeated failures
- [x] 11.3 Run `openspec validate --strict` for this change — passes

## 12. Review follow-ups (java-cop + security reviewer)

- [x] Fix TLS detection so `key-store-type` alone does not satisfy the guard (`AppProperties.isTlsConfigured`)
- [x] Use the right-most forwarded hop instead of the left-most (`AuthKeyFilter`)
- [x] Reject bodies of unknown length (`411`) so chunked uploads cannot bypass the payload bound (`RequestSizeLimitFilter`)
- [x] Apply security headers on ERROR dispatch (`SecurityHeadersFilter.shouldNotFilterErrorDispatch`)
- [x] Bound the stored `checksum` length and guard a null `maxPayloadSize` (`FileController`, `AppProperties`)
- [x] Synchronize throttle reads (`AuthThrottle.isBlocked`)
- [x] Use `HttpStatus.CONTENT_TOO_LARGE` (the Spring 7 non-deprecated 413 constant)
- [x] Remove the vacuous MD5 assertion in `FileControllerChecksumTest`
