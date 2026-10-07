# Tasks

## 1. Coverage gate

- [x] 1.1 Add a `jacocoTestCoverageVerification` rule to `build.gradle` — line 0.85 / branch 0.70 (current measured 0.916 / 0.762); probe confirmed a higher minimum fails the build
- [x] 1.2 Wire `check` to depend on the verification task — `check.dependsOn jacocoTestCoverageVerification`

## 2. Static analysis

- [x] 2.1 Apply Checkstyle (`checkstyle` plugin, tool 10.20.0) with `config/checkstyle/checkstyle.xml` — `./gradlew check` passes on the current code
- [x] 2.2 Confirm a violation fails the gate — injected unused import failed `checkstyleMain`; probe reverted

## 3. Dependency and security scanning

- [x] 3.1 Add an OWASP dependency-check step to `gradle.yml` with `--failOnCVSS 7` — runs in CI (cannot run on this host)
- [x] 3.2 Add `.github/dependabot.yml` for Gradle and GitHub Actions — YAML valid
- [x] 3.3 Enable CodeQL for Java — `.github/workflows/codeql.yml`

## 4. CI workflow rework

- [x] 4.1 Rewrite `.github/workflows/gradle.yml` with `checkout@v4`, `setup-java@v4` (temurin, JDK 25), Gradle cache, `concurrency`, `permissions: contents: read` — YAML valid
- [x] 4.2 Add build/test/coverage/static-analysis/dependency-scan steps — each gate verified locally where runnable
- [x] 4.3 Upload test, coverage, and analysis reports with `if: always()`
- [x] 4.4 Remove the CI badge auto-commit step — CI no longer pushes to `main`

## 5. Release workflow separation

- [x] 5.1 Add `.github/workflows/release.yml` triggered by `v*` tags, verifying the tag matches `build.gradle`'s version and building the jar; publishing specifics extended by `add-release-packaging`

## 6. Documentation and branch protection

- [x] 6.1 Document the required status checks in `docs/ci.md` (branch protection is a repository setting)
- [x] 6.2 Document running the gates locally (`./gradlew check`)

## 7. Verification

- [x] 7.1 Workflow syntax validated; gates verified locally. A real GitHub run is pending the first push (cannot run on this host)
- [x] 7.2 Run `openspec validate --strict` for this change — passes
