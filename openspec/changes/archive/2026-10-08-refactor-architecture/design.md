# Design

## Context

- See proposal.md — Why. Current structure: `controllers/FileController` (REST + payload-size/blank rule + retention + repository call + entity-as-DTO), `domains/File` (JPA entity, anemic, id-based `equals`/`hashCode`), `domains/FileRepository`, `domains/FileNotFoundException`, `advices/FileNotFoundAdvice`, plus `auth/`, `security/`, `persistence/DatabasePermissions`.
- Constraints: HTTP contract byte-for-byte identical (`/check`, `/files`, `/files/last`, `/files/last/checksum`; `token` header; JSON `check`/`file`/`hash`/`id`/`createdDate`/`modifiedDate`/`checksum`; statuses `200/400/401/404/411/413/429`); H2 schema unchanged; files ≤350 lines; JUnit 5 only; keep the service small (no ceremony).
- Architecture guidance: DDD tactical patterns and GRASP (via the architecture agent and the `java-ddd` skill).

## Goals / Non-Goals

**Goals:**

- Honest package names: persistence lives in `persistence`.
- One place for the use-case rules (validation, retention, checksum, last).
- A thin controller that only does HTTP.
- The install procedure actually exercised.

**Non-Goals:**

- No separate domain object + mapper (there is no behavior to model).
- No request/response DTOs (the entity's serialization already matches the schema).
- No service interface/implementation pair, no CQRS, no event sourcing, no ports/ACL package.
- No API, schema, or behavior change.

## Decisions

### D1: Rename `domains` → `persistence` (do not split)

`domains` contains only a JPA-mapped row (`File` with `@Entity`/`@Column`/`@Lob`) and its Spring Data repository; there is no domain behavior to protect — `File` is an opaque encrypted-blob holder. Splitting a pure domain object from a separate persistence entity would require two near-identical types, a mapper, and mapping tests for a class whose only rules are "not blank" and "under N characters" — ceremony, not architecture. Fix the misleading name instead.

- Pattern: **Entity** (tactical) for `File`; **Repository** for `FileRepository`. The entity stays anemic *correctly* because it genuinely has no behavior.

### D2: `FileNotFoundException` moves to `services`

It is not a persistence concept; it is the use-case outcome "no payload stored yet", raised by the service and mapped to `404` by `FileNotFoundAdvice`. Placement: **Protected Variations** — a stable error signal decoupled from the web handler.

### D3: `services/VaultService` — the application layer

```java
@Service
class VaultService {
  VaultService(FileRepository repository, AppProperties appProperties);

  @Transactional
  File store(File newFile);      // validate → save → delete all but the saved row → return saved
  File last();                   // orElseThrow(FileNotFoundException::new)
  String lastChecksum();         // "" when empty; else stored checksum; else Crypto.sha256Hex(payload)

  private void validate(File newFile);  // null/blank → 400; payload length → 413; checksum length → 413
}
```

- **Validation → service (Information Expert):** the service holds both the request payload and the configured bound (`AppProperties.getMaxPayloadSize()`); keeping the three checks together avoids splitting one rule.
- **Retention → service policy + repository operation (Creator / Information Expert):** the service owns the "only the newest is kept" policy and wraps save + delete in one transaction; `deleteByIdNot` remains the repository's collection operation.
- **Checksum selection → service;** the wire field name `hash` is HTTP and stays in the controller.
- Keep throwing `ResponseStatusException` for `400`/`413` (established pattern); a domain-exception hierarchy plus advice would be ceremony.

### D4: Keep the entity as the API model

The documented `StoredFile` schema is exactly the entity's serialization, and the request is the two-field subset. `@JsonProperty(READ_ONLY)` on `getId()` already blocks a client-supplied id; `createdDate`/`modifiedDate` are getter-only. A DTO + mapper would add a class and tests with no contract or safety gain. Revisit only if a response ever diverges from the stored row.

### D5: `@Transactional` on `store`

The retention delete and the insert must commit together; a single annotation is the correct, minimal fix.

### D6: Verify `install.sh` in a container

Run the installer and the service inside a systemd-capable container (Docker) so the unit file, user creation, directories, and service startup are exercised for real. If a systemd container is not feasible in this environment, fall back to a container that runs the same file/permission steps and the service as a non-root user, and record the limitation. Fix any defect found; keep the change a verification otherwise.

## Risks / Trade-offs

- [Package move breaks imports/`@Entity` scan] → the entity is discovered by `@SpringBootApplication` scanning; moving within the same base package is safe; compile + full test suite confirm.
- [Controller tests change shape] → logic assertions move to `VaultService` unit tests; controller tests verify delegation; the MockMvc `SecurityFilterChainIntegrationTest` remains the contract guard.
- [Coverage gate] → moved logic must stay covered by the new `VaultService` tests.
- [Systemd in Docker may be unavailable] → fall back to a plain-container verification and document it.

## Migration Plan

1. Move `File`, `FileRepository` → `persistence`; move `FileNotFoundException` → `services`; update imports and move tests.
2. Add `VaultService`; move validation/retention/checksum/last out of the controller; add `@Transactional`.
3. Slim `FileController` to `VaultService` + `/check`.
4. Re-point tests; add a retention integration test.
5. Verify `install.sh` in Docker.
6. Update `AGENTS.md`, `CHANGELOG.md`, `backlog.md`.

Rollback: revert the commit; no data or contract change.

## Open Questions

None. (DDD/GRASP consultation is recorded in the design above.)
