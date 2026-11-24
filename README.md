# APIGUARDIAN

**Continuous API Authorization Security Testing**

APIGUARDIAN is a developer-focused platform for documenting expected API access policies and checking an explicitly authorized API environment against those policies. It is designed to help teams find authorization regressions such as users accessing resources or operations outside their assigned role.

> **Safety boundary:** Only test systems you own or are explicitly authorized to assess. The initial release uses a local demo API and controlled test identities. It does not scan arbitrary public targets.

## Project status

This repository is being built in milestones. The initial foundation includes the product specification, architecture, security model, local demo workflow and implementation roadmap. Features are marked as implemented only after code and tests exist.

## Planned stack

- Backend: Java 21, Spring Boot, Maven, Spring Security, PostgreSQL, Flyway
- Frontend: React, TypeScript, Vite
- API contract: OpenAPI
- Testing: JUnit 5, integration tests, GitHub Actions
- Local development: Docker Compose

## Quick start

The runnable application and detailed setup instructions will be added as implementation milestones land. Start with:

- [Architecture](docs/ARCHITECTURE.md)
- [Product requirements](docs/PRODUCT-REQUIREMENTS.md)
- [Threat model](docs/THREAT-MODEL.md)
- [Security and safe-use policy](docs/SECURITY.md)
- [Roadmap](docs/ROADMAP.md)

## Core workflow

1. Create a workspace and project.
2. Import an OpenAPI document for an API you control.
3. Define roles, endpoints and expected allow/deny outcomes.
4. Run controlled tests against the configured local or authorized test environment.
5. Review evidence-backed findings and compare subsequent runs.

## Non-goals

APIGUARDIAN is not an exploit framework, internet-wide scanner, credential harvesting tool or substitute for a professional penetration test. It validates documented authorization expectations in a controlled environment.

## License

MIT. See [LICENSE](LICENSE).
