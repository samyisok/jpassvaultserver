# Spec Delta

## MODIFIED Requirements

### Requirement: Persistent, owner-only data location

The database SHALL live on a path that survives restarts and upgrades, in a dedicated data directory rather than the process working directory, and on POSIX systems that directory SHALL be owner-only. Both deployment modes SHALL mount or point at that same persistent location.

#### Scenario: Restart preserves the database

- **WHEN** the service is restarted
- **THEN** previously stored records remain available

#### Scenario: Default location is a dedicated data directory

- **WHEN** the service runs with its default configuration
- **THEN** the database is stored in a dedicated data directory and not in the process working directory

#### Scenario: Data directory is owner-only

- **WHEN** the data directory exists on a POSIX system
- **THEN** its permissions restrict access to the owning user
