package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.Elicitation;
import io.quarkiverse.mcp.server.ElicitationRequest;
import io.quarkiverse.mcp.server.ElicitationResponse;
import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Progress;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.Tool.Annotations;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.WrapBusinessError;
import io.quarkiverse.mcp.server.http.McpParamHeader;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    public TextContent getCurrentWeather(
            @McpParamHeader("x-client-name")
            @ToolArg(description = "Client display name (mirrored as the x-client-name HTTP header)", required = false) String clientName,
            McpLog log) {
        log.info("Fetching current weather data from Netatmo API (client-name: %s)", clientName);

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

    private static final int DEFAULT_MAX_DATA_POINTS = 24;

    @Tool(name = "get_historical_weather",
          description = "Get historical weather data from Netatmo weather station for a specified date range. Returns timestamped measurements in JSON format. Data is automatically summarized for large ranges (daily aggregates for >7 days, capped at 24 data points by default).",
          annotations = @Annotations(
              title = "Historical Weather",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public TextContent getHistoricalWeather(
            @ToolArg(description = "Device ID (optional, uses first available device if not provided)", required = false) String deviceId,
            @Pattern(regexp = "(30min|1hour|3hours|1day|1week|1month)?", message = "Scale must be one of: 30min, 1hour, 3hours, 1day, 1week, 1month")
            @ToolArg(description = "Scale: 30min, 1hour, 3hours, 1day, 1week, 1month (default: auto-selected based on date range)", required = false) String scale,
            @ToolArg(description = "Sensor types comma-separated. Available: Temperature,min_temp,max_temp,Humidity,min_hum,max_hum,Pressure,CO2,Noise. For daily/weekly scale use min_temp,max_temp to get daily highs/lows. Default: auto-selected based on scale.", required = false) String sensorTypes,
            @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})?", message = "Begin date must be in format YYYY-MM-DD")
            @ToolArg(description = "Begin date in format YYYY-MM-DD (default: 7 days ago)", required = false) String beginDate,
            @Pattern(regexp = "(\\d{4}-\\d{2}-\\d{2})?", message = "End date must be in format YYYY-MM-DD")
            @ToolArg(description = "End date in format YYYY-MM-DD (default: current date)", required = false) String endDate,
            @ToolArg(description = "Maximum number of data points to return (default: 24)", required = false) String maxDataPoints,
            Progress progress,
            McpLog log,
            Elicitation elicitation
    ) {
        log.info("Requesting historical weather data");

        final Integer maxPoints = parseMaxDataPoints(maxDataPoints);
        LocalDate begin = beginDate != null ? LocalDate.parse(beginDate) : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        long days = ChronoUnit.DAYS.between(begin, end);
        checkLargeRange(days, begin, end, elicitation);

        String effectiveScale = autoSelectScale(scale, beginDate, endDate);
        String effectiveSensorTypes = autoSelectSensorTypes(sensorTypes, effectiveScale);

        sendProgress(progress, "Resolving device and querying Netatmo API", 0);

        var apiResponse = weatherService.getHistoricalWeather(
            deviceId,
            null,
            effectiveScale,
            effectiveSensorTypes,
            beginDate,
            endDate,
            null
        );

        if (!apiResponse.isSuccess()) {
            throw new ToolCallException(apiResponse.getMessage());
        }

        sendProgress(progress, "Processing results", 70);

        Map<String, Object> data = apiResponse.getData();
        int limit = (maxPoints != null && maxPoints > 0) ? maxPoints : DEFAULT_MAX_DATA_POINTS;
        limitDataPoints(data, limit);

        sendProgress(progress, "Complete", 100);

        log.info("Retrieved historical weather data (scale=%s, limit=%d)", effectiveScale, limit);
        return ApiResponse.success(data, "Successfully retrieved historical weather data").toTextContent();
    }

    private static final long MAX_RANGE_DAYS = 30;
    private static final String CONFIRM_RANGE_KEY = "confirm_range";

    private void checkLargeRange(long days, LocalDate begin, LocalDate end, Elicitation elicitation) {
        if (days <= MAX_RANGE_DAYS) {
            return;
        }
        if (elicitation.isServerInitiatedRequestSupported()) {
            throw new ToolCallException(
                "Requested range of " + days + " days is too large (max " + MAX_RANGE_DAYS + " days).");
        }
        // A client that does not declare the elicitation capability (e.g. the backend
        // chat client) cannot answer an interactive confirm. Return a clean, actionable
        // result instead of throwing a form-mode capability error it cannot act on.
        if (!elicitation.isFormModeSupported()) {
            throw new ToolCallException(
                "Range of " + days + " days exceeds the " + MAX_RANGE_DAYS + "-day limit and this client " +
                "cannot confirm it interactively, so it was not executed. Do not retry this call and do " +
                "not shrink the range. Tell the user it needs a one-time confirmation and output the " +
                "range-confirmation block using the exact dates you just used.");
        }
        if (elicitation.inputResponses().isEmpty()) {
            throw elicitation.inputRequired()
                .addElicitationRequest(CONFIRM_RANGE_KEY,
                    elicitation.requestBuilder()
                        .setMessage("The requested range covers " + days + " days. Fetch it anyway?")
                        .addSchemaProperty("confirm",
                            ElicitationRequest.BooleanSchema.builder()
                                .setTitle("Confirm")
                                .setDescription("Set to true to fetch the full range")
                                .setRequired(true)
                                .build())
                        .build())
                .setRequestState("confirm:" + begin + ":" + end)
                .build();
        }
        if (elicitation.inputResponses().has(CONFIRM_RANGE_KEY)) {
            ElicitationResponse r = elicitation.inputResponses().getElicitationResponse(CONFIRM_RANGE_KEY);
            boolean confirm = r != null && r.actionAccepted()
                && Boolean.TRUE.equals(r.content().getBoolean("confirm"));
            if (!confirm) {
                throw new ToolCallException("Range confirmation was declined.");
            }
        }
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

    private String autoSelectSensorTypes(String sensorTypes, String scale) {
        boolean isDailyOrLonger = "1day".equals(scale) || "1week".equals(scale) || "1month".equals(scale);
        if (sensorTypes != null && !sensorTypes.isBlank()) {
            // For daily+ scales, replace plain "Temperature" with min/max to get proper highs/lows
            if (isDailyOrLonger) {
                sensorTypes = sensorTypes.replace("Temperature", "min_temp,max_temp");
            }
            return sensorTypes;
        }
        if (isDailyOrLonger) {
            return "min_temp,max_temp,Humidity";
        }
        return "Temperature,Humidity,Pressure";
    }

    private String autoSelectScale(String scale, String beginDate, String endDate) {
        if (scale != null && !scale.isBlank()) {
            return scale;
        }
        try {
            LocalDate begin = beginDate != null ? LocalDate.parse(beginDate) : LocalDate.now().minusDays(7);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            long days = ChronoUnit.DAYS.between(begin, end);
            if (days > 30) return "1week";
            if (days > 7) return "1day";
            if (days > 2) return "3hours";
            return "1hour";
        } catch (Exception e) {
            return "1day";
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
