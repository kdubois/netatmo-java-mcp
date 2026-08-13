package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Progress;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.Tool.Annotations;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.WrapBusinessError;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;

import com.kevindubois.dto.ApiResponse;
import com.kevindubois.service.WeatherService;

@Singleton
@WrapBusinessError
public class WeatherMcpTools {

    @Inject
    WeatherService weatherService;

    @Tool(name = "get_current_weather",
          description = "Get current weather data from Netatmo weather station including indoor/outdoor temperature, humidity, pressure, CO2, and noise levels.",
          annotations = @Annotations(
              title = "Current Weather",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public TextContent getCurrentWeather(McpLog log) {
        log.info("Fetching current weather data from Netatmo API");

        var apiResponse = weatherService.getCurrentWeather();

        if (!apiResponse.isSuccess()) {
            throw new ToolCallException(apiResponse.getMessage());
        }

        log.info("Successfully retrieved current weather data");
        return ApiResponse.success(apiResponse.getData(), "Successfully retrieved current weather data").toTextContent();
    }

    @Tool(name = "get_available_devices",
          description = "Get list of available Netatmo weather station devices with their IDs, names, types, and supported data types.",
          annotations = @Annotations(
              title = "Available Devices",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public TextContent getAvailableDevices(McpLog log) {
        log.info("Fetching available devices from Netatmo API");

        var apiResponse = weatherService.getAvailableDevices();

        if (!apiResponse.isSuccess()) {
            throw new ToolCallException(apiResponse.getMessage());
        }

        log.info("Found %d device(s)", apiResponse.getData().size());
        return ApiResponse.success(apiResponse.getData(), "Successfully retrieved available devices").toTextContent();
    }

    @Tool(name = "get_historical_weather",
          description = "Get historical weather data from Netatmo weather station for a specified date range. Returns timestamped measurements in JSON format.",
          annotations = @Annotations(
              title = "Historical Weather",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public TextContent getHistoricalWeather(
            @ToolArg(description = "Device ID (optional, uses first available device if not provided)", required = false) String deviceId,
            @Pattern(regexp = "(30min|1hour|3hours|1day|1week|1month)?", message = "Scale must be one of: 30min, 1hour, 3hours, 1day, 1week, 1month")
            @ToolArg(description = "Scale: 30min, 1hour, 3hours, 1day, 1week, 1month (default: 1hour)", required = false) String scale,
            @ToolArg(description = "Sensor types comma-separated: Temperature,Humidity,Pressure,CO2,Noise (default: Temperature,Humidity,Pressure)", required = false) String sensorTypes,
            @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})?", message = "Begin date must be in format YYYY-MM-DD")
            @ToolArg(description = "Begin date in format YYYY-MM-DD (default: 7 days ago)", required = false) String beginDate,
            @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})?", message = "End date must be in format YYYY-MM-DD")
            @ToolArg(description = "End date in format YYYY-MM-DD (default: current date)", required = false) String endDate,
            @ToolArg(description = "Maximum number of data points to return (default: all)", required = false) String maxDataPoints,
            Progress progress,
            McpLog log
    ) {
        log.info("Requesting historical weather data");

        final Integer maxPoints = parseMaxDataPoints(maxDataPoints);

        sendProgress(progress, "Resolving device and querying Netatmo API", 0);

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
            throw new ToolCallException(apiResponse.getMessage());
        }

        sendProgress(progress, "Processing results", 70);

        Map<String, Object> data = apiResponse.getData();

        if (maxPoints != null && maxPoints > 0) {
            limitDataPoints(data, maxPoints);
        }

        sendProgress(progress, "Complete", 100);

        log.info("Retrieved %d data points", data.getOrDefault("totalDataPoints", 0));
        return ApiResponse.success(data, "Successfully retrieved historical weather data").toTextContent();
    }

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

    private Integer parseMaxDataPoints(String maxDataPoints) {
        if (maxDataPoints != null && !maxDataPoints.trim().isEmpty()) {
            try {
                return Integer.parseInt(maxDataPoints.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void sendProgress(Progress progress, String message, long value) {
        if (progress.token().isPresent()) {
            progress.notificationBuilder()
                .setMessage(message)
                .setProgress(value).setTotal(100)
                .build().sendAndForget();
        }
    }
}
