# Tasks

## 1. Jar artifact

- [x] 1.1 The release build derives the version from the tag and the jar is named `jpassvaultserver-<version>.jar` — the release workflow verifies tag == `build.gradle` version before building
- [x] 1.2 Confirm the default `./gradlew build` produces only the jar — `jar { enabled = false }`; `build/libs` contains only the boot jar

## 2. Container image

- [x] 2.1 Add a `Dockerfile` using a Temurin 25 JRE base, a non-root user, the service port, and a `HEALTHCHECK` — image builds
- [x] 2.2 Health-check authentication — uses the `token` header with `JPASSVAULT_SECRET` from the container environment
- [x] 2.3 Confirm the process runs as non-root — verified `uid=10001(app)`

## 3. Integrity metadata

- [x] 3.1 Add the CycloneDX SBOM generation step (`anchore/sbom-action`) — runs in CI (not runnable on this host)
- [x] 3.2 Generate SHA-256 checksums for the released artifacts (`SHA256SUMS`)

## 4. Release workflow

- [x] 4.1 Tag-triggered workflow: verify tag/version, build jar, generate SBOM + checksums, build/push image to GHCR — syntax valid; image verified locally
- [x] 4.2 Scope `packages: write` to the release workflow only; default remains read-only
- [x] 4.3 Publish jar, SBOM, and checksums to the GitHub release; tag the image with the version and `latest`
- [x] 4.4 Extract release notes from `CHANGELOG.md` with a minimal fallback

## 5. Verification

- [ ] 5.1 Push a real version tag and inspect the release assets and GHCR tag — deferred: requires a remote push; jar, image, and metadata steps verified locally
- [x] 5.2 Start the built image and run `GET /check` — verified healthy
- [x] 5.3 Run `openspec validate --strict` for this change — passes
