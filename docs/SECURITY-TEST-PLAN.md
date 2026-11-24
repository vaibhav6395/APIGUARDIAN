# Security test plan

## Automated checks
- Registration validation and password hashing.
- Login rejects invalid credentials.
- JWTs are signed with HS256 and expire.
- Unauthenticated requests to protected endpoints are rejected.
- Workspace membership is checked server-side.
- A user from another workspace cannot list or create projects in it.
- Policy evaluation reports a mismatch when observed and expected decisions differ.
- Invalid request payloads receive a client error.

## Required before production
- Tenant isolation tests across every new resource endpoint.
- Rate limiting for authentication and sensitive APIs.
- Password reset and account recovery threat review.
- Token revocation/rotation strategy and short-lived access tokens.
- Audit logging for authentication and authorization changes.
- OpenAPI upload size/type/parser limits.
- Runner SSRF, DNS rebinding, private-network and egress controls.
- Container image and dependency scanning.
- Backup restore, load and failure-recovery exercises.

## Scope
All tests must use local or explicitly authorized systems and synthetic data. Do not use production customer credentials or data in test fixtures.
