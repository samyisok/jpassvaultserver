# Spec Delta

## MODIFIED Requirements

### Requirement: Published container image

A release SHALL publish an OCI container image containing the application and a Java 25 runtime, running as a non-root user, exposing the service port, and declaring a health check against the service. The image SHALL be tagged with the release version, and the `latest` tag SHALL move only for the newest stable (non-pre-release) release.

#### Scenario: Image is published with the version tag

- **WHEN** a version tag is pushed and the release workflow completes
- **THEN** an image tagged with that version is available in the configured registry

#### Scenario: Image runs as non-root

- **WHEN** the published image is started
- **THEN** the application process runs as a non-root user

#### Scenario: Image health check reflects readiness

- **WHEN** the container is running and the service is ready
- **THEN** the declared health check reports the container as healthy

#### Scenario: Pre-release does not move latest

- **WHEN** a pre-release version tag is pushed
- **THEN** the image is tagged with the version but the `latest` tag is unchanged
