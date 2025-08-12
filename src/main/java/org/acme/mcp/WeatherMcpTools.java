package org.acme.mcp;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

import org.acme.dto.HistoricalWeatherData;
import org.acme.service.WeatherService;
import org.acme.util.HistoricalDataUtil;

@Singleton
public class WeatherMcpTools {

    @Inject
    WeatherService weatherService;

    @Tool(name = "get_current_weather", description = "Get current weather data from Netatmo weather station")
    public TextContent getCurrentWeather() {
        return McpResponseUtil.executeWithExceptionHandling(() -> {
            var result = weatherService.getCurrentWeather();
            
            return McpResponseUtil.handleResult(result, () -> {
                var response = new StringBuilder();
                response.append("Current Weather Data for ").append(result.data.stationName).append(":\n");
                response.append("Temperature: ").append(result.data.indoorTemperature).append("°C\n");
                response.append("Humidity: ").append(result.data.indoorHumidity).append("%\n");
                response.append("Pressure: ").append(result.data.pressure).append(" mbar\n");
                response.append("CO2: ").append(result.data.co2).append(" ppm\n");
                response.append("Noise: ").append(result.data.noise).append(" dB\n");
                
                if (result.data.outdoorTemperature != null) {
                    response.append("Outdoor Temperature: ").append(result.data.outdoorTemperature).append("°C\n");
                }
                if (result.data.outdoorHumidity != null) {
                    response.append("Outdoor Humidity: ").append(result.data.outdoorHumidity).append("%\n");
                }
                
                return response.toString();
            });
        });
    }

    @Tool(name = "get_available_devices", description = "Get list of available Netatmo weather station devices")
    public TextContent getAvailableDevices() {
        return McpResponseUtil.executeWithExceptionHandling(() -> {
            var result = weatherService.getAvailableDevices();
            
            return McpResponseUtil.handleResult(result, () -> {
                var response = new StringBuilder();
                response.append("Available Weather Station Devices:\n");
                
                result.devices.forEach(device -> {
                    response.append("- Device ID: ").append(device.deviceId).append("\n");
                    response.append("  Station Name: ").append(device.stationName).append("\n");
                    response.append("  Type: ").append(device.type).append("\n");
                    response.append("  Data Types: ").append(device.dataTypes).append("\n\n");
                });
                
                return response.toString();
            });
        });
    }

@Tool(name = "get_historical_weather", description = "Get historical weather data from Netatmo weather station. Returns data in JSON format.")
    public TextContent getHistoricalWeather(
            @ToolArg(description = "Device ID (optional, uses first device if not provided)") String deviceId,
            @ToolArg(description = "Scale: 30min, 1hour, 3hours, 1day, 1week, 1month (default: 1hour)") String scale,
            @ToolArg(description = "Sensor types comma-separated: Temperature,Humidity,Pressure,CO2,Noise (default: Temperature,Humidity,Pressure)") String sensorTypes,
            @ToolArg(description = "Number of days back to get data (default: 7)") String daysBack
    ) {
        return McpResponseUtil.executeWithExceptionHandling(() -> {
            // Parse and normalize parameters
            Integer days = parseDaysBack(daysBack);
            final String normalizedScale = HistoricalDataUtil.normalizeScale(scale);
            final String normalizedSensorTypes = HistoricalDataUtil.normalizeSensorTypes(sensorTypes);
            
            var result = weatherService.getHistoricalWeather(deviceId, null, normalizedScale, normalizedSensorTypes, days, null);
            
            return McpResponseUtil.handleResult(result, () -> {
                // Create a JSON-like representation of the data
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"success\": ").append(result.success).append(",\n");
                json.append("  \"deviceId\": \"").append(result.deviceId).append("\",\n");
                json.append("  \"scale\": \"").append(result.scale).append("\",\n");
                json.append("  \"sensorTypes\": ").append(formatList(result.sensorTypes)).append(",\n");
                json.append("  \"beginTime\": \"").append(result.beginTime).append("\",\n");
                json.append("  \"stepTime\": ").append(result.stepTime).append(",\n");
                json.append("  \"totalDataPoints\": ").append(result.totalDataPoints).append(",\n");
                json.append("  \"daysBack\": ").append(result.daysBack).append(",\n");
                
                // Add outdoor module info if available
                if (result.outdoorModuleId != null) {
                    json.append("  \"outdoorModuleId\": \"").append(result.outdoorModuleId).append("\",\n");
                    json.append("  \"outdoorModuleName\": \"").append(result.outdoorModuleName).append("\",\n");
                    
                    if (result.outdoorTemperature != null) {
                        json.append("  \"outdoorTemperature\": ").append(result.outdoorTemperature).append(",\n");
                    }
                    
                    if (result.outdoorHumidity != null) {
                        json.append("  \"outdoorHumidity\": ").append(result.outdoorHumidity).append(",\n");
                    }
                }
                
                // Add data points (limited to first 5 for readability)
                json.append("  \"values\": [\n");
                if (result.values != null && !result.values.isEmpty()) {
                    int count = 0;
                    for (Object value : result.values) {
                        if (count > 0) {
                            json.append(",\n");
                        }
                        json.append("    ").append(formatValue(value));
                        count++;                        
                    }
                }
                json.append("\n  ]\n");
                json.append("}");
                
                return json.toString();
            });
        });
    }
        
    private Integer parseDaysBack(String daysBack) {
        if (daysBack != null && !daysBack.trim().isEmpty()) {
            try {
                return Integer.parseInt(daysBack.trim());
            } catch (NumberFormatException e) {
                return HistoricalDataUtil.DEFAULT_DAYS_BACK;
            }
        }
        return HistoricalDataUtil.DEFAULT_DAYS_BACK;
    }

    /**
     * Format a list as a JSON array
     */
    private String formatList(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(list.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Format a value as JSON
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            
            if (!list.isEmpty()) {
                sb.append("\"timestamp\": ").append(list.get(0));
                
                if (list.size() > 1) {
                    sb.append(", \"temperature\": ").append(list.get(1));
                }
                
                if (list.size() > 2) {
                    sb.append(", \"humidity\": ").append(list.get(2));
                }
                
                if (list.size() > 3) {
                    sb.append(", \"pressure\": ").append(list.get(3));
                }
            }
            
            sb.append("}");
            return sb.toString();
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return value.toString();
        }
    }
}
