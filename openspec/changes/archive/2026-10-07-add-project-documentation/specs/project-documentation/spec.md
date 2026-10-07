# Spec Delta

## Purpose

Defines the documentation the project maintains and keeps accurate: an entry-point README, an authoritative API description, a contributor and agent guide, a release changelog, and an operations runbook — each describing the implemented behavior.

## ADDED Requirements

### Requirement: README describes the project and how to use it

The repository SHALL provide a README that states the service's purpose, its requirements, and how to build, run, and configure it, and that links to the API reference, operations guide, contributor guide, and changelog.

#### Scenario: A newcomer can build and run

- **WHEN** a newcomer follows the README
- **THEN** they can build the service and start it with a configured secret

#### Scenario: README links are valid

- **WHEN** the referenced documents are checked
- **THEN** every link in the README resolves to an existing document

### Requirement: API is described by an OpenAPI specification

The repository SHALL provide an OpenAPI specification that describes every HTTP endpoint, the authentication header, the request and response fields, and the error responses, matching the implemented behavior.

#### Scenario: Specification covers all endpoints

- **WHEN** the implemented endpoints are compared with the specification
- **THEN** each of `GET /check`, `POST /files`, `GET /files/last`, and `GET /files/last/checksum` is documented

#### Scenario: Documented fields match the implementation

- **WHEN** a request or response is exercised against the implementation
- **THEN** the documented paths, header name, request field, and response fields match what the service accepts and returns

#### Scenario: Error responses are documented

- **WHEN** an invalid token, an oversized payload, or a throttled client is exercised
- **THEN** the documented status codes for those cases match the service's responses

### Requirement: Contributor and agent guide

The repository SHALL provide a guide describing the build and test commands, the verification gates, the architecture, and the project's coding and testing rules.

#### Scenario: Guide reflects the build

- **WHEN** the documented build and test commands are run
- **THEN** they work as described

#### Scenario: Guide reflects the rules

- **WHEN** a contributor follows the guide's testing and file-size rules
- **THEN** their change conforms to the documented expectations

### Requirement: Changelog records each release

The repository SHALL provide a changelog that records user-visible changes per version in a consistent format, with a section for each released version.

#### Scenario: Release has a changelog section

- **WHEN** a version is released
- **THEN** the changelog contains a section for that version describing the changes

#### Scenario: Release notes can be derived

- **WHEN** the release process looks up the version in the changelog
- **THEN** the corresponding section can be extracted as release notes

### Requirement: Operations guide is available

The repository SHALL provide an operations guide covering configuration, transport/TLS setup, backup and restore, upgrade and rollback, health checks, and troubleshooting.

#### Scenario: An operator can back up and restore

- **WHEN** an operator follows the operations guide's backup and restore sections
- **THEN** stored vault records survive the procedure

#### Scenario: An operator can upgrade safely

- **WHEN** an operator follows the operations guide's upgrade section
- **THEN** the service upgrades without losing stored records

### Requirement: Documentation is updated with behavior

A change that alters endpoints, request or response fields, configuration, or deployment procedures SHALL update the corresponding documentation in the same change.

#### Scenario: API change updates the specification

- **WHEN** an endpoint or field changes
- **THEN** the same change updates the OpenAPI specification

#### Scenario: Configuration change updates the guide

- **WHEN** a configuration setting is added, removed, or changes meaning
- **THEN** the same change updates the README and operations guide
