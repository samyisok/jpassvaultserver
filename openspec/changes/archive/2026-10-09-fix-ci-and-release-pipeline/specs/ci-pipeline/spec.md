# Spec Delta

## MODIFIED Requirements

### Requirement: Dependencies are scanned for known vulnerabilities

The project SHALL monitor its dependencies for known vulnerabilities and SHALL propose dependency and action updates automatically. When an NVD API key is configured, continuous integration SHALL additionally run an OWASP dependency scan and SHALL fail on high-severity findings that are not explicitly allow-listed; when no key is configured the scan SHALL be skipped and vulnerability alerting SHALL be provided by Dependabot.

#### Scenario: A vulnerable dependency fails the change

- **WHEN** an NVD API key is configured and a dependency with a high-severity known vulnerability is introduced
- **THEN** the workflow fails unless the finding is explicitly allow-listed

#### Scenario: Updates are proposed automatically

- **WHEN** a newer compatible dependency or action version is available
- **THEN** an automated update proposal is created

#### Scenario: Scan is skipped without an API key

- **WHEN** no NVD API key is configured
- **THEN** continuous integration does not run the OWASP scan and does not fail because of it
