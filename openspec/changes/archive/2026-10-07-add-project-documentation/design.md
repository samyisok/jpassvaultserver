# Design

## Context

- See proposal.md — Why. The current README is the only documentation; it is partly wrong (jar name `jpassvaultserver-1.0.0.jar`, `systemctl enable example.service`) and silent on the API, configuration, and security model. The sibling jpassvault project maintains an `AGENTS.md`, a `CHANGELOG.md`, and OpenSpec specs that this project can mirror.
- Constraint: docs must describe behavior that the other changes (`modernize-java-stack`, `harden-server-security`, `add-release-packaging`, `add-installation`) define; sequencing matters, so this change is best applied after (or alongside) those.
- Constraint: the API field names are part of the client contract and must be documented exactly.

## Goals / Non-Goals

**Goals:**

- One accurate entry point (README) and one authoritative API description (OpenAPI).
- A changelog that the release pipeline can consume.
- A contributor guide that encodes the project's build/test/verification rules.
- An operations runbook for real deployments.

**Non-Goals:**

- A documentation site generator or hosted docs.
- Rewriting the client (jpassvault) documentation.
- Changing any behavior — documentation only.

## Decisions

### D1: README as the entry point, docs/ for depth

README gains: what the service is, the API summary, requirements, quick start (jar and container), configuration table (linking to the operations guide), and links to `docs/openapi.yaml`, `docs/operations.md`, `AGENTS.md`, and `CHANGELOG.md`. Deep content lives under `docs/`.

### D2: OpenAPI 3.1 from the actual contract

Author `docs/openapi.yaml` by inspecting `FileController` and `AuthKeyFilter`, using the Zarando-style conventions the project already favors in the sibling repo (kebab-case paths where applicable, `problem+json` for errors is optional here because the current error bodies are plain text — the spec must document the *actual* bodies rather than invent a new format). Paths, `token` header, `file` request field, and `hash`/`check` response fields are documented exactly as implemented. The spec is a description, not a driver, unless a later change adopts code generation.

**Decision:** do not silently change the error shape; if a future change adopts RFC 9457, it updates both code and spec together.

### D3: `AGENTS.md` mirrors the sibling project

Describe build/run/test commands (`./gradlew`), the verification gates (`./gradlew check`), the architecture (controller → repository → H2, filter-based auth), the project rules (tests first, JUnit 5, Mockito, `UnitTest`/`IntegrationTest` suffixes, ≤350 lines per file, Javadoc/comment conventions), and gotchas (`ddl-auto=update`, file database, single instance).

### D4: `CHANGELOG.md` follows Keep a Changelog

Sections per version with `Added`/`Changed`/`Fixed`/`Security`/`Removed`. The release workflow extracts the matching version section as the release body (shared with `add-release-packaging`). Seeded with the changes currently in flight, using the archive convention when OpenSpec changes are archived.

### D5: Operations runbook

`docs/operations.md` covers the configuration reference, TLS/proxy setup, backup/restore, upgrade/rollback, health/readiness, log locations, and troubleshooting. It is the canonical home for what `add-installation` needs; the README links to it.

### D6: Documentation freshness rule

A change that alters endpoints, fields, configuration, or deployment SHALL update the relevant document in the same change; the release workflow's changelog check plus a documentation review in PRs enforce this. Recorded as a requirement so it is testable at review time.

## Risks / Trade-offs

- [Docs drift from behavior] → D6 rule plus OpenAPI kept next to handlers; review is part of the PR checklist.
- [OpenAPI becomes a maintenance burden] → keep it a description of the four endpoints; do not generate code.
- [Documentation-only change has no runtime spec] → the `project-documentation` capability defines observable documentation obligations (files exist, API matches, changelog sections present), so the change is not a zero-delta change.
- [Sequencing with other changes] → apply after the behavior changes land, or update docs as those changes land; the tasks call this out.

## Migration Plan

1. Draft the OpenAPI from the current handlers and the target (post-security) behavior.
2. Rewrite the README and add `docs/operations.md`, `AGENTS.md`, and `CHANGELOG.md`.
3. Wire the release workflow to read the changelog (with `add-release-packaging`).
4. Add a PR checklist item and branch-protection note about documentation.

Rollback: documentation-only; revert the files.

## Open Questions

- Whether to publish the OpenAPI to an external hub (for example SwaggerHub) or keep it repository-local.
- Whether to adopt RFC 9457 `problem+json` errors (would require a code change and a spec-touching change; out of scope here).
- Whether the changelog is seeded from OpenSpec archives automatically or maintained by hand.
