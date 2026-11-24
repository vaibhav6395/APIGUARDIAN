# Hosting-platform preparation

APIGUARDIAN is prepared as a single Docker web image plus an external PostgreSQL database. This is intentionally provider-neutral: build and run the root `Dockerfile` on a Docker-capable host. No deployment is performed by this repository setup.

## What the hosting platform runs

- One web service built from the repository root `Dockerfile`.
- One managed PostgreSQL database (PostgreSQL 16 recommended).
- The React frontend is compiled during the image build and served by Spring Boot from the same origin as the API. This avoids a separately configured frontend-to-backend hostname and CORS setup.
- The platform routes public HTTPS traffic to the container's assigned port.

## Required environment variables

Configure these in the hosting platform's secret/environment settings. Do not commit real values.

| Variable | Required | Description |
|---|---|---|
| `PORT` | Platform-provided | Port the container must listen on. The app defaults to 8080 if absent. |
| `SPRING_DATASOURCE_URL` | Yes | JDBC URL, e.g. `jdbc:postgresql://HOST:5432/DB?sslmode=require` |
| `DATABASE_USERNAME` | Yes | Managed PostgreSQL username |
| `DATABASE_PASSWORD` | Yes | Managed PostgreSQL password |
| `JWT_SECRET` | Yes | Random secret with at least 32 characters; use a secret manager |
| `JWT_ISSUER` | Recommended | Stable issuer identifier, e.g. `apiguardian-production` |
| `JWT_EXPIRATION_MINUTES` | Optional | Access-token lifetime; defaults to 60 |

If your provider supplies a PostgreSQL URI such as `postgres://...`, do not paste that URI into `SPRING_DATASOURCE_URL` unchanged. Convert it to a JDBC URL and set username/password separately, or use provider-specific connection-variable mapping. Keep TLS enabled when the managed database requires it.

Generate a JWT secret locally with `openssl rand -base64 48`. Never use the sample value from `.env.example` in a hosted environment.

## Build and start

- Build context: repository root
- Dockerfile: `Dockerfile`
- Build command: platform default Docker build
- Start command: image default entrypoint; no custom command is required
- Health/readiness path: `/actuator/health/readiness`
- Liveness path: `/actuator/health/liveness`
- Public smoke-check path: `/api/v1/health`

The app listens on `PORT` (or 8080 locally). Run Flyway migrations automatically at startup. Provision a persistent managed PostgreSQL database before starting the service.

## Generic provider setup sequence

1. Create a PostgreSQL 16 database in the hosting provider.
2. Create a Docker web service from this Git repository and use the root Dockerfile.
3. Set the required environment variables above using the provider's secret manager.
4. Ensure the service forwards HTTPS traffic to the container port supplied in `PORT`.
5. Set the readiness check to `/actuator/health/readiness`.
6. Deploy a staging instance first, then test registration, login, workspace isolation, policy CRUD, local demo runs, findings and a database restart/restore.
7. Configure database backups, log retention, alerting and a documented rollback process before accepting real user data.

This workflow can be adapted to Render, Railway, Fly.io or another Docker-capable host. Exact dashboard labels and managed database connection details vary by provider; use the provider's current documentation rather than copying credentials into source control.

## Local image verification

```bash
docker build -t apiguardian:local .
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/apiguardian' \
  -e DATABASE_USERNAME=apiguardian \
  -e DATABASE_PASSWORD='your-local-db-password' \
  -e JWT_SECRET="$(openssl rand -base64 48)" \
  apiguardian:local
```

The example expects a PostgreSQL server reachable from the container. It is a local smoke-test command, not a production secret-management pattern.

## Hosting-readiness boundaries

The Docker image and configuration make the application portable; they do not by themselves certify production readiness. Before real users/data, complete the security test plan, independent review, backup/restore test, monitoring, rate-limit review, privacy/legal review, and an incident-response plan. Features listed as not implemented in the README remain out of scope for this deployment package.
