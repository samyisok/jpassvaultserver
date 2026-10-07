# server-installation Specification

## Purpose
Defines how the sync service is installed, configured, and operated — as a systemd service or a container — including the configuration surface, persistent data, backup/restore, upgrade/rollback, and health/readiness.

## Requirements

### Requirement: systemd installation is supported

The project SHALL provide a hardened systemd unit and an install procedure that runs the service as a dedicated non-root user, supplies configuration from an environment file, and restarts the service on failure.

#### Scenario: Service installs and starts

- **WHEN** an operator runs the install procedure on a supported Linux host with the secret configured
- **THEN** the service is installed, enabled, and running as a non-root user

#### Scenario: Service restarts after a crash

- **WHEN** the service process exits unexpectedly
- **THEN** systemd restarts it

### Requirement: container installation is supported

The project SHALL provide a container deployment template that runs the published image as a non-root user, injects the secret from an environment file, mounts the database on a persistent volume, declares a health check, and sets a restart policy.

#### Scenario: Stack starts with persistent data

- **WHEN** an operator starts the container deployment with a fresh volume
- **THEN** the service starts and stores its database on the mounted volume

#### Scenario: Data survives container replacement

- **WHEN** the container is recreated while the volume is retained
- **THEN** previously stored vault records are still available

### Requirement: Configuration is supplied through the environment

The service SHALL read its secret, listen address and port, database path, transport settings, and trusted-proxy setting from the environment (or an external configuration source), and the project SHALL document each setting with its default.

#### Scenario: Documented settings configure the service

- **WHEN** an operator provides the documented environment variables
- **THEN** the service starts with that configuration and the values are reflected in behavior

#### Scenario: Missing required secret blocks startup

- **WHEN** the service starts without the required secret in the environment
- **THEN** startup fails with a clear error

### Requirement: Persistent, owner-only data location

The database SHALL live on a path that survives restarts and upgrades, and on POSIX systems that directory SHALL be owner-only. Both deployment modes SHALL mount or point at that same persistent location.

#### Scenario: Restart preserves the database

- **WHEN** the service is restarted
- **THEN** previously stored records remain available

#### Scenario: Data directory is owner-only

- **WHEN** the data directory exists on a POSIX system
- **THEN** its permissions restrict access to the owning user

### Requirement: Backup and restore are documented

The project SHALL document a backup procedure that captures the database consistently and a restore procedure that replaces it while the service is stopped.

#### Scenario: Backup captures stored vaults

- **WHEN** an operator follows the backup procedure
- **THEN** the resulting copy contains the current database and can be restored

#### Scenario: Restored data is served again

- **WHEN** an operator restores a backup and starts the service
- **THEN** `GET /files/last` returns the record that was present at backup time

### Requirement: Upgrade and rollback preserve data

The project SHALL document an upgrade procedure that verifies the service after upgrading and a rollback procedure that restores the previous version without losing stored records.

#### Scenario: Upgrade keeps existing data

- **WHEN** the service is upgraded following the documented steps
- **THEN** previously stored records remain available and the service reports healthy

#### Scenario: Rollback restores the previous version

- **WHEN** an upgrade fails and the documented rollback is performed
- **THEN** the previous version runs and serves the same stored records

### Requirement: Health and readiness are exposed

Both deployment modes SHALL use the service's health endpoint to determine readiness, and the documented endpoint SHALL reflect the service's ability to serve requests.

#### Scenario: Ready service reports healthy

- **WHEN** the service is running and able to serve requests
- **THEN** the configured health probe reports it healthy

#### Scenario: Not-ready service is detected

- **WHEN** the service is starting or cannot serve requests
- **THEN** the health probe does not report it healthy
