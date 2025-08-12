# Netatmo Weather Station API

A Quarkus REST AND MCP Server that connects to the Netatmo Weather Station API and retrieves weather data.

## Features

- OAuth2 authentication with Netatmo API using refresh tokens
- RESTful endpoints to retrieve weather station data
- JSON response models for all Netatmo API data structures
- Support for retrieving data from all stations or specific devices
- Simplified current weather data endpoint

## Endpoints

### Get All Weather Stations Data
```
GET /weather/stations
```
Returns complete weather station data including all devices and modules.

### Get Specific Device Data
```
GET /weather/stations/{deviceId}
```
Returns weather station data for a specific device ID.

### Get Current Weather Data
```
GET /weather/current
```
Returns simplified current weather data (temperature, humidity, pressure, CO2, noise) from the first station.

### Get Available Devices
```
GET /weather/devices
```
Returns a list of available weather station devices with their IDs, names, types, and supported data types. Use this to get device IDs for historical data requests.

### Get Historical Weather Data
```
GET /weather/historical
```
Returns historical weather data. If no device_id is provided, automatically uses the first available device. Supports query parameters:
- `device_id` (optional): Specific device ID. If not provided, uses the first available device.
- `module_id` (optional): Specific module ID 
- `scale` (optional): Data granularity (30min, 1hour, 3hours, 1day, 1week, 1month). Default: 1hour
- `type` (optional): Sensor types (Temperature, Humidity, Pressure, CO2, Noise, Rain, WindStrength, WindAngle, GustStrength, GustAngle). Default: Temperature,Humidity,Pressure
- `date_begin` (optional): Unix timestamp for start date. Default: 7 days ago
- `date_end` (optional): Unix timestamp for end date. Default: now
- `limit` (optional): Maximum number of data points. Default: 1024

### Get Historical Weather Data for Specific Device
```
GET /weather/historical/{deviceId}
```
Returns historical weather data for a specific device. Supports same query parameters as above.

### Get Processed Historical Weather Data
```
GET /weather/historical/processed
```
Returns historical weather data in a processed, more user-friendly format with structured timestamps and measurements. Supports same query parameters as `/weather/historical`. This endpoint provides:
- Separated timestamps and measurement values
- Cleaner data structure for easier consumption
- Both raw and processed data in the response

### Get Simple Historical Weather Data
```
GET /weather/historical/simple
```
Returns basic information about the historical data request without complex nested structures. This endpoint is useful for debugging and understanding the Netatmo API response structure. Supports same query parameters as `/weather/historical`. This endpoint provides:
- Basic request parameters (device_id, scale, sensor types)
- Response status from Netatmo API
- Debug information about the response body structure
- No complex nested objects that might cause serialization issues

### Get Working Historical Weather Data
```
GET /weather/historical/working
```
Returns properly parsed historical weather data based on the actual Netatmo API response structure. Supports same query parameters as `/weather/historical`. This endpoint provides:
- Correctly parsed measurement data from Netatmo's `{beg_time, step_time, value}` structure
- Begin time (Unix timestamp when measurements start)
- Step time (interval between measurements in seconds)
- Values array (the actual historical measurements)
- Total data points count
- Clean, usable format for consuming historical weather data

### Health Check
```
GET /weather
```
Returns a simple status message.

## Model Context Protocol (MCP) Server

This application also exposes weather data as MCP tools that can be used by AI assistants and other MCP clients.

### MCP Endpoints

- **Main MCP endpoint**: `http://localhost:8080/mcp` (Streamable HTTP - 2025-03-26 protocol)
- **SSE endpoint**: `http://localhost:8080/mcp/sse` (HTTP/SSE - 2024-11-05 protocol)

### Available MCP Tools

#### `get_current_weather`
Gets current weather data from the Netatmo weather station.
- Returns temperature, humidity, pressure, CO2, noise levels
- Includes both indoor and outdoor measurements

#### `get_available_devices` 
Lists all available Netatmo weather station devices.
- Shows device IDs, station names, types, and supported data types
- Useful for getting device IDs for historical data requests

#### `get_historical_weather`
Gets historical weather data from the Netatmo weather station.
- **Parameters:**
  - `deviceId` (optional): Device ID, uses first device if not provided
  - `scale` (optional): Data granularity (30min, 1hour, 3hours, 1day, 1week, 1month), default: 1hour
  - `sensorTypes` (optional): Comma-separated sensor types (Temperature,Humidity,Pressure,CO2,Noise), default: Temperature,Humidity,Pressure
  - `daysBack` (optional): Number of days back to get data, default: 7

### Using MCP Tools

MCP tools can be used by:
- AI assistants (Claude, GPT, etc.) that support MCP
- MCP clients and inspectors
- Custom applications using MCP protocol

Example MCP client usage:
```bash
# Connect to the MCP server
curl -X POST http://localhost:8080/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"method": "tools/call", "params": {"name": "get_current_weather"}}'
```

## Configuration

The application is configured via `application.properties`:

```properties
# Netatmo API Configuration
netatmo.api.base.url=https://api.netatmo.com

# OAuth2 Configuration for Netatmo
quarkus.oidc-client.netatmo.auth-server-url=${netatmo.api.base.url}/oauth2
quarkus.oidc-client.netatmo.client-id=YOUR_CLIENT_ID
quarkus.oidc-client.netatmo.client-secret=YOUR_CLIENT_SECRET
quarkus.oidc-client.netatmo.credentials.secret=YOUR_CLIENT_SECRET
quarkus.oidc-client.netatmo.grant.type=refresh_token
quarkus.oidc-client.netatmo.refresh-token=YOUR_REFRESH_TOKEN
quarkus.oidc-client.netatmo.scopes=read_station

# REST Client Configuration
quarkus.rest-client.netatmo-api.url=${netatmo.api.base.url}
quarkus.rest-client.netatmo-api.scope=jakarta.inject.Singleton

# MCP Server Configuration
quarkus.mcp.server.traffic-logging.enabled=true
quarkus.mcp.server.traffic-logging.text-limit=1000
quarkus.mcp.server.sse.root-path=mcp
```

## Setup Requirements

1. **Netatmo Developer Account**: Create an application at [Netatmo Connect](https://dev.netatmo.com/)
2. **OAuth2 Credentials**: Obtain `client_id`, `client_secret`, and `refresh_token`
3. **Weather Station**: Have a registered Netatmo weather station

## Authentication Flow

This application uses OAuth2 refresh token authentication. The refresh token is used to automatically obtain access tokens for API calls without requiring user interaction.

## Data Models

The application includes comprehensive DTOs for:
- `NetatmoStationsDataResponse` - Main API response wrapper
- `Device` - Weather station device information
- `Module` - Individual sensor modules (outdoor, indoor, etc.)
- `DashboardData` - Current measurements and trends
- `Place` - Location information

## Example Response

```json
{
  "temperature": 22.5,
  "humidity": 65,
  "pressure": 1013.2,
  "co2": 456,
  "noise": 42,
  "stationName": "Home Weather Station",
  "timeUtc": 1640995200
}
```

## Running the Application

```bash
# Development mode
./mvnw quarkus:dev

# Package and run
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## Dependencies

- Quarkus REST with Jackson
- Quarkus OIDC Client for OAuth2 authentication
- Quarkus REST Client for HTTP calls