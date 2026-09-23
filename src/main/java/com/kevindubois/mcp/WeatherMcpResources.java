package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.CacheScope;
import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.RequestUri;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.Resource.Annotations;
import io.quarkiverse.mcp.server.Resource.CacheControl;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.Role;
import io.quarkiverse.mcp.server.TextResourceContents;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.kevindubois.dto.ApiResponse;
import com.kevindubois.service.WeatherService;

@Singleton
public class WeatherMcpResources {

    @Inject
    WeatherService weatherService;

    @Resource(uri = "weather:///current",
              description = "Current weather conditions from the Netatmo weather station including indoor/outdoor readings",
              annotations = @Annotations(audience = Role.ASSISTANT, priority = 1.0),
              cacheControl = @CacheControl(ttlMs = 60000, cacheScope = CacheScope.PRIVATE))
    public TextResourceContents currentWeather(RequestUri uri, McpLog log) {
        log.info("Reading current weather resource");

        var apiResponse = weatherService.getCurrentWeather();
        if (!apiResponse.isSuccess()) {
            return new TextResourceContents(uri.value(),
                "Error: " + apiResponse.getMessage(), "application/json");
        }

        try {
            String json = ApiResponse.toJsonString(apiResponse.getData());
            return new TextResourceContents(uri.value(), json, "application/json");
        } catch (Exception e) {
            return new TextResourceContents(uri.value(),
                "Error serializing weather data: " + e.getMessage(), "text/plain");
        }
    }

    @Resource(uri = "weather:///devices",
              description = "List of available Netatmo weather station devices",
              annotations = @Annotations(audience = Role.ASSISTANT, priority = 0.8),
              cacheControl = @CacheControl(ttlMs = 60000, cacheScope = CacheScope.PRIVATE))
    public TextResourceContents devices(RequestUri uri, McpLog log) {
        log.info("Reading devices resource");

        var apiResponse = weatherService.getAvailableDevices();
        if (!apiResponse.isSuccess()) {
            return new TextResourceContents(uri.value(),
                "Error: " + apiResponse.getMessage(), "application/json");
        }

        try {
            String json = ApiResponse.toJsonString(apiResponse.getData());
            return new TextResourceContents(uri.value(), json, "application/json");
        } catch (Exception e) {
            return new TextResourceContents(uri.value(),
                "Error serializing device data: " + e.getMessage(), "text/plain");
        }
    }

    @ResourceTemplate(uriTemplate = "weather:///{deviceId}/current",
                      description = "Current weather data for a specific device by its ID",
                      annotations = @Annotations(audience = Role.ASSISTANT, priority = 0.9),
                      cacheControl = @CacheControl(ttlMs = 60000, cacheScope = CacheScope.PRIVATE))
    public TextResourceContents deviceWeather(String deviceId, RequestUri uri, McpLog log) {
        log.info("Reading weather resource for device: %s", deviceId);

        var stationResponse = weatherService.fetchStation(deviceId);

        if (stationResponse.getBody() == null ||
            stationResponse.getBody().getDevices() == null ||
            stationResponse.getBody().getDevices().isEmpty()) {
            return new TextResourceContents(uri.value(),
                "No data available for device: " + deviceId, "text/plain");
        }

        var device = stationResponse.getBody().getDevices().get(0);

        try {
            String json = ApiResponse.toJsonString(device.getDashboardData());
            return new TextResourceContents(uri.value(), json, "application/json");
        } catch (Exception e) {
            return new TextResourceContents(uri.value(),
                "Error serializing device data: " + e.getMessage(), "text/plain");
        }
    }
}
