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
     * Process and combine data points from historical measurements
     */
    public static List<Object> processDataPoints(
            NetatmoMeasurementData parsedData,
            List<List<Object>> outdoorDataPoints,
            long beginTimeTimestamp,
            int stepTime) {
        
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
                    if (indoorValues.size() >= 1) {
                        dataPoint.put("indoorTemperature", indoorValues.get(0));
                    }
                    if (indoorValues.size() >= 2) {
                        dataPoint.put("indoorHumidity", indoorValues.get(1));
                    }
                    if (indoorValues.size() >= 3) {
                        dataPoint.put("indoorPressure", indoorValues.get(2));
                    }
                }
                
                // Add outdoor values if available
                if (outdoorDataPoints != null && i < outdoorDataPoints.size()) {
                    List<Object> outdoorPoint = outdoorDataPoints.get(i);
                    if (outdoorPoint.size() >= 2) { // First element is timestamp
                        dataPoint.put("outdoorTemperature", outdoorPoint.get(1));
                    }
                    if (outdoorPoint.size() >= 3) {
                        dataPoint.put("outdoorHumidity", outdoorPoint.get(2));
                    }
                }
                
                result.add(dataPoint);
            }
        }
        
        return result;
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


