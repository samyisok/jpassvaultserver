[![CI](https://github.com/samyisok/jpassvaultserver/actions/workflows/gradle.yml/badge.svg)](https://github.com/samyisok/jpassvaultserver/actions/workflows/gradle.yml)

# jpassvaultserver

Online sync service for [jpassvault](https://github.com/samyisok/jpassvault).

A small Spring Boot service that stores the encrypted vault payload uploaded by
the desktop client and answers change-detection checksum requests. It holds
already-encrypted data; it never sees the master password.

## Requirements

- Java 25 (to build and run the jar directly), or Docker.
- A TLS-terminating reverse proxy for any non-local deployment.

## Build and run

```sh
./gradlew build
JPASSVAULT_SECRET=change-me \
  APP_PROPERTIES_ALLOW_PLAIN_HTTP=true \
  java -jar build/libs/jpassvaultserver-2.0.0.jar
```

The server refuses to start without a secret, and without TLS unless the
plain-HTTP acknowledgment is set.

## API

All endpoints require a `token` header equal to the configured secret; a bad or
missing token returns `401` with the body `Invalid API KEY`.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/check` | Health/readiness: `{"check":"ok"}`. |
| `POST` | `/files` | Store a vault payload: `{"file":"<base64>","checksum":"<optional>"}`. |
| `GET` | `/files/last` | Return the most recently stored payload. |
| `GET` | `/files/last/checksum` | Return the stored checksum (SHA-256 fallback) as `{"hash":"..."}`. |

A full description is in [`docs/openapi.yaml`](docs/openapi.yaml).

## Configuration

See [`docs/operations.md`](docs/operations.md) for every setting and its default.

## Deploy

- **systemd:** [`deploy/install.sh`](deploy/install.sh) installs a hardened unit;
  see [`docs/operations.md`](docs/operations.md).
- **Container:** [`docker-compose.yml`](docker-compose.yml) runs the published
  image with a persistent data volume.

## Operations

Backup, restore, upgrade, rollback, TLS, and troubleshooting:
[`docs/operations.md`](docs/operations.md). CI and required checks:
[`docs/ci.md`](docs/ci.md). Release history: [`CHANGELOG.md`](CHANGELOG.md).

## Contributing

Build, test, and project rules: [`AGENTS.md`](AGENTS.md). The API description is
[`docs/openapi.yaml`](docs/openapi.yaml).
