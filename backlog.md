# Backlog

Deferred items found during autonomous implementation. Not blocking the current
workflow; address later.

## Pre-existing design smells

- `src/main/java/com/samyisok/jpassvaultserver/domains/File.java` — JPA
  (`@Entity`, `@Column`, `@Lob`) annotations and an anemic model inside the
  `domains` package. Consider separating a persistence entity from a domain
  object or renaming the package to reflect it is persistence-only. Surfaced by
  the DDD review during `modernize-java-stack`.
- `FileController` computed the integrity hash itself; resolved by
  `harden-server-security` (checksum handling + MD5 removal). Re-check after
  that change lands.
