# Tasks

## 1. API reference

- [x] 1.1 Enumerate the implemented endpoints, the `token` header, request/response fields, and error codes
- [x] 1.2 Author `docs/openapi.yaml` (OpenAPI 3.1) covering `GET /check`, `POST /files`, `GET /files/last`, `GET /files/last/checksum`, and `401`/`404`/`411`/`413`/`429` — parses and covers all endpoints

## 2. README rewrite

- [x] 2.1 Rewrite the README: purpose, architecture, requirements, build/run, configuration summary, API summary, and links — done across this change and `add-installation`
- [x] 2.2 Fix the stale jar name and `systemctl` command typos — corrected

## 3. Contributor and agent guide

- [x] 3.1 Add `AGENTS.md` describing build/test commands, verification gates, architecture, and project rules — includes the ≤350-line rule and the Mockito/Spring 7 note

## 4. Changelog

- [x] 4.1 Add `CHANGELOG.md` (Keep a Changelog) seeded with the in-flight changes — present
- [x] 4.2 Confirm the release workflow can extract a version section — verified (missing section → minimal note)

## 5. Operations runbook

- [x] 5.1 Add `docs/operations.md` covering configuration, TLS/proxy, backup/restore, upgrade/rollback, health, and troubleshooting — done in `add-installation`

## 6. Freshness rule

- [x] 6.1 Document the documentation-updated-in-the-same-change expectation — `AGENTS.md` "Documentation" section
- [x] 6.2 Verify the docs match the post-change behavior of `modernize-java-stack`, `harden-server-security`, and `add-installation` — OpenAPI/operations/README cross-checked against the code

## 7. Verification

- [x] 7.1 Validate the OpenAPI file and check all README links — both pass
- [x] 7.2 Run `openspec validate --strict` for this change — passes
