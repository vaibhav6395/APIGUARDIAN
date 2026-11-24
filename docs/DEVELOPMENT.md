# Development guide

## Prerequisites
- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Engine and Docker Compose v2

## Local environment
1. Copy `.env.example` to `.env`.
2. Replace the local database password with a unique value.
3. Never commit `.env`.
4. Start services with `docker compose up --build` once the application services are present.

## Quality checks
- Backend: `mvn -f backend/pom.xml test`
- Frontend: `cd frontend && npm ci && npm test`
- Frontend production build: `npm run build`

Commands become executable as the corresponding modules are added. Check the root README and release notes for current implementation status.
