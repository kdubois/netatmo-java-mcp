package org.acme.mcp;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

import org.acme.dto.ApiResponse;
import org.acme.service.WeatherService;

@Singleton
public class WeatherMcpTools {

    @Inject
    WeatherService weatherService;

    @Tool(name = "get_current_weather", description = "Get current weather data from Netatmo weather station")
    public TextContent getCurrentWeather() {
        try {
            var apiResponse = weatherService.getCurrentWeather();
            
            if (!apiResponse.isSuccess()) {
                return new TextContent("Error: " + apiResponse.getMessage());
            }
            
            // Return JSON data instead of formatted text
            return ApiResponse.success(apiResponse.getData(), "Successfully retrieved current weather data").toTextContent();
        } catch (Exception e) {
            return new TextContent("Error: " + e.getMessage());
        }
    }

    @Tool(name = "get_available_devices", description = "Get list of available Netatmo weather station devices")
    public TextContent getAvailableDevices() {
        try {
            var apiResponse = weatherService.getAvailableDevices();
            
            if (!apiResponse.isSuccess()) {
                return new TextContent("Error: " + apiResponse.getMessage());
            }
            
            // Return JSON data instead of formatted text
            return ApiResponse.success(apiResponse.getData(), "Successfully retrieved available devices").toTextContent();
        } catch (Exception e) {
            return new TextContent("Error: " + e.getMessage());
        }
    }

    @Tool(name = "get_historical_weather", description = "Get historical weather data from Netatmo weather station for a specified date range. Returns data in JSON format.")
    public TextContent getHistoricalWeather(
            @ToolArg(description = "Device ID (optional, uses first available device if not provided)", required = false) String deviceId,
            @ToolArg(description = "Scale: 30min, 1hour, 3hours, 1day, 1week, 1month (default: 1hour)", required = false) String scale,
            @ToolArg(description = "Sensor types comma-separated: Temperature,Humidity,Pressure,CO2,Noise (default: Temperature,Humidity,Pressure)", required = false) String sensorTypes,
            @ToolArg(description = "Begin date in format YYYY-MM-DD (default: 7 days ago)", required = false) String beginDate,
            @ToolArg(description = "End date in format YYYY-MM-DD (default: current date)", required = false) String endDate,
            @ToolArg(description = "Maximum number of data points to return (default: all)", required = false) String maxDataPoints
    ) {
        try {
            // Parse and normalize parameters
            final Integer maxPoints = parseMaxDataPoints(maxDataPoints);

            // Call service to get historical data with direct date parameters
            var apiResponse = weatherService.getHistoricalWeather(
                deviceId,
                null,
                scale,
                sensorTypes,
                beginDate,
                endDate,
                null
            );
            
            if (!apiResponse.isSuccess()) {
                return new TextContent("Error: " + apiResponse.getMessage());
            }
            
            // Get the data map
            Map<String, Object> data = apiResponse.getData();
            
            // Apply max data points limit if needed
            if (maxPoints != null && maxPoints > 0) {
                limitDataPoints(data, maxPoints);
            }
            
            // Return JSON data using ApiResponse
            return ApiResponse.success(data, "Successfully retrieved historical weather data").toTextContent();
        } catch (Exception e) {
            return new TextContent("Error: " + e.getMessage());
        }
    }
    
    /**
     * Limit the number of data points in the result map
     */
    private void limitDataPoints(Map<String, Object> data, int maxPoints) {
        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) data.get("values");
        
        if (values != null && values.size() > maxPoints) {
            List<Object> limitedValues = values.subList(0, maxPoints);
            data.put("values", limitedValues);
            data.put("limitedDataPoints", true);
            data.put("displayedDataPoints", maxPoints);
            data.put("totalDataPoints", values.size());
        }
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
}
