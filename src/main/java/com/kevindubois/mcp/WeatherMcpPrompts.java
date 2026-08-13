package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.CompletePrompt;
import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.TextContent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

import com.kevindubois.service.WeatherService;

@Singleton
public class WeatherMcpPrompts {

    @Inject
    WeatherService weatherService;

    @Prompt(description = "Generate a weather summary for the current conditions at the station")
    public List<PromptMessage> weather_summary(
            @PromptArg(description = "Style of the summary: brief, detailed, or conversational", defaultValue = "detailed") String style) {
        return List.of(
            PromptMessage.withAssistantRole(new TextContent(
                "You are a meteorologist providing weather summaries. " +
                "Use the get_current_weather tool to fetch real-time data, then summarize it in a " +
                style + " style. Include indoor and outdoor readings when available. " +
                "Mention any notable conditions (high CO2, extreme temperatures, etc).")),
            PromptMessage.withUserRole(new TextContent(
                "Please provide a " + style + " weather summary for my station."))
        );
    }

    @Prompt(description = "Compare weather data between two time periods")
    public List<PromptMessage> weather_comparison(
            @PromptArg(description = "First period start date (YYYY-MM-DD)") String period1Start,
            @PromptArg(description = "First period end date (YYYY-MM-DD)") String period1End,
            @PromptArg(description = "Second period start date (YYYY-MM-DD)") String period2Start,
            @PromptArg(description = "Second period end date (YYYY-MM-DD)") String period2End) {
        return List.of(
            PromptMessage.withAssistantRole(new TextContent(
                "You are a data analyst specializing in weather patterns. " +
                "Use the get_historical_weather tool to fetch data for both periods, then compare " +
                "temperatures, humidity, and pressure trends. Highlight significant differences.")),
            PromptMessage.withUserRole(new TextContent(
                "Compare the weather between " + period1Start + " to " + period1End +
                " and " + period2Start + " to " + period2End +
                ". What are the key differences in temperature, humidity, and pressure?"))
        );
    }

    @Prompt(description = "Analyze sensor readings for a device and flag any anomalies or concerns")
    public List<PromptMessage> device_diagnostics(
            @PromptArg(description = "Device ID to analyze") String deviceId,
            @PromptArg(description = "Number of days to look back", defaultValue = "7") String daysBack) {
        return List.of(
            PromptMessage.withAssistantRole(new TextContent(
                "You are a home environment specialist. Analyze the sensor readings and flag:\n" +
                "- CO2 levels above 1000 ppm (poor ventilation)\n" +
                "- Humidity below 30% or above 60% (comfort issues)\n" +
                "- Temperature anomalies or rapid changes\n" +
                "- Any sensor data gaps that might indicate device issues\n" +
                "Provide actionable recommendations.")),
            PromptMessage.withUserRole(new TextContent(
                "Analyze the last " + daysBack + " days of data for device " + deviceId +
                " and report any anomalies or concerns."))
        );
    }

    @CompletePrompt("device_diagnostics")
    public List<String> completeDeviceId(String deviceId) {
        var devicesResponse = weatherService.getAvailableDevices();
        if (!devicesResponse.isSuccess() || devicesResponse.getData() == null) {
            return List.of();
        }

        return devicesResponse.getData().stream()
            .map(device -> device.id())
            .filter(id -> id != null && (deviceId == null || deviceId.isEmpty() || id.startsWith(deviceId)))
            .collect(Collectors.toList());
    }
}
