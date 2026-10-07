# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [2.1.0] - 2026-10-07

### Added

- Add a CI pipeline with build/test, an enforced coverage floor, Checkstyle, dependency vulnerability scanning, CodeQL, Dependabot, and a tag-triggered release workflow. (`2026-10-07-add-ci-pipeline`)
- Publish versioned release artifacts from a tag: an executable jar, a non-root container image with a health check, a CycloneDX SBOM, and SHA-256 checksums. (`2026-10-07-add-release-packaging`)
- Add supported installation and operations: a hardened systemd unit and install script, a Docker Compose deployment with a persistent volume, a configuration reference, and backup/restore and upgrade/rollback procedures. (`2026-10-07-add-installation`)
- Add project documentation: an OpenAPI 3.1 API description, a contributor/agent guide, an operations runbook, and a changelog, with a rule to update docs in the same change. (`2026-10-07-add-project-documentation`)

### Changed

- Upgrade the sync server to Java 25 and Spring Boot 4.1.1 (Jakarta namespace) on Gradle 9.8.1; the HTTP API and stored data are unchanged. (`2026-10-07-modernize-java-stack`)
- Return the client-supplied change-detection value verbatim (SHA-256 fallback) instead of an unkeyed MD5. (`2026-10-07-harden-server-security`)
- Keep only the newest stored vault payload and reject blank uploads (code review 2026-10-07).

### Deprecated

### Removed

### Fixed

### Security

- Remove the committed API secret, require an externally supplied secret, compare tokens in constant time, keep tokens out of logs, require TLS unless explicitly acknowledged, bound request payloads, throttle repeated failures, and make the H2 data directory owner-only. (`2026-10-07-harden-server-security`)
- Reject weak or short API secrets, key authentication throttling on the connection address, sanitize logged addresses, pin the CI dependency-scan action, and verify the build before releasing (code review 2026-10-07).
