# Review

## Code review — 2026-10-07

**Scope:** CI/pipeline change (workflows, `build.gradle` gates, Checkstyle config, docs). No domain or application code changed; the only Java edits are tab→space normalization in three legacy files so Checkstyle passes.

### Checks
- DDD: not applicable — no domain/application code changed.
- GRASP: not applicable.
- Readability: `docs/ci.md` and workflow comments describe intent; Checkstyle baseline (`UnusedImports`, `RedundantImport`, `AvoidStarImport`, `FileTabCharacter`, `NewlineAtEndOfFile`) is enforced.
- Cop: no new Java logic; legacy tab characters removed rather than excluded from the ruleset.

### Gate verification (local)
- Checkstyle gate fires: an injected unused import fails `checkstyleMain` (`UnusedImports`); probe reverted.
- Coverage gate fires: raising the line minimum to 0.99 fails `jacocoTestCoverageVerification` ("lines covered ratio is 0.91"); probe reverted.
- `./gradlew clean build` — BUILD SUCCESSFUL.
- All four workflow files parse as valid YAML.

### Notes / residuals
- The OWASP dependency-check GitHub Action and CodeQL cannot run on this host; they are validated for syntax only. First remote run is the real check.
- The old JaCoCo badge (`.github/badges/jacoco.svg`) is no longer regenerated (the auto-commit step was removed per the spec); the README badge is now stale. `add-project-documentation` rewrites the README.
- Branch protection (required checks) is a repository setting, documented in `docs/ci.md`; it cannot be applied from code.
