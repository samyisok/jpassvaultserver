# Design

## Context

- See proposal.md — Why. Today the only artifact is the Spring Boot jar produced by `./gradlew build`; `README.md` tells operators to build the jar and run it under systemd. The project is a Spring Boot web service (embedded Tomcat, H2 file database) with a `/check` health endpoint.
- Constraint: the tag must be the single version source; `add-ci-pipeline` already defines the tag/version check and workflow separation.
- Constraint: the container image must run on a JDK 25 runtime and must not require root.
- Constraint: `./gradlew build` for developers must not start producing images.

## Goals / Non-Goals

**Goals:**

- Reproducible, versioned release artifacts built from the tag.
- A runnable jar and an OCI image with a health check.
- Integrity metadata (checksums + SBOM) attached to the release.
- Release notes sourced from the changelog.

**Non-Goals:**

- Deployment automation and configuration files — `add-installation`.
- Image signing / build provenance attestation — Open Question.
- Multi-arch images on GitHub-hosted runners where cross-building is not native (may be limited to `linux/amd64` initially).
- Changing the `/check` contract or any API.

## Decisions

### D1: The versioned boot jar is the primary artifact

The release workflow runs `./gradlew clean build -Pversion=<tag-version>` (or sets the Gradle version from the tag) and attaches `jpassvaultserver-<version>.jar`. The jar remains the artifact used by the systemd installation path.

### D2: Container image from a JRE 25 base, non-root

Build the image either with Jib (no Docker daemon) or a small `Dockerfile` using an Eclipse Temurin 25 JRE base. Run as a dedicated non-root user, expose the configured port, and declare a `HEALTHCHECK` that calls `GET /check` with a token (or a dedicated unauthenticated readiness path if `/check` requires auth). Keep the image small and the layers cache-friendly.

**Constraint check:** `/check` is behind `AuthKeyFilter`, so the health check must supply a token or the health path must be exempted. If exempting it weakens the security change, prefer supplying the token from the environment or add a dedicated readiness endpoint (a small, explicitly documented addition) — decided at implementation time and reflected in `add-installation`.

### D3: Publish to GHCR with version and `latest`

Push to `ghcr.io/<owner>/jpassvaultserver` tagged `:<version>`; also update `:latest` when the tag is the newest stable release. Use the workflow `GITHUB_TOKEN` with `packages: write` scoped to the release job only.

### D4: Tag is the single version source; mismatch fails

The release workflow derives the version from `GITHUB_REF_NAME` and verifies it equals the Gradle project version before building anything. This is shared with `add-ci-pipeline`'s requirement so no artifact can be published under a version the build does not carry.

### D5: SBOM and checksums

Generate a CycloneDX SBOM for the runtime dependencies and compute SHA-256 checksums for each published artifact. Attach `*.sha256` files and the SBOM to the release so an operator can verify what they downloaded.

### D6: Release notes from CHANGELOG

Extract the section for the release version from `CHANGELOG.md` (added by `add-project-documentation`) and use it as the release body; if the section is missing, publish a minimal body rather than failing the release.

### D7: Developer build unchanged

Image and SBOM steps live in the release workflow, not in the default `build` task. `./gradlew build` keeps producing only the executable jar.

## Risks / Trade-offs

- [Image build needs a daemon and is slow] → prefer Jib (daemonless) if it covers the required base image; otherwise a minimal Dockerfile with layer caching.
- [Health check cannot authenticate] → decide between a token-in-healthcheck and a dedicated readiness endpoint; the latter is a tiny, documented API addition and must not bypass the main filter for data endpoints.
- [GHCR permissions] → scope `packages: write` to the release job; keep the default read-only.
- [`latest` tag mis-published from an old release] → only move `latest` when the tag is the newest semver version.
- [SBOM grows the release assets] → acceptable; it is small and valuable.
- [Jar built with H2 embedded, image also embeds H2] → the database must live on a mounted volume in the container; the installation change documents the volume path.

## Migration Plan

1. Add the SBOM plugin and container definition; verify `./gradlew build` is unchanged.
2. Add the tag-triggered release workflow with version check, jar, SBOM, checksums, GHCR push, and GitHub release.
3. Dry-run with a pre-release tag on a fork/branch, then with a real tag.
4. Update the installation guide to consume `jpassvaultserver-<version>.jar` and the published image.

Rollback: deleting a bad release/tag and re-running from a fixed tag; no data is affected.

## Open Questions

- Whether to add image signing and build provenance (for example `cosign` / SLSA provenance) now or later.
- Whether the health check uses a token on `/check` or a new unauthenticated readiness endpoint, pending the `harden-server-security` outcome.
- Whether multi-architecture images (`linux/amd64`, `linux/arm64`) are required; GitHub-hosted runners may need emulation.
- Exact base image and whether to use Jib versus a Dockerfile.
