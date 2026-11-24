# API design

Base path: `/api/v1`. JSON request/response bodies. Authenticated endpoints require a signed JWT in the Authorization bearer header.

## Authentication

| Method | Path | Purpose |
|---|---|---|
| POST | /auth/register | Create user, workspace and owner membership |
| POST | /auth/login | Verify credentials and issue access token |

Registration password minimum: 12 characters. Passwords are BCrypt-hashed. Access tokens are signed HS256 JWTs and include issuer and expiry claims.

## Workspaces and projects

| Method | Path | Purpose |
|---|---|---|
| GET | /workspaces/{workspaceId}/projects | List projects for a workspace the caller belongs to |
| POST | /workspaces/{workspaceId}/projects | Create a project in a workspace the caller belongs to |

Workspace membership is verified server-side on every request. Unknown or inaccessible workspace/project identifiers return 404 to avoid confirming their existence.

## Policies, evaluations, demo runs and findings

| Method | Path | Purpose |
|---|---|---|
| GET | /projects/{projectId}/policies | List saved authorization policies |
| POST | /projects/{projectId}/policies | Create or update an expected access decision |
| POST | /projects/{projectId}/policies/evaluate | Compare expected and caller-supplied observed decision; persist evaluation |
| GET | /projects/{projectId}/policies/evaluations | List the latest 100 persisted evaluations |
| POST | /projects/{projectId}/runs/demo | Execute bounded synthetic authorization cases against the bundled local demo model |
| GET | /projects/{projectId}/runs | List the latest 100 demo runs and pass/fail totals |
| GET | /projects/{projectId}/findings | List project findings |
| PATCH | /projects/{projectId}/findings/{findingId} | Update finding status to OPEN, ACKNOWLEDGED or RESOLVED |

An unexpected ALLOW where DENY was expected is classified CRITICAL in this initial rule set. Unexpected DENY is classified MEDIUM. Severity policy will be configurable in a later milestone.

## System

| Method | Path | Purpose |
|---|---|---|
| GET | /health | Public service health |

## Important limitation

The current evaluation endpoint compares an observed decision supplied by the authenticated caller. It does not independently call a customer target API. The bundled demo runner evaluates saved policies against a fixed local synthetic authorization model; it does not scan external hosts or make network requests to customer systems. OpenAPI import and authorized target integrations remain future milestones; no arbitrary target URL is accepted by the current API.
