# Architecture

## Logical components
- **Web client:** React UI for workspaces, projects, policy definitions, test runs and findings.
- **REST API:** Spring Boot service enforcing tenant boundaries and validating input.
- **Policy engine:** deterministic evaluator for expected role/resource/action outcomes.
- **Runner:** executes bounded test cases against an explicitly configured target.
- **Persistence:** PostgreSQL with Flyway-managed schema.
- **Demo API:** isolated local service with synthetic users and records.

## Trust boundaries
1. Browser to APIGUARDIAN API.
2. APIGUARDIAN API to PostgreSQL.
3. Test runner to customer-controlled target API.
4. Workspace member to tenant-owned records.

The runner must reject loopback/private-network exceptions only when a remote target is configured? For the initial MVP, the runner is restricted to the bundled demo service; remote targets are disabled. Future remote support must use explicit ownership verification, strict host allowlists, egress controls, rate limits and audit logging.

## Data flow
OpenAPI contract -> normalized endpoint inventory -> expected policy matrix -> controlled test cases -> observed outcomes -> findings -> remediation tracking.

## Design principles
- Secure defaults and least privilege.
- Tenant isolation enforced server-side.
- Explainable findings with evidence.
- Deterministic tests before AI-assisted explanations.
- No secrets in logs, reports or source control.
