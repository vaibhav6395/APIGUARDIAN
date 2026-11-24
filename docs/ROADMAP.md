# Roadmap

## Foundation and first usable milestone
- [x] Product direction and responsible-use boundaries
- [x] Architecture and initial threat model
- [x] React dashboard foundation
- [x] Registration and login with password hashing and signed JWTs
- [x] Workspace membership and project CRUD foundation
- [x] Persisted authorization policies
- [x] Persisted evaluation history
- [x] Findings list and status updates
- [x] Flyway schema migrations
- [x] Docker Compose stack and CI workflow
- [x] PostgreSQL-backed integration tests for auth and tenant isolation

## Functional API security MVP
- [ ] OpenAPI import with strict parsing and upload limits
- [ ] Endpoint inventory and operation metadata
- [ ] Bundled synthetic demo API with owner-scoped resources
- [ ] Bounded local authorization regression runner
- [ ] Persisted run grouping and evidence
- [ ] Run-to-run regression comparison
- [ ] Report export

## Collaboration and SaaS hardening
- [ ] Workspace invitations and member roles
- [ ] Role-aware write permissions
- [ ] MFA and password recovery
- [ ] Token revocation/refresh strategy
- [ ] Rate limiting and authentication abuse controls
- [ ] Audit trail for security-sensitive changes
- [ ] Usage limits and billing integration

## Production readiness
- [ ] Independent security review and remediation
- [ ] Load and resilience testing
- [ ] Backup restore and disaster recovery exercise
- [ ] Monitoring, alerting and retention policies
- [ ] External target support only after ownership verification, SSRF and egress controls
