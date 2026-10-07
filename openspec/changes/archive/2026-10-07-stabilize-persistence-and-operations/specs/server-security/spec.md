# Spec Delta

## MODIFIED Requirements

### Requirement: Encrypted transport by default

The deployed server SHALL serve requests over TLS, either terminated at a reverse proxy or configured in-process, and SHALL refuse ordinary cleartext HTTP unless the deployment explicitly acknowledges its transport mode. Acknowledging TLS termination at a trusted reverse proxy SHALL be distinct from the development-only plain-HTTP acknowledgment.

#### Scenario: Cleartext refused in production configuration

- **WHEN** the server is configured without in-process TLS and without any transport acknowledgment
- **THEN** startup fails with a clear error

#### Scenario: Explicit development opt-in allows plain HTTP

- **WHEN** the development plain-HTTP acknowledgment is set for local development
- **THEN** the server starts and serves requests over HTTP

#### Scenario: In-process TLS is accepted

- **WHEN** in-process TLS is configured
- **THEN** the server starts without a transport acknowledgment

#### Scenario: Proxy-terminated TLS is acknowledged

- **WHEN** the deployment sets the proxy-terminated-TLS acknowledgment
- **THEN** the server starts, because TLS is terminated at the trusted reverse proxy

### Requirement: Stored data is owner-only

On systems that support POSIX permissions, the database SHALL live in a dedicated data directory that is owner-only (`0700`), and the database file and its auxiliary files SHALL be owner-only (`0600`). The data directory SHALL be created owner-only before the database is opened, and the default configuration SHALL place the database in a dedicated data directory rather than the process working directory.

#### Scenario: Database directory is owner-only

- **WHEN** the server starts with its default configuration on a POSIX system
- **THEN** a dedicated data directory exists with permissions `0700` and is not the process working directory

#### Scenario: Database file is owner-only

- **WHEN** the database file exists on a POSIX system
- **THEN** its permissions are `0600`

## ADDED Requirements

### Requirement: Authentication is evaluated before request limits

The server SHALL evaluate the request credential before enforcing payload-size limits, so an unauthenticated request is answered as unauthorized rather than as an oversized or length-required request.

#### Scenario: Invalid token with an oversized body is unauthorized

- **WHEN** a request with an invalid token carries a body larger than the configured maximum
- **THEN** the server responds with HTTP `401` and does not reveal the size limit

#### Scenario: Valid token with an oversized body is rejected by size

- **WHEN** a request with a valid token carries a body larger than the configured maximum
- **THEN** the server responds with HTTP `413`
