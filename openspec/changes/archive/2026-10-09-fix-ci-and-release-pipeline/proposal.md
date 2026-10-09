# Proposal

## Why

The first real run of the pipelines (after pushing `main` and tag `v2.1.0`) failed on two defects:

- The **Release** workflow fails at "Build and push container image": it sets `provenance: true` but never sets up Buildx, so the default builder cannot produce attestations (`buildx failed … attestations`). No release was published.
- The **CI** workflow fails only at "Dependency vulnerability scan": the OWASP dependency-check action requires an NVD API key (dependency-check ≥ 9), which is not configured, so the step errors and the whole CI job goes red even though the build, tests, coverage, Checkstyle, OpenAPI lint, and installer verification all pass.

## What Changes

- **Release:** add `docker/setup-buildx-action` before the image build so the provenance attestation the workflow requests can actually be produced.
- **CI:** make the OWASP dependency scan run only when an `NVD_API_KEY` secret is configured, and skip it otherwise. Vulnerability detection without a key is provided by Dependabot (already configured). The spec's dependency-scan requirement is updated to match this reality.
- No application code changes; no API or behavior change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ci-pipeline`: the "Dependencies are scanned for known vulnerabilities" requirement becomes: Dependabot monitors vulnerabilities and proposes updates; when an NVD API key is configured, CI additionally runs the OWASP scan and fails on high-severity findings. A scenario covers the no-key skip.

## Impact

- `.github/workflows/release.yml`: add the pinned `docker/setup-buildx-action` step.
- `.github/workflows/gradle.yml`: gate the dependency-scan step on `secrets.NVD_API_KEY` and pass it to the action.
- `docs/ci.md`: note that the OWASP scan requires the `NVD_API_KEY` secret.
- No source, schema, or contract change.
