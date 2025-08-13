package org.acme.mcp;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acme.dto.HistoricalWeatherData;
import org.acme.exception.WeatherApiException;
import org.acme.exception.WeatherApiException.ErrorType;
import org.acme.service.WeatherService;
import org.acme.util.ResponseUtil.ResultWithStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Singleton
public class WeatherMcpTools {

    @Inject
    WeatherService weatherService;
    
    // Reusable ObjectMapper instance configured once
    private final ObjectMapper objectMapper;
    
    public WeatherMcpTools() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

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

    @Tool(name = "get_historical_weather", description = "Get historical weather data from Netatmo weather station for a specified date range. Returns data in JSON format.")
    public TextContent getHistoricalWeather(
            @ToolArg(description = "Device ID (optional, uses first device if not provided)") String deviceId,
            @ToolArg(description = "Scale: 30min, 1hour, 3hours, 1day, 1week, 1month (default: 1hour)") String scale,
            @ToolArg(description = "Sensor types comma-separated: Temperature,Humidity,Pressure,CO2,Noise (default: Temperature,Humidity,Pressure)") String sensorTypes,
            @ToolArg(description = "Begin date in format YYYY-MM-DD (default: 7 days ago)") String beginDate,
            @ToolArg(description = "End date in format YYYY-MM-DD (default: current date)") String endDate,
            @ToolArg(description = "Maximum number of data points to return (default: all)") String maxDataPoints
    ) {
        return McpResponseUtil.executeWithExceptionHandling(() -> {
            // Parse and normalize parameters
            final Integer maxPoints = parseMaxDataPoints(maxDataPoints);
            
            // Parse date parameters
            Long dateBegin = parseBeginDate(beginDate);
            Long dateEnd = parseEndDate(endDate);
            
            // Call service to get historical data with direct date parameters
            HistoricalWeatherData result = weatherService.getHistoricalWeather(
                deviceId,
                null,
                scale,
                sensorTypes,
                beginDate,
                endDate,
                null
            );
            
            return McpResponseUtil.handleResult((ResultWithStatus)result, () -> {
                // Convert result to JSON
                return convertToJson(result, maxPoints);
            });
        });
    }

    /**
     * Parse the max data points parameter
     */
    private Integer parseMaxDataPoints(String maxDataPoints) {
        if (maxDataPoints != null && !maxDataPoints.trim().isEmpty()) {
            try {
                return Integer.parseInt(maxDataPoints.trim());
            } catch (NumberFormatException e) {
                return null; // Return all data points
            }
        }
        return null; // Return all data points
    }

    /**
     * Convert HistoricalWeatherData to JSON string
     *
     * @param data The historical weather data to convert
     * @param maxDataPoints Optional limit on the number of data points to include
     * @return JSON string representation of the data
     */
    private String convertToJson(HistoricalWeatherData data, Integer maxDataPoints) {
        try {
            // Create a map to hold the data
            Map<String, Object> jsonMap = new HashMap<>();
            
            // Populate the map with data
            populateBasicFields(jsonMap, data);
            populateOutdoorModuleData(jsonMap, data);
            populateDataPoints(jsonMap, data, maxDataPoints);
            
            // Convert to JSON string
            return objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new WeatherApiException("Failed to serialize weather data to JSON", e, ErrorType.SERVER_ERROR);
        }
    }
    
    /**
     * Populate the JSON map with basic fields from the data object
     */
    private void populateBasicFields(Map<String, Object> jsonMap, HistoricalWeatherData data) {
        jsonMap.put("success", data.success);
        jsonMap.put("deviceId", data.deviceId);
        jsonMap.put("scale", data.scale);
        jsonMap.put("sensorTypes", data.sensorTypes);
        jsonMap.put("beginTime", data.getBeginTime());
        jsonMap.put("endTime", data.getEndTime());
        jsonMap.put("stepTime", data.stepTime);
        jsonMap.put("totalDataPoints", data.totalDataPoints);
    }
    
    /**
     * Populate the JSON map with outdoor module data if available
     */
    private void populateOutdoorModuleData(Map<String, Object> jsonMap, HistoricalWeatherData data) {
        if (data.outdoorModuleId == null) {
            return;
        }
        
        jsonMap.put("outdoorModuleId", data.outdoorModuleId);
        jsonMap.put("outdoorModuleName", data.outdoorModuleName);
        
        if (data.outdoorTemperature != null) {
            jsonMap.put("outdoorTemperature", data.outdoorTemperature);
        }
        
        if (data.outdoorHumidity != null) {
            jsonMap.put("outdoorHumidity", data.outdoorHumidity);
        }
    }
    
    /**
     * Populate the JSON map with data points, optionally limiting the number
     */
    private void populateDataPoints(Map<String, Object> jsonMap, HistoricalWeatherData data, Integer maxDataPoints) {
        if (data.values == null || data.values.isEmpty()) {
            jsonMap.put("values", Collections.emptyList());
            return;
        }
        
        List<Object> limitedValues;
        boolean isLimited = maxDataPoints != null && maxDataPoints > 0 && maxDataPoints < data.values.size();
        
        if (isLimited) {
            limitedValues = data.values.subList(0, maxDataPoints);
            jsonMap.put("limitedDataPoints", true);
            jsonMap.put("displayedDataPoints", maxDataPoints);
        } else {
            limitedValues = data.values;
            jsonMap.put("limitedDataPoints", false);
            jsonMap.put("displayedDataPoints", data.values.size());
        }
        
        jsonMap.put("values", limitedValues);
    }

    // Removed unused methods formatList and formatValue as they are not needed
    // with the Jackson ObjectMapper approach
}
