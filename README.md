# Netatmo Weather Station MCP Server (and optional JSON REST endpoint as well)

A Quarkus REST AND MCP Server that connects to the Netatmo Weather Station API and retrieves weather data.

## Features

- OAuth2 authentication with Netatmo API using refresh tokens
- RESTful endpoints to retrieve weather station data
- JSON response models for all Netatmo API data structures
- Support for retrieving data from all stations or specific devices
- Simplified current weather data endpoint
- Historical weather data with flexible parameters
- Model Context Protocol (MCP) tools for AI assistants

## Endpoints

### Get All Weather Stations Data

```http
GET /weather/stations
```

Returns complete weather station data including all devices and modules.

### Get Current Weather Data

```http
GET /weather/current
```

Returns simplified current weather data (temperature, humidity, pressure, CO2, noise) from the first station.

### Get Available Devices

```http
GET /weather/devices
```

Returns a list of available weather station devices with their IDs, names, types, and supported data types. Use this to get device IDs for historical data requests.

### Get Historical Weather Data

```http
GET /weather/historical
```

Returns historical weather data. If no device_id is provided, automatically uses the first available device. Supports query parameters:
- `device_id` (optional): Specific device ID. If not provided, uses the first available device.
- `module_id` (optional): Specific module ID 
- `scale` (optional): Data granularity (30min, 1hour, 3hours, 1day, 1week, 1month). Default: 1hour
- `type` (optional): Sensor types (Temperature, Humidity, Pressure, CO2, Noise, Rain, WindStrength, WindAngle, GustStrength, GustAngle). Default: Temperature,Humidity,Pressure
- `date_begin` (optional): Unix timestamp or ISO date (YYYY-MM-DD) for start date. Default: 7 days ago
- `date_end` (optional): Unix timestamp or ISO date (YYYY-MM-DD) for end date. Default: now
- `limit` (optional): Maximum number of data points. Default: 1024

### Health Check

```http
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
- Returns data in JSON format

#### `get_available_devices`

Lists all available Netatmo weather station devices.
- Shows device IDs, station names, types, and supported data types
- Useful for getting device IDs for historical data requests
- Returns data in JSON format

#### `get_historical_weather`

Gets historical weather data from the Netatmo weather station.
- **Parameters:**
  - `deviceId` (optional): Device ID, uses first device if not provided
  - `scale` (optional): Data granularity (30min, 1hour, 3hours, 1day, 1week, 1month), default: 1hour
  - `sensorTypes` (optional): Comma-separated sensor types (Temperature,Humidity,Pressure,CO2,Noise), default: Temperature,Humidity,Pressure
  - `beginDate` (optional): Begin date in format YYYY-MM-DD, default: 7 days ago
  - `endDate` (optional): End date in format YYYY-MM-DD, default: current date
  - `maxDataPoints` (optional): Maximum number of data points to return, default: all
- Returns data in JSON format

### Using MCP Tools

MCP tools can be used by:
- AI assistants (Claude, GPT, etc.) that support MCP
- MCP clients and inspectors
- Custom applications using MCP protocol

Example MCP client usage:

```bash
# Connect to the MCP server
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"method": "tools/call", "params": {"name": "get_current_weather"}}'
```

## Configuration

The application is configured via environment variables:

```properties
# Netatmo API Configuration
NETATMO_API_BASE_URL=https://api.netatmo.com
NETATMO_API_CLIENT_ID=YOUR_CLIENT_ID
NETATMO_API_CLIENT_SECRET=YOUR_CLIENT_SECRET
NETATMO_API_REFRESH_TOKEN=YOUR_REFRESH_TOKEN
```

These are referenced in `application.properties`:

```properties
# Netatmo API Configuration
netatmo.api.base.url=${NETATMO_API_BASE_URL}
netatmo.api.client-id=${NETATMO_API_CLIENT_ID}
netatmo.api.client-secret=${NETATMO_API_CLIENT_SECRET}
netatmo.api.refresh-token=${NETATMO_API_REFRESH_TOKEN}

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
4. **Environment Variables**: Set the required environment variables

## Authentication Flow

This application uses OAuth2 refresh token authentication. The refresh token is used to automatically obtain access tokens for API calls without requiring user interaction.

## Data Models

The application includes comprehensive DTOs for:
- `ApiResponse` - Generic response wrapper for consistent formatting
- `NetatmoStationsDataResponse` - Main API response wrapper
- `CurrentWeatherData` - Simplified current weather data
- `HistoricalWeatherData` - Historical weather measurements
- `DeviceInfo` - Weather station device information
- `BaseResult` - Base class for result objects

## Example Response

```json
{
  "success": true,
  "message": "Successfully retrieved current weather data",
  "data": {
    "temperature": 22.5,
    "humidity": 65,
    "pressure": 1013.2,
    "co2": 456,
    "noise": 42,
    "stationName": "Home Weather Station",
    "timeUtc": 1640995200
  },
  "status": 200
}
```

## Running the Application

```bash
# Set environment variables
export NETATMO_API_BASE_URL=https://api.netatmo.com
export NETATMO_API_CLIENT_ID=your_client_id
export NETATMO_API_CLIENT_SECRET=your_client_secret
export NETATMO_API_REFRESH_TOKEN=your_refresh_token

# Development mode
./mvnw quarkus:dev

# Package and run
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## Container Support

The application includes container support with several Dockerfile options (in src/main/docker):

```bash
# Build using the Quarkus CLI:
quarkus image build

# Build using mvn and native compilation:
quarkus image build --native

# Build using docker, in JVM mode
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/netatmo .

# Run the container
docker run -i --rm -p 8080:8080 \
  -e NETATMO_API_BASE_URL=https://api.netatmo.com \
  -e NETATMO_API_CLIENT_ID=your_client_id \
  -e NETATMO_API_CLIENT_SECRET=your_client_secret \
  -e NETATMO_API_REFRESH_TOKEN=your_refresh_token \
  quarkus/netatmo
```

## Dependencies

- Quarkus REST with Jackson
- Quarkus OIDC Client for OAuth2 authentication
- Quarkus REST Client for HTTP calls
- Quarkus MCP Server for Model Context Protocol support
