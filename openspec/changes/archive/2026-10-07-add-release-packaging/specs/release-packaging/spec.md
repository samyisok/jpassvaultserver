# Spec Delta

## Purpose

Defines the release artifacts the sync server publishes and how they are produced: a versioned executable jar, a container image, integrity metadata (checksums and SBOM), tag/version consistency, and release notes — all built from the release tag.

## ADDED Requirements

### Requirement: Versioned executable jar

A release SHALL publish a runnable Spring Boot jar named with the release version (for example `jpassvaultserver-<version>.jar`), built from the tagged commit.

#### Scenario: Jar is attached to the release

- **WHEN** a version tag is pushed and the release workflow completes
- **THEN** the release assets include `jpassvaultserver-<version>.jar`

#### Scenario: Jar runs the service

- **WHEN** the released jar is started with the required configuration on a JDK 25 runtime
- **THEN** the service starts and answers `GET /check` as expected

### Requirement: Published container image

A release SHALL publish an OCI container image containing the application and a Java 25 runtime, running as a non-root user, exposing the service port, and declaring a health check against the service.

#### Scenario: Image is published with the version tag

- **WHEN** a version tag is pushed and the release workflow completes
- **THEN** an image tagged with that version is available in the configured registry

#### Scenario: Image runs as non-root

- **WHEN** the published image is started
- **THEN** the application process runs as a non-root user

#### Scenario: Image health check reflects readiness

- **WHEN** the container is running and the service is ready
- **THEN** the declared health check reports the container as healthy

### Requirement: Integrity metadata accompanies the release

Each release SHALL publish a software bill of materials and SHA-256 checksums for the published artifacts.

#### Scenario: Checksums are published

- **WHEN** a release is published
- **THEN** a SHA-256 checksum is available for each published artifact

#### Scenario: SBOM is published

- **WHEN** a release is published
- **THEN** a bill of materials listing the runtime dependencies is attached to the release

### Requirement: Tag and version must match

The release workflow SHALL fail before building or publishing when the pushed tag's version does not equal the project version.

#### Scenario: Matching tag publishes

- **WHEN** the tag version equals the project version
- **THEN** the release proceeds

#### Scenario: Mismatched tag fails

- **WHEN** the tag version differs from the project version
- **THEN** the release workflow fails and no artifacts are published

### Requirement: Release notes come from the changelog

A release SHALL use the changelog section for the release version as its notes, and SHALL still publish a minimal note when that section is missing rather than failing the release.

#### Scenario: Changelog section becomes the notes

- **WHEN** `CHANGELOG.md` contains a section for the release version
- **THEN** the release body contains that section

#### Scenario: Missing section does not fail the release

- **WHEN** the version has no section in `CHANGELOG.md`
- **THEN** the release is published with a minimal note

### Requirement: Developer build is unchanged

The default developer build SHALL continue to produce only the executable jar and SHALL NOT build or publish images, SBOMs, or checksums.

#### Scenario: Local build is jar-only

- **WHEN** a developer runs `./gradlew build`
- **THEN** only the executable jar is produced and no image or release metadata is generated
