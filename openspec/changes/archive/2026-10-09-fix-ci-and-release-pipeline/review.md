# Review

## Review — 2026-10-09

**Scope:** CI/release workflow configuration only (`release.yml`, `gradle.yml`, `docs/ci.md`, `AGENTS.md`). No application code.

### Findings fixed
- Release: missing `docker/setup-buildx-action` before the image build caused the
  `provenance: true` attestation to fail (`buildx failed … attestations`). Buildx
  is now set up (pinned SHA).
- CI: the OWASP dependency scan required an NVD API key and failed the whole job
  without one. It is now gated on the `NVD_API_KEY` secret; Dependabot provides
  alerting when no key is set. The `ci-pipeline` spec requirement was updated to
  match.

### DDD / GRASP / readability
- Not applicable: no Java code changed. YAML is valid; steps are named and
  comments explain the gating and pinning.

### Verification
- `openspec validate --strict fix-ci-and-release-pipeline`: valid.
- All workflow files parse as valid YAML.
- The authoritative check is the next real run: push, then confirm CI is green
  and the Release workflow publishes the image.

### Residual
- Action versions emit Node 20 deprecation warnings (checkout, setup-java,
  upload-artifact, docker/*); warnings only, Dependabot proposes newer majors.
- Vulnerability detection is not build-blocking until the operator adds an
  `NVD_API_KEY` secret (documented in `docs/ci.md`).
