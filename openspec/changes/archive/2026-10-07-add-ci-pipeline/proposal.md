# Proposal

## Why

The current CI is a single GitHub Actions workflow that builds with Gradle and generates a coverage badge, but it relies on retired actions (`checkout@v2`, `setup-java@v2`, AdoptOpenJDK), has no test/coverage gate, no static analysis, no dependency vulnerability scanning, no caching, and no least-privilege permissions. It also pushes a generated badge commit to `main` from CI. As a service that stores encrypted vaults, the project needs a trustworthy verification pipeline: every change is built, tested, coverage-checked, statically analyzed, and scanned for vulnerable dependencies before it can merge, with a release path separated from the PR path.

## What Changes

- Replace the retired actions with `actions/checkout@v4` and `actions/setup-java@v4` (Temurin) on JDK 25, with a Gradle dependency cache.
- Split the pipeline into a pull-request gate (build, test, coverage, static analysis, dependency scan) and a release workflow triggered by version tags (owned jointly with `add-release-packaging`).
- Enforce a minimum line and branch coverage threshold via the existing JaCoCo report, failing the build below it.
- Add static analysis as a required check (checkstyle/spotbugs or an equivalent), and fail on violations.
- Add dependency vulnerability scanning (OWASP dependency-check and/or GitHub Dependabot + CodeQL) as a required check.
- Archive test and coverage reports as workflow artifacts so failures are diagnosable from the run.
- Set least-privilege workflow permissions (`contents: read` by default) and never push to protected branches from CI; publish coverage as an artifact/badge instead of committing to `main`.
- Add a `concurrency` group so superseded runs are cancelled.
- Document the required branch-protection status checks.

## Capabilities

### New Capabilities

- `ci-pipeline`: the verification gates the project runs on every change and release — build, test, coverage threshold, static analysis, dependency scanning, artifact retention, and least-privilege workflow behavior.

### Modified Capabilities

None. `server-runtime` defines the toolchain; this change defines the pipeline that enforces quality on it.

## Impact

- CI: `.github/workflows/gradle.yml` is reworked, and a release workflow is introduced (with `add-release-packaging`).
- Build: `build.gradle` gains JaCoCo coverage-verification rules and static-analysis plugins; Dependabot configuration is added.
- Repository settings (outside the code): branch protection gains required status checks.
- Badges: `.github/badges/*` are no longer committed by CI; coverage is exposed as an artifact or an external badge.
- No runtime behavior change; no API change.
