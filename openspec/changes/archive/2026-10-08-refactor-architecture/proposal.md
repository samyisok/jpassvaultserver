# Proposal

## Why

Two code-quality items from the backlog are approved for work. `domains/File` is a JPA persistence entity sitting in a package named `domains`, and `FileController` mixes HTTP handling with business rules (payload-size validation, "keep only the newest payload" retention) and calls the repository directly. Both make the code harder to change safely. Separately, `deploy/install.sh` and the systemd unit were only verified statically; the installation procedure has never been exercised end to end.

This change is an internal refactor plus a verification of the existing install path. It changes no externally observable behavior: the HTTP contract, status codes, JSON fields, and database schema stay exactly the same.

## What Changes

- Rename the `domains` package to `persistence` (it only holds a JPA entity and a Spring Data repository) and move the use-case error `FileNotFoundException` to a new `services` package.
- Add a thin application-service layer, `services/VaultService`, that owns payload validation, the retention policy, the checksum selection, and the "last payload" lookup; make `store` transactional.
- Slim `FileController` to HTTP binding and response shaping; it depends only on `VaultService` (no repository, `AppProperties`, or `Crypto`).
- Keep the JPA entity as the request/response model (no new DTOs or mappers).
- Add a real-database test for the retention rule.
- Verify `deploy/install.sh` end to end using Docker (systemd-capable container) rather than only static checks; fix any issues found.
- Update `AGENTS.md` (architecture section) and remove the completed backlog items.

No new features, no API change, no schema change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This is a pure internal refactor plus a verification of existing behavior; the change sets `skip_specs: true` because no spec-level behavior changes.

## Impact

- Code: `domains/*` → `persistence/*`; `FileNotFoundException` → `services/`; new `services/VaultService`; `controllers/FileController` slimmed; `advices/FileNotFoundAdvice` import updated.
- Tests: controller logic tests become `VaultService` unit tests; thin controller delegation tests; retention integration test; package moves.
- Ops: `deploy/install.sh` verified in a container (no behavior change unless a defect is found).
- Docs: `AGENTS.md` architecture section; `CHANGELOG.md`; `backlog.md`.
