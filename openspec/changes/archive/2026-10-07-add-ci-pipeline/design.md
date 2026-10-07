# Design

## Context

- Current `.github/workflows/gradle.yml` runs on `push`/`pull_request` to `main` with `checkout@v2` and `setup-java@v2` (AdoptOpenJDK, JDK 11), runs `./gradlew build`, `test`, and `jacocoTestReport`, generates a badge with `cicirello/jacoco-badge-generator@v2`, and commits the regenerated badge to the branch. There is no threshold, no static analysis, no dependency scan, and no cache.
- `build.gradle` already applies the `jacoco` plugin and enables CSV reports. Tests run on `useJUnitPlatform()`.
- Constraint: the project is Apache-licensed and public; scanning must not require private infrastructure or paid runners.
- Constraint: a failed gate must block merge; a separate tag-triggered workflow publishes releases (shared with `add-release-packaging`).

## Goals / Non-Goals

**Goals:**

- Every change is built, tested, and measured on JDK 25 with a fixed toolchain.
- Coverage below threshold, static-analysis violations, and known-vulnerable dependencies fail the change.
- CI has least privilege and does not write to protected branches.
- Release publishing is separate and tag-driven.

**Non-Goals:**

- Deployment of the built service (installation change).
- Building/publishing container images (packaging change).
- Rewriting the test suite to raise its coverage number (the threshold is set to a realistic minimum and may be raised over time).

## Decisions

### D1: One verification workflow, triggered on PR and `main`

Keep a single `ci` workflow on `push`/`pull_request` to `main`. It runs: checkout → JDK 25 (Temurin) with Gradle cache → `./gradlew build` → `./gradlew jacocoTestReport jacocoTestCoverageVerification` → static analysis → dependency scan → upload reports. A `concurrency` group cancels superseded runs.

### D2: Coverage threshold from the existing JaCoCo setup

Add a `jacocoTestCoverageVerification` rule with a minimum line and branch ratio, wired so `./gradlew check` fails below it. The initial threshold is set slightly below the current measured coverage to avoid an immediate red build, then raised as coverage improves.

**Alternative:** an external coverage service — rejected; the project already produces JaCoCo data and should not require a third-party account.

### D3: Static analysis as a required gate

Apply a lightweight static analyzer (Checkstyle and/or SpotBugs) with a checked-in configuration, bound to the `check` task. Start with a pragmatic rule set (no unused imports, no obvious bug patterns) to avoid a wall of pre-existing violations; tighten over time.

**Alternative:** no static analysis — rejected; the service handles secrets and the cost of a bug is high.

### D4: Dependency vulnerability scanning

Add OWASP dependency-check (Gradle plugin, run in CI) and a `.github/dependabot.yml` for Gradle and GitHub Actions updates. A high-severity finding fails the build; a documented allowlist handles false positives. CodeQL for Java is enabled as a GitHub-native check.

**Rationale:** EOL dependencies are the reason for `modernize-java-stack`; ongoing scanning keeps the upgrade from regressing.

### D5: Least privilege; no CI commits to protected branches

Set top-level `permissions: contents: read`. Coverate is uploaded as a workflow artifact; if a badge is wanted, use an external badge endpoint or a dedicated `gh-pages`/artifact flow, not an auto-commit to `main`. Remove the auto-commit badge step.

**Rationale:** CI writing to `main` couples the pipeline to branch protection and lets a compromised action alter the default branch.

### D6: Release workflow triggered by tags

A separate workflow runs on `v*` tags, verifies the tag matches the project version, builds the release artifact(s), and publishes them. The artifact and image specifics live in `add-release-packaging`; this change defines the trigger, the version check, and the separation from PR gating.

### D7: Reports retained on failure

Always upload test results, the JaCoCo report, and analyzer reports (`if: always()`), so a red run is diagnosable without re-running locally.

## Risks / Trade-offs

- [Coverage threshold blocks a low-coverage codebase] → set a realistic initial threshold and ratchet upward; document the policy.
- [Static analysis floods with pre-existing violations] → start with a narrow rule set and a baseline file; expand deliberately.
- [OWASP dependency-check slows the build] → run it as a scheduled/nightly job in addition to PRs, with a cached database; keep the PR run bounded.
- [Removing the auto-committed badge loses the README badge] → switch to an external/artifact-based badge; the README references the CI status badge, which remains valid.
- [Third-party actions are a supply-chain risk] → pin actions to major versions and set least privilege.

## Migration Plan

1. Add coverage-verification and static-analysis configuration to `build.gradle`; confirm `./gradlew check` locally.
2. Rewrite `.github/workflows/gradle.yml` with current actions, caching, gates, and report uploads.
3. Add `.github/dependabot.yml` and enable CodeQL.
4. Add the tag-triggered release workflow (jointly with `add-release-packaging`).
5. Update branch protection to require the new checks; remove the auto-commit badge step.

Rollback is a revert of the workflow files; no runtime data is affected.

## Open Questions

- Exact initial coverage threshold and which static analyzer(s) to adopt (Checkstyle vs SpotBugs vs both) — decided at implementation time against the measured baseline.
- Whether OWASP dependency-check runs on every PR or nightly only, depending on runtime cost.
