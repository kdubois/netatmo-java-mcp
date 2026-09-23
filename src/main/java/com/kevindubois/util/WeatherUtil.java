package com.kevindubois.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.kevindubois.dto.NetatmoHistoricalDataResponse.NetatmoMeasurementData;

/**
 * Utility class for weather data operations
 */
public class WeatherUtil {
    private static final Logger logger = Logger.getLogger(WeatherUtil.class.getName());
    
    /**
     * Gets the current timestamp
     * 
     * @return The current timestamp in seconds
     */
    public static Long getCurrentTimestamp() {
        return Instant.now().getEpochSecond();
    }
    
    /**
     * Calculates a timestamp in the past based on days back
     * 
     * @param daysBack The number of days to look back
     * @return The timestamp in seconds
     */
    public static Long getTimestampDaysAgo(int daysBack) {
        return Instant.now().minus(daysBack, ChronoUnit.DAYS).getEpochSecond();
    }
    
    /**
     * Format a timestamp using DateTimeFormatter with UTC timezone
     * 
     * @param timestamp The timestamp in seconds
     * @param pattern The date format pattern
     * @return The formatted date string
     */
    public static String formatTimestamp(long timestamp, String pattern) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        return DateTimeFormatter.ofPattern(pattern)
            .withZone(ZoneOffset.UTC) // Always use UTC for consistency
            .format(instant);
    }

    /**
     * Parse a formatted date string back to a timestamp
     * 
     * @param dateStr The date string to parse
     * @param pattern The date format pattern
     * @return The timestamp in seconds
     */
    public static Long parseTimestamp(String dateStr, String pattern) {
        try {
            return java.time.LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern))
                .atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond();
        } catch (Exception e) {
            logger.warning("Failed to parse date: " + dateStr + " with pattern: " + pattern);
            return null;
        }
    }
    
    /**
     * Process and combine data points from historical measurements.
     *
     * @param parsedData The parsed measurement data
     * @param outdoorDataPoints List of outdoor data points ([timestamp, val0, val1, ...] per point)
     * @param beginTimeTimestamp The beginning timestamp in epoch seconds
     * @param stepTime The time step between measurements in seconds
     * @param sensorTypes Ordered sensor type names requested (e.g. ["min_temp","max_temp"] or
     *                    ["Temperature","CO2","Humidity"]), or null to fall back to positional defaults
     * @return List of processed data point maps
     */
    public static List<Object> processDataPoints(
            NetatmoMeasurementData parsedData,
            List<List<Object>> outdoorDataPoints,
            long beginTimeTimestamp,
            int stepTime,
            List<String> sensorTypes) {

        List<Object> result = new ArrayList<>();

        if (parsedData.values != null) {
            for (int i = 0; i < parsedData.values.size(); i++) {
                long timestamp = beginTimeTimestamp + (i * stepTime);
                Object indoorValue = parsedData.values.get(i);

                // Format timestamp as ISO string (yyyy-MM-dd HH:mm) using UTC
                String formattedTimestamp = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm");

                // Create a map for this data point
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("timestamp", formattedTimestamp);

                // Add indoor values
                if (indoorValue instanceof List<?>) {
                    List<?> indoorValues = (List<?>) indoorValue;
                    for (int j = 0; j < indoorValues.size(); j++) {
                        Object raw = indoorValues.get(j);
                        if (raw == null) continue;
                        String sensorType = (sensorTypes != null && j < sensorTypes.size())
                                ? sensorTypes.get(j).trim().toLowerCase()
                                : null;
                        if ("min_temp".equals(sensorType)) {
                            dataPoint.put("indoorMinTemperature", raw);
                        } else if ("max_temp".equals(sensorType)) {
                            dataPoint.put("indoorMaxTemperature", raw);
                        } else if ("min_hum".equals(sensorType)) {
                            dataPoint.put("indoorMinHumidity", raw);
                        } else if ("max_hum".equals(sensorType)) {
                            dataPoint.put("indoorMaxHumidity", raw);
                        } else if ("co2".equals(sensorType)) {
                            dataPoint.put("indoorCO2", raw);
                        } else if ("pressure".equals(sensorType)) {
                            dataPoint.put("indoorPressure", raw);
                        } else if ("noise".equals(sensorType)) {
                            dataPoint.put("indoorNoise", raw);
                        } else if ("humidity".equals(sensorType)) {
                            dataPoint.put("indoorHumidity", raw);
                        } else if ("temperature".equals(sensorType)) {
                            dataPoint.put("indoorTemperature", raw);
                        } else {
                            if (j == 0) dataPoint.put("indoorTemperature", raw);
                            else if (j == 1) dataPoint.put("indoorHumidity", raw);
                            else if (j == 2) dataPoint.put("indoorPressure", raw);
                        }
                    }
                }

                // Add outdoor values if available
                if (outdoorDataPoints != null && i < outdoorDataPoints.size()) {
                    List<Object> outdoorPoint = outdoorDataPoints.get(i);
                    // outdoorPoint[0] is the timestamp; sensor values start at index 1
                    for (int j = 1; j < outdoorPoint.size(); j++) {
                        Object raw = outdoorPoint.get(j);
                        if (raw == null) continue;
                        String sensorType = (sensorTypes != null && (j - 1) < sensorTypes.size())
                                ? sensorTypes.get(j - 1).trim().toLowerCase()
                                : null;

                        if ("min_temp".equals(sensorType)) {
                            dataPoint.put("outdoorMinTemperature", raw);
                        } else if ("max_temp".equals(sensorType)) {
                            dataPoint.put("outdoorMaxTemperature", raw);
                        } else if ("temperature".equals(sensorType)) {
                            dataPoint.put("outdoorTemperature", raw);
                        } else if ("humidity".equals(sensorType) || "min_hum".equals(sensorType) || "max_hum".equals(sensorType)) {
                            dataPoint.put("outdoorHumidity", raw);
                        } else if ("co2".equals(sensorType)) {
                            dataPoint.put("outdoorCO2", raw);
                        } else {
                            // No type info — positional fallback
                            if (j == 1) dataPoint.put("outdoorTemperature", raw);
                            else if (j == 2) dataPoint.put("outdoorHumidity", raw);
                        }
                    }
                }

                result.add(dataPoint);
            }
        }

        return result;
    }

    /**
     * Backwards-compatible overload — delegates to the sensor-type-aware variant with null types.
     */
    public static List<Object> processDataPoints(
            NetatmoMeasurementData parsedData,
            List<List<Object>> outdoorDataPoints,
            long beginTimeTimestamp,
            int stepTime) {
        return processDataPoints(parsedData, outdoorDataPoints, beginTimeTimestamp, stepTime, null);
    }
    
    /**
     * Normalize a parameter with a default value
     */
    public static <T> T normalizeParameter(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String && ((String)value).trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}


