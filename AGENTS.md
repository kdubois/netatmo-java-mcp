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

- **Public** (`/mcp`): 3 tools + resources + prompts. No auth.
- **Admin** (`/admin/mcp`): `@McpServer("admin")` + `@RolesAllowed("admin")`. Requires OIDC bearer token.

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

- Cache: HashMap with 60s TTL (not thread-safe, fine for demo)
- OAuth2: Refresh token auth via `NetatmoAuthService.getAccessToken()`
- MCP version: `quarkus-mcp-server 2.0.0.CR1`
- Dev MCP endpoint for code assistants: `http://localhost:8091/q/dev-mcp`
