# Spec Delta

## ADDED Requirements

### Requirement: Third-party workflow actions are pinned to immutable references

Workflows SHALL reference third-party GitHub Actions by an immutable commit reference rather than a mutable branch or floating tag, so an upstream change cannot alter what runs with the workflow's token.

#### Scenario: Actions are pinned

- **WHEN** a workflow file is inspected
- **THEN** each third-party action is referenced by a full commit SHA (or an equivalent immutable reference) with a human-readable version comment

### Requirement: The API specification is linted

The project SHALL validate its OpenAPI specification in continuous integration and SHALL fail the build when the specification has lint errors.

#### Scenario: Specification is valid

- **WHEN** a change updates the OpenAPI specification
- **THEN** the lint step validates it and passes

#### Scenario: Invalid specification fails the build

- **WHEN** the specification has a lint error
- **THEN** the lint step fails the build

### Requirement: Web-layer tests cover the request filter chain

The project SHALL include tests that exercise the real servlet filter chain for the documented status codes and their ordering.

#### Scenario: Unauthorized request is exercised end to end

- **WHEN** a request without a valid token is sent through the filter chain
- **THEN** the response is HTTP `401` with the documented body

#### Scenario: Filter ordering is exercised

- **WHEN** an oversized request carries an invalid token
- **THEN** the response is HTTP `401`, not `411` or `413`
