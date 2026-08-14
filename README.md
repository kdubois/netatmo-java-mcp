# Netatmo Weather Station MCP Server

Quarkus MCP Server that connects to the Netatmo Weather Station API. Also exposes REST endpoints.

## MCP Server

### Endpoints

| Endpoint | Protocol | Auth |
|----------|----------|------|
| `/mcp` | Streamable HTTP (2025-03-26) | None |
| `/admin/mcp` | Streamable HTTP (2025-03-26) | OIDC (admin role) |

### Tools

| Tool | Description | Server |
|------|-------------|--------|
| `get_current_weather` | Current indoor/outdoor temperature, humidity, pressure, CO2, noise | public |
| `get_available_devices` | List devices with IDs, names, types | public |
| `get_historical_weather` | Historical data with auto-scaling and daily high/low support | public |
| `refreshStationData` | Force refresh of station data cache | admin |
| `getStationDiagnostics` | Station health and connectivity diagnostics | admin |

### Resources

| URI | Description |
|-----|-------------|
| `weather:///current` | Current weather as JSON |
| `weather:///devices` | Device list as JSON |
| `weather:///{deviceId}/current` | Device-specific weather (template) |

### Prompts

| Name | Description |
|------|-------------|
| `weather_summary` | Generate a weather summary for all devices |
| `weather_comparison` | Compare weather across a date range |
| `device_diagnostics` | Diagnose a specific device (with auto-completion) |

### Key Features

- **Tool annotations**: readOnlyHint, idempotentHint, destructiveHint, openWorldHint
- **Progress notifications**: Historical data tool reports progress
- **Validation**: `@Pattern` on date/scale parameters via Hibernate Validator
- **Error handling**: `@WrapBusinessError` + `ToolCallException` for structured errors
- **Smart defaults**: Auto-selects scale (daily for >7d) and sensor types (min_temp/max_temp for daily+)
- **Data capping**: Default 24 data points max to prevent LLM context overflow

### MCP Explorer UI

A visual dashboard at `/explorer.html` for interacting with the MCP server directly from the browser. Has tabs for Tools, Resources, Prompts, and Admin (with Keycloak token acquisition).

## Authentication & Authorization

Uses OIDC with Keycloak Dev Services (auto-starts in dev mode):
- `alice/alice` → admin + user roles
- `bob/bob` → user role only

The admin MCP server at `/admin/mcp` requires a bearer token with the `admin` role.

## REST Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/weather/stations` | All station data |
| GET | `/weather/current` | Current weather |
| GET | `/weather/devices` | Device list |
| GET | `/weather/historical` | Historical data (query params: device_id, scale, type, date_begin, date_end, limit) |

## Configuration

Required env vars (put in `.env` file):
```properties
NETATMO_API_CLIENT_ID=<from Netatmo Connect>
NETATMO_API_CLIENT_SECRET=<from Netatmo Connect>
NETATMO_API_REFRESH_TOKEN=<OAuth2 refresh token>
```

## Running

```bash
./mvnw quarkus:dev    # Dev mode, port 8091, Keycloak Dev Services auto-start
./mvnw package        # Build
```

## Dependencies

- Quarkus MCP Server 2.0.0.CR1
- Quarkus MCP Server Hibernate Validator
- Quarkus MCP Server Test (McpAssured)
- Quarkus OIDC + Keycloak Dev Services
- Quarkus REST Client (Netatmo API)
