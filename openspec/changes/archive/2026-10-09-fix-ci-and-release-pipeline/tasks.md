# Tasks

## 1. Release: buildx setup

- [x] 1.1 `docker/setup-buildx-action` (pinned SHA) added before `docker/build-push-action` in `.github/workflows/release.yml`
- [x] 1.2 Workflow YAML valid

## 2. CI: dependency scan gating

- [x] 2.1 OWASP step gated on `secrets.NVD_API_KEY` and passed via `--nvdApiKey`
- [x] 2.2 Workflow YAML valid

## 3. Docs and spec

- [x] 3.1 `docs/ci.md` notes the `NVD_API_KEY` requirement; `AGENTS.md` updated
- [x] 3.2 `ci-pipeline` spec delta updated (dependency requirement + no-key scenario)

## 4. Verification

- [x] 4.1 `openspec validate --strict` for this change — valid
- [ ] 4.2 Push and confirm CI is green and the Release workflow publishes the image
