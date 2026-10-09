# Design

## Context

- First real pipeline run (commit `738fa99`, tag `v2.1.0`):
  - `Release` failed at step "Build and push container image" with `buildx failed with: … attestations`. The workflow sets `provenance: true` on `docker/build-push-action` but never runs `docker/setup-buildx-action`, so the default `docker` driver is used and attestations are unsupported.
  - `CI` failed only at "Dependency vulnerability scan". OWASP dependency-check requires an NVD API key since version 9; without it the update fails and the step errors. Every other step passed.
- Constraints: no NVD API key is available as a repository secret; the project already has Dependabot configured; keep least privilege.

## Goals / Non-Goals

**Goals:**

- A green CI pipeline without requiring a secret to be provisioned first.
- A Release workflow that can actually publish the image (with the requested provenance).
- Keep vulnerability detection meaningful.

**Non-Goals:**

- Provisioning an NVD API key (the operator can add one later; the scan then activates automatically).
- Changing application code or the HTTP/data contract.

## Decisions

### D1: Set up Buildx before the image build

Add `docker/setup-buildx-action` (pinned to a commit SHA) before `docker/build-push-action`. This provides a builder that supports the `provenance: true` attestation the workflow requests. Alternative: drop `provenance: true` — rejected because the review specifically wanted provenance; setting up Buildx is the correct fix.

### D2: Gate the OWASP scan on the NVD API key

Run the dependency-check action only when `secrets.NVD_API_KEY` is non-empty, and pass the key via `args: --nvdApiKey …`. Without a key the step is skipped, so CI is green and Dependabot still surfaces vulnerabilities. Alternative: replace the action with `gradle/actions/dependency-submission` — rejected for now because it requires `contents: write`, which conflicts with the least-privilege rule; Dependabot already provides alerting.

### D3: Make the spec match the mechanism

Update `ci-pipeline`'s dependency requirement: Dependabot monitors and proposes; the OWASP scan is conditional on the NVD key and fails on high severity when it runs. This keeps the spec honest rather than asserting a scan that cannot run.

## Risks / Trade-offs

- [Vulnerabilities are not build-blocking until an NVD key is configured] → Dependabot alerts still surface them; the operator can enable the gate by adding the secret. Documented.
- [Action versions emit Node 20 deprecation warnings] → warnings only; Dependabot proposes newer majors. Not addressed here.
- [Buildx adds a step] → negligible; it is the standard prerequisite for build-push attestations.

## Migration Plan

1. Add `docker/setup-buildx-action` to `release.yml`.
2. Gate the dependency-scan step in `gradle.yml` on `secrets.NVD_API_KEY`.
3. Update `docs/ci.md` and the `ci-pipeline` spec delta.
4. Push; re-run CI and a release to confirm green and that the image publishes.

Rollback: revert the workflow edits; no data impact.

## Open Questions

- Whether to obtain an NVD API key (free from NIST) and add it as a repository secret to re-enable the build-failing scan. Deferred to the operator.
