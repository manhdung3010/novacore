# Docker deployment (Novacore)

## Assumptions

- You have Docker Desktop (Compose v2) installed.
- The backend is a Spring Boot app listening on port `5001` (avoids conflict with Jenkins on 8080).
- Infra dependencies are run via `docker/docker-compose.yml` using the `infra` profile.

## High-level design

- **Infra stack**: PostgreSQL + Redis + Kafka (+ Zookeeper) + Kafka UI live on a shared Docker network `novacore-network`.
- **App stack**: the `app` service builds from the repo root `Dockerfile` and connects to infra services via the same `novacore-network`.
- **Production mode**: `docker/docker-compose.prod.yml` overrides the infra compose to avoid exposing DB/Redis/Kafka ports publicly and tightens defaults.

## Code

### Local: start infra

From repo root:

```bash
docker compose -f docker/docker-compose.yml --profile infra up -d
```

Check:

```bash
docker compose -f docker/docker-compose.yml --profile infra ps
```

### Local: build & run the app

The app compose expects the infra network to exist (`novacore-network` is external in `docker-compose.app.yml`).

```bash
docker compose -f docker/docker-compose.app.yml up -d --build
```

Logs:

```bash
docker compose -f docker/docker-compose.app.yml logs -f app
```

Stop:

```bash
docker compose -f docker/docker-compose.app.yml down
docker compose -f docker/docker-compose.yml --profile infra down
```

### Environment variables

Infra compose supports these (with defaults for local):

- `POSTGRES_DB` (default `novacore`)
- `POSTGRES_USER` (default `novacore`)
- `POSTGRES_PASSWORD` (default `changeme`)
- `POSTGRES_PORT` (default `5432`)
- `REDIS_PASSWORD` (default `changeme`)
- `REDIS_PORT` (default `6379`)
- `KAFKA_PORT` (default `9092`)
- `KAFKA_UI_PORT` (default `8090`)
- `ZOOKEEPER_PORT` (default `2181`)

App compose reads:

- `POSTGRES_USER` / `POSTGRES_PASSWORD`
- `REDIS_PASSWORD`

### Production-ish infra (no public ports)

```bash
docker compose -f docker/docker-compose.yml -f docker/docker-compose.prod.yml --profile infra up -d
```

Notes:

- This only affects infra services in `docker-compose.yml`.
- The app exposure (e.g. `5001:5001`) is controlled by `docker-compose.app.yml`.

### Troubleshooting

- **Redis unhealthy**: ensure `REDIS_PASSWORD` matches between infra and app; infra healthcheck uses `redis-cli -a "$REDIS_PASSWORD" ping`.
- **App can’t reach Postgres/Redis/Kafka**: make sure infra is up and `novacore-network` exists:

```bash
docker network ls | findstr novacore-network
```

- **Rebuild app image cleanly**:

```bash
docker compose -f docker/docker-compose.app.yml build --no-cache
```

## Edge cases handled

- **Redis healthcheck with password**: healthcheck now authenticates, so containers don’t get stuck in `unhealthy` state when `requirepass` is enabled.
- **Compose v2 pathing**: production override usage line matches the actual repo paths (`docker/...`) and `docker compose` syntax.

## Possible improvements

- Add a non-root user + filesystem permissions in `Dockerfile` runtime stage.
- Add an application `HEALTHCHECK` (e.g. Spring Actuator `/actuator/health`) once confirmed enabled in this project.
- Pin `kafka-ui` image tag instead of `latest` for reproducibility.
