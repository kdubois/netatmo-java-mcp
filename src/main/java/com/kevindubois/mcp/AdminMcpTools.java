package com.kevindubois.mcp;

import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.McpServer;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.Tool.Annotations;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkiverse.mcp.server.WrapBusinessError;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kevindubois.service.WeatherService;

@Singleton
@McpServer("admin")
@WrapBusinessError
@RolesAllowed("admin")
public class AdminMcpTools {

    @Inject
    WeatherService weatherService;

    @Inject
    ResourceManager resourceManager;

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

        resourceManager.getResource("weather:///current").sendUpdateAndForget();

        return "Successfully refreshed station data. Found " + deviceCount + " device(s).";
    }

    @Tool(name = "get_station_diagnostics",
          description = "Get a brief diagnostic summary of the station: latest reading freshness and a device count/health line.",
          annotations = @Annotations(
              title = "Station Diagnostics",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public String getStationDiagnostics(McpLog log) {
        log.info("Running station diagnostics");

        var currentResponse = weatherService.getCurrentWeather();
        if (!currentResponse.isSuccess()) {
            return "Diagnostics failed: " + currentResponse.getMessage();
        }
        var data = currentResponse.getData();

        var devicesResponse = weatherService.getAvailableDevices();
        int deviceCount = 0;
        if (devicesResponse.isSuccess() && devicesResponse.getData() != null) {
            deviceCount = devicesResponse.getData().size();
        }

        StringBuilder diag = new StringBuilder();
        diag.append("=== Station Diagnostics ===\n");
        diag.append("Devices: ").append(deviceCount).append(" device(s) reporting\n");

        if (data.timeUtc() != null) {
            long ageSeconds = (System.currentTimeMillis() / 1000) - data.timeUtc();
            diag.append("Latest reading (").append(data.stationName()).append("): ")
                .append(data.timeUtc()).append(" UTC, age ").append(ageSeconds).append("s\n");
            if (ageSeconds > 3600) {
                diag.append("WARNING: data is more than 1 hour old\n");
            }
        } else {
            diag.append("No timestamp available for the latest reading\n");
        }

        log.info("Diagnostics complete");
        return diag.toString();
    }

    @Tool(name = "compare_periods",
          description = "Compare outdoor temperatures (daily min/max) between two date periods, with per-day tables and like-for-like deltas.",
          structuredContent = true,
          annotations = @Annotations(
              title = "Compare Periods",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public PeriodComparison comparePeriods(
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "period1Start must be in format YYYY-MM-DD")
            @ToolArg(description = "First period start date (YYYY-MM-DD)") String period1Start,
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "period1End must be in format YYYY-MM-DD")
            @ToolArg(description = "First period end date (YYYY-MM-DD)") String period1End,
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "period2Start must be in format YYYY-MM-DD")
            @ToolArg(description = "Second period start date (YYYY-MM-DD)") String period2Start,
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "period2End must be in format YYYY-MM-DD")
            @ToolArg(description = "Second period end date (YYYY-MM-DD)") String period2End,
            McpLog log) {
        log.info("Comparing periods %s..%s vs %s..%s", period1Start, period1End, period2Start, period2End);

        PeriodComparison.PeriodResult period1 = buildPeriod("Period 1 (" + period1Start + " to " + period1End + ")",
            period1Start, period1End, log);
        PeriodComparison.PeriodResult period2 = buildPeriod("Period 2 (" + period2Start + " to " + period2End + ")",
            period2Start, period2End, log);

        Double deltaAvgMin = nullSafeSubtract(period2.avgMin(), period1.avgMin());
        Double deltaAvgMax = nullSafeSubtract(period2.avgMax(), period1.avgMax());

        String summary = "Period 1 avg daily min/max: " + fmt(period1.avgMin()) + "/" + fmt(period1.avgMax())
            + " C; Period 2 avg daily min/max: " + fmt(period2.avgMin()) + "/" + fmt(period2.avgMax())
            + " C. Delta (P2 - P1): " + fmt(deltaAvgMin) + " C avg min, " + fmt(deltaAvgMax) + " C avg max.";

        return new PeriodComparison(period1, period2,
            new PeriodComparison.Deltas(deltaAvgMin, deltaAvgMax), summary);
    }

    private PeriodComparison.PeriodResult buildPeriod(String label, String begin, String end, McpLog log) {
        var response = weatherService.getHistoricalWeather(null, null, "1day", "min_temp,max_temp", begin, end, null);
        if (!response.isSuccess()) {
            throw new ToolCallException("Failed to fetch period " + begin + ".." + end + ": " + response.getMessage());
        }

        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) response.getData().get("values");

        List<PeriodComparison.PeriodResult.DayReading> days = new ArrayList<>();
        if (values != null) {
            for (Object v : values) {
                if (!(v instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> point = (Map<String, Object>) v;
                Double min = asDouble(point.get("outdoorMinTemperature"));
                Double max = asDouble(point.get("outdoorMaxTemperature"));
                String date = dayString(point.get("timestamp"));
                if (date != null && (min != null || max != null)) {
                    days.add(new PeriodComparison.PeriodResult.DayReading(date, min, max));
                }
            }
        }

        return new PeriodComparison.PeriodResult(label, days,
            average(days, true), average(days, false),
            extreme(days, true, true), extreme(days, false, false));
    }

    private Double average(List<PeriodComparison.PeriodResult.DayReading> days, boolean min) {
        double sum = 0;
        int count = 0;
        for (var day : days) {
            Double v = min ? day.min() : day.max();
            if (v != null) {
                sum += v;
                count++;
            }
        }
        return count > 0 ? round2(sum / count) : null;
    }

    private Double extreme(List<PeriodComparison.PeriodResult.DayReading> days, boolean ofMin, boolean lowest) {
        Double result = null;
        for (var day : days) {
            Double v = ofMin ? day.min() : day.max();
            if (v == null) continue;
            if (result == null || (lowest ? v < result : v > result)) {
                result = v;
            }
        }
        return result != null ? round2(result) : null;
    }

    private Double nullSafeSubtract(Double a, Double b) {
        if (a == null || b == null) return null;
        return round2(a - b);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String fmt(Double v) {
        return v == null ? "n/a" : String.valueOf(v);
    }

    @Tool(name = "run_anomaly_scan",
          description = "Scan recent hourly sensor data for anomalies: high CO2, humidity out of the 30-60% comfort band, "
              + "rapid outdoor temperature jumps, and data gaps. Returns structured findings and recommendations.",
          structuredContent = true,
          annotations = @Annotations(
              title = "Run Anomaly Scan",
              readOnlyHint = true,
              destructiveHint = false,
              idempotentHint = true,
              openWorldHint = true))
    public AnomalyScan runAnomalyScan(
            @ToolArg(description = "Device ID to scan (optional, uses first available device if not provided)", required = false) String deviceId,
            @ToolArg(description = "Number of days to look back (default: 7)", required = false) String daysBack,
            McpLog log) {
        int days = 7;
        if (daysBack != null && !daysBack.isBlank()) {
            try {
                days = Integer.parseInt(daysBack.trim());
            } catch (NumberFormatException e) {
                // fall back to default
            }
        }
        if (days <= 0) {
            days = 7;
        }

        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(days);
        String beginDate = begin.toString();
        String endDate = end.toString();

        log.info("Running anomaly scan for device %s over %s..%s", deviceId, beginDate, endDate);

        var response = weatherService.getHistoricalWeather(deviceId, null, "1hour", "Temperature,CO2,Humidity",
            beginDate, endDate, null);
        if (!response.isSuccess()) {
            throw new ToolCallException("Failed to fetch data for anomaly scan: " + response.getMessage());
        }

        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) response.getData().get("values");
        int dataPoints = values != null ? values.size() : 0;

        List<AnomalyScan.Finding> findings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (values != null) {
            double maxCo2 = Double.NEGATIVE_INFINITY;
            String maxCo2At = null;
            int humidityOutOfRange = 0;
            double humidityMin = Double.POSITIVE_INFINITY;
            double humidityMax = Double.NEGATIVE_INFINITY;
            int humiditySamples = 0;
            Double prevOutdoorTemp = null;
            int tempJumps = 0;

            for (Object v : values) {
                if (!(v instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> point = (Map<String, Object>) v;
                String ts = String.valueOf(point.get("timestamp"));

                Double co2 = asDouble(point.get("indoorCO2"));
                if (co2 == null) co2 = asDouble(point.get("outdoorCO2"));
                if (co2 != null && co2 > maxCo2) {
                    maxCo2 = co2;
                    maxCo2At = ts;
                }

                Double humidity = asDouble(point.get("indoorHumidity"));
                if (humidity == null) humidity = asDouble(point.get("outdoorHumidity"));
                if (humidity != null) {
                    humiditySamples++;
                    humidityMin = Math.min(humidityMin, humidity);
                    humidityMax = Math.max(humidityMax, humidity);
                    if (humidity < 30 || humidity > 60) {
                        humidityOutOfRange++;
                    }
                }

                Double outdoorTemp = asDouble(point.get("outdoorTemperature"));
                if (outdoorTemp != null && prevOutdoorTemp != null) {
                    if (Math.abs(outdoorTemp - prevOutdoorTemp) > 5) {
                        tempJumps++;
                    }
                }
                if (outdoorTemp != null) {
                    prevOutdoorTemp = outdoorTemp;
                }
            }

            if (maxCo2 > 1000) {
                findings.add(new AnomalyScan.Finding("high_co2", "warning",
                    "Peak CO2 of " + (int) maxCo2 + " ppm (above 1000) at " + maxCo2At));
                recommendations.add("Improve ventilation when CO2 exceeds 1000 ppm.");
            }

            if (humiditySamples > 0 && humidityOutOfRange > 0) {
                findings.add(new AnomalyScan.Finding("humidity_out_of_range", "warning",
                    humidityOutOfRange + " of " + humiditySamples + " readings outside 30-60% "
                        + "(range " + round2(humidityMin) + "% to " + round2(humidityMax) + "%)"));
                recommendations.add("Use a humidifier or dehumidifier to bring humidity into the 30-60% band.");
            }

            if (tempJumps > 0) {
                findings.add(new AnomalyScan.Finding("temperature_jump", "info",
                    tempJumps + " consecutive outdoor readings changed by more than 5 C in one hour"));
                recommendations.add("Check the outdoor sensor for calibration or obstruction issues.");
            }

            long expected = (parseDateEpoch(endDate) - parseDateEpoch(beginDate)) / 3600;
            if (expected > 0 && dataPoints < expected - 3) {
                findings.add(new AnomalyScan.Finding("data_gap", "warning",
                    "Expected about " + expected + " hourly points but received " + dataPoints));
                recommendations.add("Investigate missing data; the device may have lost power or connectivity.");
            }
        }

        if (findings.isEmpty()) {
            recommendations.add("No anomalies detected in the scanned window.");
        }

        return new AnomalyScan(deviceId, beginDate, endDate, dataPoints, findings, recommendations);
    }

    private long parseDateEpoch(String date) {
        return LocalDate.parse(date).atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond();
    }

    private String dayString(Object timestamp) {
        if (timestamp == null) return null;
        String s = String.valueOf(timestamp);
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private Double asDouble(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}
