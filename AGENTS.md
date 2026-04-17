# Netatmo Weather Station MCP Server - Agent Guide

## Project Overview

Quarkus REST + MCP server connecting to Netatmo Weather Station API via OAuth2 refresh token auth. Exposes weather data via:
- **MCP endpoint**: `http://localhost:8091/mcp` (Streamable HTTP, 2025-03-26 protocol)
- **REST endpoints**: `/weather/stations`, `/weather/current`, `/weather/devices`, `/weather/historical`

## Quick Start Commands

### Development Mode
```bash
# Load env vars from .env file
export $(cat .env | xargs)
./mvnw quarkus:dev
# Runs on port 8091 in dev mode
```

### Package and Run (JVM)
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Build
```bash
./mvnw package -Pnative
# Use Mandrel container by default: quarkus.native.container-build=true
```

## Environment Setup

Required env vars (see `.env` for example):
- `NETATMO_API_BASE_URL=https://api.netatmo.com`
- `NETATMO_API_CLIENT_ID=<from Netatmo Connect>`
- `NETATMO_API_CLIENT_SECRET=<from Netatmo Connect>`
- `NETATMO_API_REFRESH_TOKEN=<OAuth2 refresh token>`

Load via `.env` file or Kubernetes secrets:
```bash
kubectl create secret generic netatmo --from-env-file=./.env
```

## Architecture

### Package Boundaries
| Package | Purpose |
|---------|---------|
| `com.kevindubois.service.*` | Core business logic (WeatherService, NetatmoAuthService) |
| `com.kevindubois.mcp.*` | MCP tool implementations |
| `com.kevindubois.client.*` | Netatmo REST client interface |
| `com.kevindubois.filter.*` | Auth filter for OAuth2 token injection |
| `com.kevindubois.dto.*` | Response DTOs and data models |

### Entry Points
- **REST**: `WeatherStationResource.java` (`@Path("/weather")`)
- **MCP**: `WeatherMcpTools.java` (exposes 3 tools: get_current_weather, get_available_devices, get_historical_weather)

## Testing

### Run All Tests
```bash
./mvnw test
```

### Run Single Test Class
```bash
./mvnw test -Dtest=WeatherStationResourceTest
```

### Integration Tests (require running server)
Located in `src/test/java/com/kevindubois/*IT.java`

## Build Artifacts

- **JVM jar**: `target/quarkus-app/quarkus-run.jar`
- **Native binary**: `target/netatmo` (when `-Pnative`)
- **Docker images**: Built via `quarkus image build` or Dockerfiles in `src/main/docker/`

## Quarkus Profiles

- `%dev` → port 8091, dev services enabled
- `%prod` → Kubernetes secrets integration, Knative deployment target

## Important Notes

### OAuth2 Flow
Uses refresh token authentication. Access tokens are obtained automatically on each request via `NetatmoAuthService.getAccessToken()`. No user interaction needed after initial setup.

### Caching Strategy
`WeatherService` uses a simple `HashMap<String, CacheEntry<?>>` cache with 60s TTL for:
- Device list (`DEVICE_LIST_CACHE_KEY`)
- Station data per device ID (`STATION_DATA_CACHE_PREFIX + deviceId`)

**Note**: This cache is not thread-safe and persists across requests. Consider using Quarkus Arc CDI scope or `@Singleton` beans for production workloads requiring concurrent access.

### Client Singleton Scope
The Netatmo REST client is configured with `jakarta.inject.Singleton` in `application.properties`. Ensure singleton beans don't hold stale state across requests; the auth service handles token refresh automatically.

### Kubernetes Deployment
- Knative scale-to-zero enabled (`quarkus.knative.scale-to-zero-enabled=true`)
- Readiness probe period: 1s
- Resource limits: 2 CPU, 400Mi memory
- JVM options: `-XX:+UseParallelGC -XX:MaxRAMPercentage=75`

### Codegen
Quarkus generates code during build via `quarkus-maven-plugin`. Run `./mvnw quarkus:generate-code` if needed.

## CI Workflow

GitHub Actions (`.github/workflows/maven.yml`) runs:
1. Checkout
2. Setup JDK 25 (Temurin)
3. Maven package (`mvn -B package --file pom.xml`)

No lint/typecheck steps configured yet; add as needed.

## Code Assistant Help
When the app is running in dev mode, an additional endpoint is exposed at: `http://localhost:8091/q/dev-mcp` . 
This endpoint provides tools that can help code assistants to interact with the running application and get real time runtime information.
It should be configured as an MCP server to the code assistant. 

If the port in the application.properties is different, then use that one instead of the 8091 port in this file.
