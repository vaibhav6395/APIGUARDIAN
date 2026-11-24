# APIGUARDIAN

**Continuous API Authorization Security Testing**

APIGUARDIAN is a developer-focused security workspace for defining expected API access decisions and comparing them with observed authorization behaviour. Its goal is to help teams catch object-level and function-level authorization regressions before release.

> **Responsible-use boundary:** Test only applications you own or are explicitly authorized to assess. The current release is an early foundation and UI preview. It does not scan arbitrary public targets.

## Current implementation status

| Component | Status |
|---|---|
| React + TypeScript dashboard preview | Implemented; synthetic data only |
| Spring Boot service | Initial foundation |
| Health endpoint | Implemented at `/api/v1/health` |
| Deterministic policy evaluator | Implemented at `POST /api/v1/policies/evaluate` |
| Policy evaluator unit tests | Added; not yet run in this environment |
| CI workflow | Added; first GitHub Actions run pending |
| Authentication and multi-tenancy | Planned |
| OpenAPI import and endpoint inventory | Planned |
| Authorized API test runner | Planned; local demo target first |
| Persistent findings and run history | Planned |
| PostgreSQL/Flyway integration | Planned |

Dashboard figures and findings are illustrative examples, not measured security results. The current dashboard actions replay local UI state and do not send requests to a target API.

## Technology

- Backend: Java 21, Spring Boot 3.4, Maven
- Frontend: React 18, TypeScript, Vite
- Tests: JUnit 5
- Delivery: Dockerfiles, Docker Compose and GitHub Actions foundation

## Run locally

### Backend
Prerequisites: JDK 21 and Maven 3.9+.

```bash
cd backend
mvn test
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/api/v1/health
```

Policy evaluation example:

```bash
curl -X POST http://localhost:8080/api/v1/policies/evaluate \
  -H 'Content-Type: application/json' \
  -d '{"rule":{"role":"USER","resource":"admin:settings","action":"READ","expected":"DENY"},"observed":"ALLOW"}'
```

### Frontend
Prerequisites: Node.js 20+.

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.

## Documentation

- [Product requirements](docs/PRODUCT-REQUIREMENTS.md)
- [Architecture](docs/ARCHITECTURE.md)
- [API design](docs/API-DESIGN.md)
- [Threat model](docs/THREAT-MODEL.md)
- [Security and responsible use](docs/SECURITY.md)
- [Development guide](docs/DEVELOPMENT.md)
- [Roadmap](docs/ROADMAP.md)

## Next milestones

1. Add authentication and tenant-scoped workspaces.
2. Add persistent projects, endpoint inventory and policy storage.
3. Add OpenAPI parsing with strict upload limits.
4. Build an isolated local demo API with synthetic identities and owner-scoped records.
5. Implement a bounded test runner that targets only the bundled demo by default.
6. Persist runs, evidence and findings; add regression comparison and report export.

## Non-goals

APIGUARDIAN is not an exploit framework, internet-wide scanner, credential harvesting tool or substitute for a professional penetration test. Automated findings require human review.

## License

MIT. See [LICENSE](LICENSE).
