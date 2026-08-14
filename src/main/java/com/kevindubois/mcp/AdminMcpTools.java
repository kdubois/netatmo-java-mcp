package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.McpServer;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.Tool.Annotations;
import io.quarkiverse.mcp.server.WrapBusinessError;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.kevindubois.service.WeatherService;

@Singleton
@McpServer("admin")
@WrapBusinessError
@RolesAllowed("admin")
public class AdminMcpTools {

    @Inject
    WeatherService weatherService;

    @Tool(name = "refresh_station_data",
          description = "Force a refresh of the weather station data cache by fetching fresh data from the Netatmo API.",
          annotations = @Annotations(
              title = "Refresh Station Data",
              readOnlyHint = false,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public String refreshStationData(McpLog log) {
        log.info("Forcing station data refresh");

        var response = weatherService.fetchAllStations();
        int deviceCount = 0;
        if (response.getBody() != null && response.getBody().getDevices() != null) {
            deviceCount = response.getBody().getDevices().size();
        }

        log.info("Refreshed data for %d device(s)", deviceCount);
        return "Successfully refreshed station data. Found " + deviceCount + " device(s).";
    }

    @Tool(name = "get_station_diagnostics",
          description = "Get diagnostic information about all weather stations including connectivity status and data freshness.",
          annotations = @Annotations(
              title = "Station Diagnostics",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public String getStationDiagnostics(McpLog log) {
        log.info("Running station diagnostics");

        var devicesResponse = weatherService.getAvailableDevices();
        if (!devicesResponse.isSuccess()) {
            return "Diagnostics failed: " + devicesResponse.getMessage();
        }

        var currentResponse = weatherService.getCurrentWeather();

        StringBuilder diag = new StringBuilder();
        diag.append("=== Station Diagnostics ===\n");
        diag.append("Devices found: ").append(devicesResponse.getData().size()).append("\n");

        for (var device : devicesResponse.getData()) {
            diag.append("\n--- Device: ").append(device.name()).append(" ---\n");
            diag.append("  ID: ").append(device.id()).append("\n");
            diag.append("  Type: ").append(device.type()).append("\n");
            diag.append("  Data types: ").append(device.dataTypes()).append("\n");
        }

        if (currentResponse.isSuccess()) {
            var data = currentResponse.getData();
            diag.append("\n--- Latest Reading ---\n");
            diag.append("  Station: ").append(data.stationName()).append("\n");
            diag.append("  Last update (UTC): ").append(data.timeUtc()).append("\n");
            long ageSeconds = (System.currentTimeMillis() / 1000) - (data.timeUtc() != null ? data.timeUtc() : 0);
            diag.append("  Data age: ").append(ageSeconds).append(" seconds\n");
            if (ageSeconds > 3600) {
                diag.append("  WARNING: Data is more than 1 hour old!\n");
            }
        }

        log.info("Diagnostics complete");
        return diag.toString();
    }
}
