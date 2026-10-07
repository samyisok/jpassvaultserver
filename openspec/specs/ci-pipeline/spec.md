# ci-pipeline Specification

## Purpose
Defines the automated verification the project performs on every change and release: a reproducible JDK-25 build, an enforced test and coverage gate, static analysis, dependency vulnerability scanning, retained reports, least-privilege workflows, and a separate tag-driven release path.

## Requirements

### Requirement: Build and test on push and pull request

The project SHALL run an automated build and test workflow for every push to `main` and every pull request targeting `main`, using JDK 25 and the project's Gradle wrapper.

#### Scenario: Pull request is verified

- **WHEN** a pull request targeting `main` is opened or updated
- **THEN** the workflow checks out the code, sets up JDK 25, and runs the build and tests

#### Scenario: Build fails on a broken change

- **WHEN** a change does not compile or fails a test
- **THEN** the workflow fails and the change cannot be merged

### Requirement: Coverage threshold is enforced

The workflow SHALL compute test coverage and SHALL fail when line or branch coverage is below the configured minimum.

#### Scenario: Coverage below the threshold fails

- **WHEN** a change drops coverage below the configured minimum
- **THEN** the workflow fails

#### Scenario: Coverage at or above the threshold passes

- **WHEN** coverage meets the configured minimum
- **THEN** the coverage step succeeds

### Requirement: Static analysis is a required gate

The workflow SHALL run static analysis configured in the repository, and a reported violation SHALL fail the build.

#### Scenario: A violation fails the change

- **WHEN** a change introduces a static-analysis violation under the configured rule set
- **THEN** the workflow fails

### Requirement: Dependencies are scanned for known vulnerabilities

The project SHALL scan its dependencies for known vulnerabilities and SHALL fail on high-severity findings that are not explicitly allow-listed. Dependency and action updates SHALL be proposed automatically.

#### Scenario: A vulnerable dependency fails the change

- **WHEN** a dependency with a high-severity known vulnerability is introduced
- **THEN** the workflow fails unless the finding is explicitly allow-listed

#### Scenario: Updates are proposed automatically

- **WHEN** a newer compatible dependency or action version is available
- **THEN** an automated update proposal is created

### Requirement: Reports are retained for failures

The workflow SHALL archive test results, coverage data, and analysis reports so a failed run can be diagnosed from its artifacts.

#### Scenario: Failed run has diagnosable artifacts

- **WHEN** the workflow fails
- **THEN** the test, coverage, and analysis reports are available as run artifacts

### Requirement: Workflows use least privilege and do not write to protected branches

Workflows SHALL request only the permissions they need, defaulting to read-only repository contents, and SHALL NOT commit changes to protected branches.

#### Scenario: Workflow permissions are read-only by default

- **WHEN** a workflow runs
- **THEN** it holds read-only repository permissions unless a job explicitly requires more

#### Scenario: CI does not push to main

- **WHEN** a workflow completes on `main`
- **THEN** it has not created a commit on the branch

### Requirement: Releases are published separately from change verification

A tag-triggered workflow SHALL build and publish releases, and SHALL fail when the pushed tag does not match the project version. Change verification and release publishing SHALL be separate workflows.

#### Scenario: Tag triggers the release workflow

- **WHEN** a version tag is pushed
- **THEN** the release workflow runs the release build and publishing steps

#### Scenario: Tag and version mismatch fails the release

- **WHEN** a pushed tag's version differs from the project version
- **THEN** the release workflow fails before publishing anything

### Requirement: Superseded runs are cancelled

The workflow SHALL cancel an in-progress run when a newer run for the same branch or pull request starts.

#### Scenario: Newer push cancels the older run

- **WHEN** a second push supersedes an in-progress run for the same reference
- **THEN** the in-progress run is cancelled
