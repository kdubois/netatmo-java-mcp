# Netatmo Weather Station MCP Server

Quarkus MCP Server that connects to the Netatmo Weather Station API. Also exposes REST endpoints.

You can find a sample client app at https://github.com/kdubois/homebot

## MCP Server

### Endpoints

| Endpoint | Protocol | Auth |
|----------|----------|------|
| `/mcp` | Streamable HTTP (2026-07-28) | None |
| `/admin/mcp` | Streamable HTTP (2026-07-28) | OIDC (admin role) |

### Tools

| Tool | Description | Server |
|------|-------------|--------|
| `get_current_weather` | Current indoor/outdoor temperature, humidity, pressure, CO2, noise | public |
| `get_available_devices` | List devices with IDs, names, types | public |
| `get_historical_weather` | Historical data with auto-scaling and daily high/low support; ranges over 30 days require MRTR confirmation | public |
| `refresh_station_data` | Force refresh of station data cache, fires a `resources/updated` notification | admin |
| `get_station_diagnostics` | Data freshness and device-count diagnostics | admin |
| `compare_periods` | Compare two date ranges (per-day + aggregate) as structured content | admin |
| `run_anomaly_scan` | Scan recent data for CO2, humidity, temperature-jump and gap anomalies as structured content | admin |

### Resources

| URI | Description | Cache |
|-----|-------------|-------|
| `weather:///current` | Current weather as JSON | 60s, private |
| `weather:///devices` | Device list as JSON | 60s, private |
| `weather:///{deviceId}/current` | Device-specific weather (template) | 60s, private |

`refresh_station_data` fires a `notifications/resources/updated` for `weather:///current` after refreshing, so subscribed clients re-read the resource.

### Prompts

| Name | Description |
|------|-------------|
| `weather_summary` | Generate a weather summary for the current conditions |
| `device_diagnostics` | Analyze sensor readings for a device (with auto-completion) |

### Key Features

- **Tool annotations**: readOnlyHint, idempotentHint, destructiveHint, openWorldHint
- **Progress notifications**: Historical data tool reports progress
- **Validation**: `@Pattern` on date/scale parameters via Hibernate Validator
- **Error handling**: `@WrapBusinessError` + `ToolCallException` for structured errors
- **Smart defaults**: Auto-selects scale (daily for >7d) and sensor types (min_temp/max_temp for daily+)
- **Data capping**: Default 24 data points max to prevent LLM context overflow
- **MRTR (multi round-trip requests)**: `get_historical_weather` ranges over 30 days require a `confirm_range` confirmation. Elicitation-capable clients (declaring the `elicitation` capability) get the form-mode request and re-issue the call with `requestState` + `confirm`; clients without it (e.g. a backend chat client) get a clean, actionable error instead of a capability failure, so they can surface the confirmation to the user out-of-band.
- **Cache hints**: tools, prompts, and resources carry TTL/cache-scope hints (`tools.ttl-ms`, `prompts.ttl-ms`, `@Resource.CacheControl`)
- **Structured content**: `compare_periods` and `run_anomaly_scan` return typed JSON via `structuredContent = true`
- **Resilience**: Netatmo upstream is flaky (transient 503s). `NetatmoHistoricalDataFetcher` applies SmallRye Fault Tolerance `@Retry` to every historical-data call (indoor + outdoor module), so a single bad response is retried instead of degrading the result to indoor-only data; if the outdoor module is still unreachable, the result carries `outdoorDataAvailable: false`
- **Tracing**: OpenTelemetry instrumentation (`quarkus.opentelemetry`, OTLP export on `:4317`)
- **Client header**: `get_current_weather` reads the `x-client-name` request header via `@McpParamHeader`

### MCP Explorer UI

A visual dashboard at `/explorer.html` for interacting with the MCP server directly from the
browser. Has tabs for Tools, Resources, Prompts, and Admin (with Keycloak token acquisition). It
is the raw protocol tester; the user-facing demo lives in the homebot app's dashboard
(`http://localhost:8080/`), which also calls this server as a cross-origin browser MCP client
(CORS is configured for that origin, including the enforced `Mcp-*` headers).

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

