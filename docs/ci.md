# Continuous integration

## Workflows

- `.github/workflows/gradle.yml` — CI: build, tests, JaCoCo coverage
  verification, Checkstyle, dependency vulnerability scan, report upload.
  Runs on every push to `main` and every pull request targeting `main`.
- `.github/workflows/codeql.yml` — CodeQL analysis on push/PR and weekly.
- `.github/workflows/release.yml` — tag-triggered release; verifies the tag
  matches `build.gradle`'s `version`, builds the jar, and publishes a GitHub
  release.
- `.github/dependabot.yml` — weekly Gradle and GitHub Actions update proposals.

## Required status checks

Protect `main` and require these checks to pass before merge:

- `CI / build` (build, tests, coverage, Checkstyle, dependency scan)
- `CodeQL / analyze`

## Run the gates locally

```sh
./gradlew check      # tests + JaCoCo coverage verification + Checkstyle
./gradlew build      # everything above plus packaging
```

Coverage thresholds live in `build.gradle`
(`jacocoTestCoverageVerification`). The current floor is 85% line and 70%
branch; raise it as coverage improves.

## Adding new checks

Add them to `gradle.yml` (or a new workflow) and to the required-checks list
above. Keep workflow `permissions` least-privilege: jobs default to
`contents: read` and only request more when a step needs it.
