# Tasks

## 1. Dedicated persistent data directory

- [x] 1.1 Default `spring.datasource.url` → `jdbc:h2:file:./data/maindb`; `/data/` added to `.gitignore`
- [x] 1.2 `DatabasePermissions.ensureOwnerOnlyDirectory` and `dataDirectoryFor` added
- [x] 1.3 The data directory is created owner-only in `main` before `SpringApplication.run`, resolving the URL with Spring precedence (args → system property → env → default)
- [x] 1.4 The process working directory is no longer chmod'd; only the data directory and the database files are tightened
- [x] 1.5 Tests: directory creation/permissions, data-directory resolution, and URL resolution

## 2. Database credentials (spec change)

- [x] 2.1 Removed `validateCredentials` and its call; `DatabasePermissionsUnitTest` updated
- [x] 2.2 `server-security` spec delta drops the credential requirement and keeps the dedicated owner-only directory

## 3. Proxy-terminated TLS

- [x] 3.1 `app-properties.tls-terminated-at-proxy` added and accepted by `validateTransport`
- [x] 3.2 Test: TLS, proxy acknowledgment, and development plain-HTTP all pass; none fails
- [x] 3.3 Documented in `docs/operations.md`, the env example, the systemd unit, and the `Dockerfile`

## 4. Deterministic filter order

- [x] 4.1 `@Order`: `SecurityHeadersFilter` (1), `AuthKeyFilter` (2), `RequestSizeLimitFilter` (3)
- [x] 4.2 Test: an oversized request with an invalid token is `401`

## 5. Web-layer tests

- [x] 5.1 MockMvc `@SpringBootTest` covering `200`/`401`/`400`/`413`/`429` and ordering through the real chain

## 6. `File` identity

- [x] 6.1 `equals`/`hashCode` use the persistent id; tests updated

## 7. CI and release

- [x] 7.1 All third-party GitHub Actions pinned to full commit SHAs with `# vX` comments
- [x] 7.2 OpenAPI lint step (`@redocly/cli`) added; `docs/openapi.yaml` fixed and passing
- [x] 7.3 `release.yml` tags the image `:latest` only for stable (non-pre-release) versions

## 8. Documentation

- [x] 8.1 `README.md`, `docs/operations.md`, `docs/ci.md`, `docs/openapi.yaml`, `deploy/*` updated for the data directory and flags

## 9. Verification

- [x] 9.1 `./gradlew clean build` green
- [x] 9.2 Runtime: default `./data` created owner-only and the record survives a restart
- [x] 9.3 `openspec validate --strict` for this change — valid
