# Review

## Code review — 2026-10-07

**Scope:** documentation only (`docs/openapi.yaml`, `AGENTS.md`, `README.md`, `CHANGELOG.md` exists, `docs/operations.md` exists). No code changed.

### Checks
- DDD: not applicable.
- GRASP: not applicable.
- Readability: the OpenAPI document describes the *actual* wire shapes, including the plain-text error bodies, rather than inventing a new format; `AGENTS.md` mirrors the sibling project's contributor guidance.
- Cop: no code.

### Verified locally
- `docs/openapi.yaml` parses as OpenAPI 3.1 and documents all four endpoints
  (`/check`, `/files`, `/files/last`, `/files/last/checksum`), the `token`
  apiKey security scheme, request/response fields, and `401`/`404`/`411`/`413`/`429`.
- Every local markdown link in `README.md`, `AGENTS.md`, `docs/operations.md`,
  and `docs/ci.md` resolves to an existing file.
- `./gradlew check` — BUILD SUCCESSFUL (unchanged by docs).
- The release workflow's changelog extraction works (missing section → minimal note).

### Residuals
- No strict OpenAPI linter (for example Redocly/Spectral) is installed; the
  document is YAML-parsed and structurally checked only. Adding a linter to CI is
  a possible follow-up.
- `CHANGELOG.md` currently has only an `[Unreleased]` section; the first tagged
  release will create a version section.
- The OpenAPI document is a description, not a code generator; keep it updated in
  the same change as any API change (rule recorded in `AGENTS.md`).
