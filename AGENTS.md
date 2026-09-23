# Netatmo MCP Server - Agent Guide

## Overview

Quarkus MCP Server + REST API for Netatmo Weather Station. Port 8091 in dev mode.

## Commands

```bash
./mvnw quarkus:dev          # Dev mode (port 8091, Keycloak Dev Services auto-start)
./mvnw test                 # Run tests (McpAssured + REST)
./mvnw package -Pnative     # Native build (Mandrel container)
```

## Architecture

| Package | Purpose |
|---------|---------|
| `com.kevindubois.mcp` | MCP tools, resources, prompts (`WeatherMcpTools`, `WeatherMcpResources`, `WeatherMcpPrompts`, `AdminMcpTools`) |
| `com.kevindubois.service` | Business logic (`WeatherService`, `NetatmoAuthService`) |
| `com.kevindubois.client` | Netatmo REST client interface |
| `com.kevindubois.filter` | OAuth2 auth filter |
| `com.kevindubois.dto` | DTOs and response models |

## MCP Servers

- **Public** (`/mcp`): 3 tools (`get_current_weather`, `get_available_devices`, `get_historical_weather`) + resources + prompts. No auth.
- **Admin** (`/admin/mcp`): `@McpServer("admin")` + `@RolesAllowed("admin")`: `refresh_station_data`, `get_station_diagnostics`, `compare_periods`, `run_anomaly_scan`. Requires OIDC bearer token.
- Protocol: Streamable HTTP, protocol version 2026-07-28 (stateful + stateless).
- CORS: `quarkus.http.cors.*` allows the homebot dashboard origin (`http://localhost:8080`) as a
  cross-origin browser MCP client. The enforced `Mcp-Method`/`Mcp-Name`/`Mcp-Param-*` headers must
  stay in `quarkus.http.cors.headers` or the browser preflight is rejected.

## Key Files

- `WeatherMcpTools.java` — Public tools with annotations, validation, progress, error handling, smart defaults (auto-scale, auto-sensor-types, 24-point cap)
- `AdminMcpTools.java` — Admin tools (`@McpServer("admin")`)
- `WeatherMcpResources.java` — `@Resource` and `@ResourceTemplate` for weather data
- `WeatherMcpPrompts.java` — `@Prompt` templates + `@CompletePrompt` auto-completion
- `WeatherMcpTest.java` — McpAssured integration tests
- `explorer.html` — Browser-based MCP Explorer UI (vanilla JS, calls `/mcp` and `/admin/mcp` via JSON-RPC)

## Auth (OIDC)

Keycloak Dev Services auto-starts. Users: `alice/alice` (admin,user), `bob/bob` (user). Test profile disables OIDC (`%test.quarkus.oidc.enabled=false`).

## Env Vars

Required in `.env`: `NETATMO_API_CLIENT_ID`, `NETATMO_API_CLIENT_SECRET`, `NETATMO_API_REFRESH_TOKEN`.

## Notes

- `get_available_devices` returns the main (NAMain) module **and** its nested outdoor modules (NAModule1, under `modules`). Without the nested modules the AI chat assumed no outdoor sensor existed.
- `get_historical_weather` single-day ranges (beginDate == endDate) work: Netatmo omits `step_time` for single-point responses, so `WeatherService.effectiveStep` falls back to the scale's step in seconds.
- Netatmo upstream is flaky (transient 503s/403s/500s). `NetatmoHistoricalDataFetcher` wraps every
  historical-data REST call with SmallRye Fault Tolerance `@Retry(retryOn =
  ClientWebApplicationException.class, maxRetries = 2, delay = 500ms)` — the MP REST client throws
  that exception for any non-2xx, so both the indoor and outdoor module fetches retry transient
  failures instead of degrading to indoor-only data. When the outdoor fetch still fails,
  `getHistoricalWeather` sets `outdoorDataAvailable: false` in the result so callers can see the
  degradation. The AI chat system message still instructs the LLM to retry once on a 503 that
  survives the client-side retries.
- Cache: HashMap with 60s TTL (not thread-safe, fine for demo)
- OAuth2: Refresh token auth via `NetatmoAuthService.getAccessToken()`
- MCP version: `quarkus-mcp-server 2.0.1`
- Dev MCP endpoint for code assistants: `http://localhost:8091/q/dev-mcp`
- MRTR: `get_historical_weather` uses elicitation (`Elicitation` + `ToolCallException` with `inputRequired`) for ranges over 30 days. `ElicitationRequest.builder()` does not exist in 2.0.1; use `elicitation.requestBuilder()`. `checkLargeRange` branches on `elicitation.isFormModeSupported()`: elicitation-capable clients (the homebot dashboard browser declares `clientCapabilities: {elicitation: {}}`) get the `confirm_range` form; clients without the capability (the homebot chat's backend client declares `clientCapabilities: {}`) get a clean actionable `ToolCallException` telling the LLM not to retry and to output the `[RANGE_CONFIRM]` marker — the homebot chat UI then shows a confirm card and the browser re-runs the tool itself.
- Cache hints: `tools.ttl-ms`/`tools.cache-scope`, `prompts.ttl-ms`/`prompts.cache-scope` (must be paired) in `application.properties`; `@Resource.CacheControl(ttlMs, cacheScope)` on resources.
- `refresh_station_data` fires `notifications/resources/updated` via `ResourceManager.sendUpdateAndForget()`.
- Tests: `%test.quarkus.oidc.enabled=false`; admin tools reachable via `TestAdminIdentityProvider` (basic auth `admin/admin` → `admin` role). Stateless test clients: `.setStateless()`; MRTR tests need the `ELICITATION` client capability.
- OTel: `quarkus-opentelemetry` enabled, OTLP export to `localhost:4317` (connection-refused warnings in tests are expected).
