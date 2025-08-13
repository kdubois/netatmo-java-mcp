package org.acme.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtil {
    private static final Logger logger = Logger.getLogger(DateTimeUtil.class.getName());
    
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
}
