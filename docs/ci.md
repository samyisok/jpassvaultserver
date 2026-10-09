# Continuous integration

## Workflows

- `.github/workflows/gradle.yml` — CI: build, tests, JaCoCo coverage
  verification, Checkstyle, OpenAPI lint, installer smoke test, and (when the
  `NVD_API_KEY` secret is set) an OWASP dependency scan. Runs on every push to
  `main` and every pull request targeting `main`.
- `.github/workflows/codeql.yml` — CodeQL analysis on push/PR and weekly.
- `.github/workflows/release.yml` — tag-triggered release; verifies the tag
  matches `build.gradle`'s `version`, builds the jar, generates an SBOM and
  checksums, pushes the image (with Buildx provenance; moving `latest` only for
  stable versions), and publishes a GitHub release.
- `.github/dependabot.yml` — weekly Gradle and GitHub Actions update proposals,
  and vulnerability alerts when no NVD API key is configured.

Third-party Actions are pinned to immutable commit SHAs (with a `# vX` comment);
Dependabot keeps the pins current.

The OWASP dependency scan needs a free NVD API key stored as the `NVD_API_KEY`
repository secret. Without it the step is skipped and Dependabot provides
vulnerability alerting.

## Required status checks

Protect `main` and require these checks to pass before merge:

- `CI / build` (build, tests, coverage, Checkstyle, dependency scan)
- `CodeQL / analyze`

## Run the gates locally

```sh
./gradlew check      # tests + JaCoCo coverage verification + Checkstyle
./gradlew build      # everything above plus packaging
npx --yes @redocly/cli@1.34.5 lint docs/openapi.yaml   # OpenAPI lint
./deploy/install-verify.sh                             # installer smoke test (Docker)
```

Coverage thresholds live in `build.gradle`
(`jacocoTestCoverageVerification`). The current floor is 85% line and 70%
branch; raise it as coverage improves.

## Adding new checks

Add them to `gradle.yml` (or a new workflow) and to the required-checks list
above. Keep workflow `permissions` least-privilege: jobs default to
`contents: read` and only request more when a step needs it.
