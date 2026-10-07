# Tasks

## 1. Package move (no behavior change)

- [x] 1.1 `File`, `FileRepository` moved to `persistence/`; imports updated
- [x] 1.2 `FileNotFoundException` moved to `services/`; imports updated
- [x] 1.3 Tests moved; `./gradlew clean build` green

## 2. Application service

- [x] 2.1 `services/VaultService` added with `store` (`@Transactional`), `last`, `lastChecksum`, private `validate`
- [x] 2.2 Validation, retention, checksum selection, and the last lookup moved out of `FileController`
- [x] 2.3 `FileController` slimmed to depend only on `VaultService`; `HASH_FIELD` and inline `/check` kept

## 3. Tests

- [x] 3.1 Logic tests moved to `VaultService*UnitTest` (mock `FileRepository` + `AppProperties`)
- [x] 3.2 `FileControllerDelegationUnitTest` verifies delegation to `VaultService`
- [x] 3.3 `VaultRetentionIntegrationTest` proves only the newest payload survives
- [x] 3.4 `./gradlew clean build` green; coverage gate met

## 4. Install verification

- [x] 4.1 `deploy/install-verify.sh` runs `install.sh` in a container (Docker) and starts the service as the service user; `systemd-analyze verify` also runs
- [x] 4.2 No installer defect found; evidence recorded in `review.md`

## 5. Documentation and backlog

- [x] 5.1 `AGENTS.md` architecture section updated (`domains`→`persistence`, added `services`)
- [x] 5.2 `CHANGELOG.md` entry added (internal refactor; contract unchanged)
- [x] 5.3 Backlog items 7 and 8 removed; image signing (item 5) dropped

## 6. Verification

- [x] 6.1 `./gradlew clean build` green
- [x] 6.2 HTTP contract unchanged (`SecurityFilterChainIntegrationTest` + OpenAPI unchanged)
- [x] 6.3 `openspec validate --strict` for this change
