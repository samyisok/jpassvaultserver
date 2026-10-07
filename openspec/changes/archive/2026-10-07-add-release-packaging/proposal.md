# Proposal

## Why

The server has no release pipeline and no distributable artifacts beyond a locally built jar. Operators must build it themselves; there is no container image, no versioned artifact attached to a GitHub release, no checksum or bill of materials, and no guarantee that a published artifact was built from the tagged source. A sync service that holds encrypted vaults needs a repeatable, verifiable release: a versioned runnable jar and an OCI container image, both produced from the tag, with integrity metadata.

## What Changes

- Produce a versioned Spring Boot executable jar (`jpassvaultserver-<version>.jar`) as the primary release artifact.
- Produce a container image (OCI) with a Java 25 runtime, running as a non-root user, with a health check against the existing `/check` endpoint, published to GitHub Container Registry (GHCR) tagged with the version (and `latest` for the newest stable release).
- Add a tag-triggered release workflow that verifies the tag matches the project version, builds the artifacts from that exact commit, and publishes them to the GitHub release and GHCR.
- Generate a software bill of materials (SBOM, CycloneDX) and SHA-256 checksums for each release artifact.
- Attach release notes extracted from `CHANGELOG.md`.
- Keep the default developer build (`./gradlew build`) unchanged; packaging is an explicit release step.
- **Out of scope:** deployment/installation procedures (`add-installation`), multi-architecture image publishing beyond the platforms the runners can build, and signed/provenanced images (kept as an Open Question).

## Capabilities

### New Capabilities

- `release-packaging`: the release artifacts the project publishes — versioned executable jar, container image, integrity metadata, tag/version consistency, and release notes — and how they are produced from a tag.

### Modified Capabilities

None. `ci-pipeline` covers change verification and the release trigger; this change covers the artifact content and publishing.

## Impact

- CI/CD: adds `.github/workflows/release.yml` (or extends the tag workflow from `add-ci-pipeline`) with build, SBOM, checksum, GHCR push, and GitHub release steps.
- Build: adds an SBOM generation step (CycloneDX Gradle plugin) and a container definition (`Dockerfile` or Jib configuration); the default `build` task behavior is unchanged.
- Registry: publishes images to `ghcr.io/<owner>/jpassvaultserver`.
- Users/operators: gain a runnable jar and an image; the installation guide (`add-installation`) consumes them.
- No runtime API change.
