# API design (planned contract)

Base path: `/api/v1`

| Method | Path | Purpose |
|---|---|---|
| GET | /health | Service health |
| GET | /projects | List projects visible to caller |
| POST | /projects | Create a project |
| POST | /projects/{id}/specifications | Import an OpenAPI document |
| GET | /projects/{id}/endpoints | List normalized endpoints |
| POST | /projects/{id}/policies | Create/update expected authorization policy |
| POST | /projects/{id}/runs | Start bounded authorized test run |
| GET | /projects/{id}/runs | List run history |
| GET | /findings | List findings visible to caller |
| PATCH | /findings/{id} | Update finding status/assignee |

All project-scoped operations must verify organization membership and project access server-side. Request and response schemas will be published through generated OpenAPI documentation as endpoints are implemented.
