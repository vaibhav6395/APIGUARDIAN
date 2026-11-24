# Threat model

## Assets
Workspace data, API specifications, policy definitions, test credentials, findings, audit events and customer target configuration.

## Threat actors
Malicious tenant member, compromised user account, malicious uploaded specification, accidental operator error and unauthorized use of the test runner.

## Key risks and mitigations
| Risk | Mitigation |
|---|---|
| Cross-tenant data access | Tenant-scoped queries, authorization checks and isolation tests |
| Malicious OpenAPI upload | Size limits, parser limits, content validation; never execute imported content |
| SSRF through target URL | MVP runner targets bundled demo only; no arbitrary URL input |
| Credential leakage | Environment-based secrets, redaction and no credential persistence in MVP |
| Excessive test traffic | Bounded cases, concurrency and request limits |
| False positive | Show expected policy, observed result and raw sanitized evidence |
| Finding tampering | Append-only audit events and immutable run evidence |
| Session compromise | Secure cookie settings, CSRF protection and rate limiting in production |

## Safety controls
Testing must be limited to systems the operator owns or has written authorization to assess. The bundled demo uses synthetic data and is the default target. Production integrations require a separate security review.
