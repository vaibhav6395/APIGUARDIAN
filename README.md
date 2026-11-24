# APIGUARDIAN

**API authorization policy management and regression review**

APIGUARDIAN is a defensive security workspace for teams that need to document expected API access decisions, compare observed outcomes, and track authorization mismatches. Use it only for systems you own or are explicitly authorized to assess.

## Current implementation

| Capability | Status |
|---|---|
| Registration and login | Implemented with BCrypt and signed HS256 JWTs |
| Workspace creation | Implemented; registration creates an owner workspace |
| Project management | Implemented; workspace membership enforced server-side |
| Authorization policies | Implemented; stored per project |
| Policy evaluation | Implemented; expected and observed decisions compared |
| Local demo runner | Implemented; fixed synthetic role/resource cases only |
| Test run history | Persisted with pass/fail totals |
| Evaluation history | Persisted in PostgreSQL |
| Findings | Persisted, listed and status-updatable |
| Dashboard | Connected to authenticated APIs |
| Database migrations | Flyway |
| Local development deployment | Docker Compose, PostgreSQL, Nginx |
| Hosting-platform image | Single Docker image with bundled React UI and Spring Boot API; external PostgreSQL |
| HTTPS instructions | Host Caddy reverse-proxy guide |
| CI | Backend integration tests and frontend build |
| OpenAPI import and customer target runner | Not yet implemented |
| MFA, password reset, invitations and billing | Not yet implemented |

**Important scope note:** Manual policy evaluation compares a caller-supplied observed decision with the expected policy. The bundled demo runner exercises a fixed local synthetic authorization model and records its results; it does not make requests to a customer API or independently discover vulnerabilities. Dashboard results must not be represented as proof that a customer API is secure.

## Stack

- Java 21, Spring Boot 3.4, Spring Security, JDBC
- PostgreSQL 16, Flyway
- React 18, TypeScript, Vite, Nginx
- JUnit 5, MockMvc, GitHub Actions
- Docker Compose

## Hosting-platform preparation

The root `Dockerfile` builds a single image containing the React frontend and Spring Boot API. Deploy it as one Docker web service and connect it to a managed PostgreSQL database. The app honors the host-provided `PORT` and supports an external JDBC database URL. See [Hosting platform preparation](docs/HOSTING-PLATFORM-READY.md). No hosting provider is selected or deployed automatically.

## Run with Docker Compose

Prerequisites: Docker Engine and Docker Compose v2.

```bash
cp .env.example .env
```

Edit `.env` and set a strong unique `DATABASE_PASSWORD` and a random `JWT_SECRET` (at least 32 characters). Then:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

Open http://127.0.0.1:8088 (or your configured `WEB_PORT`). The API is proxied through the web container at `/api`; PostgreSQL is not published to the host.

## Run checks locally

Backend requires Java 21, Maven 3.9+ and a reachable PostgreSQL database configured through `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`.

```bash
cd backend
mvn verify
```

Frontend:

```bash
cd frontend
npm install
npm test -- --passWithNoTests
npm run build
npm run dev
```

For local frontend development, Vite proxies `/api` to `http://localhost:8080`.

## API outline

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET|POST /api/v1/workspaces/{workspaceId}/projects`
- `GET|POST /api/v1/projects/{projectId}/policies`
- `POST /api/v1/projects/{projectId}/policies/evaluate`
- `POST /api/v1/projects/{projectId}/runs/demo`
- `GET /api/v1/projects/{projectId}/runs`
- `GET /api/v1/projects/{projectId}/findings`
- `PATCH /api/v1/projects/{projectId}/findings/{findingId}`
- `GET /api/v1/health`

Protected routes require `Authorization: Bearer <access-token>`. Workspace and project access is checked by the API, not trusted from frontend state.

## Documentation

- [Product requirements](docs/PRODUCT-REQUIREMENTS.md)
- [Architecture](docs/ARCHITECTURE.md)
- [API design](docs/API-DESIGN.md)
- [Threat model](docs/THREAT-MODEL.md)
- [Security policy](docs/SECURITY.md)
- [Security test plan](docs/SECURITY-TEST-PLAN.md)
- [Development guide](docs/DEVELOPMENT.md)
- [Deployment guide](docs/DEPLOYMENT.md)
- [Hosting-platform preparation](docs/HOSTING-PLATFORM-READY.md)
- [Roadmap](docs/ROADMAP.md)

## Production-readiness note

The repository includes a deployment baseline, not a certification that the service is production-secure for every environment. Before handling real customer data, complete the deployment checklist, set and protect secrets, configure HTTPS and backups, add rate limiting and operational monitoring, and arrange an independent security review. Live API target testing, OpenAPI import, account recovery, MFA and collaboration flows remain roadmap work.

## License

MIT. See [LICENSE](LICENSE).
