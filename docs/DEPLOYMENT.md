# Deployment guide

## Supported baseline

The production container stack consists of PostgreSQL 16, the Spring Boot API, and an Nginx-served React build. PostgreSQL is on a private Docker network and has no published host port. The browser reaches the API through the same-origin `/api` reverse-proxy path.

This is a deployment baseline, not a claim of independent production security certification. Complete the checklist below for the target environment before accepting real customer data.

## Prerequisites

- Linux VPS or managed Docker host
- Docker Engine and Docker Compose v2
- A DNS name pointing to the server
- Firewall access for TCP 80 and 443
- Caddy installed on the host for automatic HTTPS

## Configure secrets

```bash
cp .env.example .env
chmod 600 .env
```

Replace `DATABASE_PASSWORD` with a unique high-entropy password. Generate a JWT signing secret with:

```bash
openssl rand -base64 48
```

Put the generated value in `JWT_SECRET`. Never commit `.env`, share production secrets in chat, or reuse the sample values. Rotate secrets through a planned maintenance procedure.

## Start the application

```bash
docker compose build
docker compose up -d
docker compose ps
docker compose logs --tail=100 api web db
```

The database schema is applied by Flyway when the API starts. The web container listens on the configured loopback-only host port (default 8088).

## HTTPS reverse proxy

Install Caddy using the official instructions for your Linux distribution. Set your DNS A/AAAA records to the server, then copy the root `Caddyfile` to the Caddy configuration directory and replace `api.example.com` with your domain. Validate and reload Caddy. Caddy obtains and renews certificates automatically when DNS and firewall requirements are satisfied.

Do not expose port 8088 publicly. Only ports 80 and 443 should be reachable from the internet. The sample Compose file binds the web port to 127.0.0.1.

## Verify

- `https://YOUR_DOMAIN/healthz` returns `ok`.
- `https://YOUR_DOMAIN/api/v1/health` returns service health.
- Register a test user and create a test workspace/project.
- Create a policy and evaluate matching and mismatching outcomes.
- Verify one workspace member cannot access another workspace's project.
- Confirm PostgreSQL is not exposed on a public interface.

## Backups

Create encrypted, off-host backups and test restores regularly. Example manual database dump:

```bash
docker compose exec -T db pg_dump -U apiguardian apiguardian > apiguardian-$(date +%F).sql
```

Encrypt the dump before moving it off the host. Define retention, recovery-point and recovery-time objectives for your deployment. Do not rely on the Docker volume as a backup.

## Operational hardening checklist

- [ ] Set unique production database and JWT secrets.
- [ ] Restrict inbound ports to 80/443; keep SSH limited to administrators.
- [ ] Configure host security updates and Docker updates.
- [ ] Enable encrypted off-host backups and test restore.
- [ ] Configure uptime, application and database monitoring.
- [ ] Set log retention and redact sensitive values.
- [ ] Establish incident response and secret-rotation procedures.
- [ ] Review privacy notice, terms, retention and applicable legal requirements.
- [ ] Conduct independent security review and load testing before public launch.

## Current product limitations

The initial release supports registration/login, workspace-scoped projects, policy storage and deterministic policy evaluation. It does not yet include external API test execution, OpenAPI import, persistent run history/findings, MFA, password reset, invitation flows or billing. Do not market these as implemented until their code and tests are delivered.
