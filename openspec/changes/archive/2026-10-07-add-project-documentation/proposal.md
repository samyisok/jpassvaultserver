# Proposal

## Why

The repository has a thin, partly stale README: it references a jar name that no longer exists, contains command typos, and does not describe the API, configuration, security model, backup, or upgrade paths. There is no API reference, no contributor guide, and no changelog, so clients, operators, and future contributors must reverse-engineer the contract from the code. For a service that stores encrypted vaults, the contract — endpoints, headers, fields, configuration, and security expectations — must be explicit and kept in sync with the implementation.

## What Changes

- Rewrite the README: purpose, architecture overview, requirements, build, run, configuration, and pointers to the API reference and operations guide.
- Add an OpenAPI 3.1 specification for the HTTP API (`GET /check`, `POST /files`, `GET /files/last`, `GET /files/last/checksum`) including the `token` header, the JSON fields, and the error responses (`401`, `413`, `429`).
- Add a contributor/agent guide (`AGENTS.md`) describing the build, test, verification, architecture, and project rules (testing style, file length, TDD expectations).
- Add a `CHANGELOG.md` following Keep a Changelog, updated for each release and consumed by the release workflow.
- Add an operations guide (runbook): configuration reference, backup/restore, upgrade/rollback, and troubleshooting (shared with `add-installation`).
- Establish a rule that documentation is updated in the same change as the behavior it describes, and checked at release time.

## Capabilities

### New Capabilities

- `project-documentation`: the documentation the project maintains and keeps accurate — README, API reference, contributor guide, changelog, and operations runbook — and the expectation that it matches the implemented behavior.

### Modified Capabilities

None. `server-installation` defines how the service is deployed; this change defines the documentation set that describes it.

## Impact

- New files: `docs/openapi.yaml` (or equivalent), `docs/operations.md`, `AGENTS.md`, `CHANGELOG.md`.
- README: rewritten installation, configuration, and API sections.
- Release: `add-release-packaging` reads release notes from `CHANGELOG.md`.
- No code or API behavior change.
