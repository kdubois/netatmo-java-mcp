package org.acme.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * Utility class for handling historical data parameters and calculations
 */
public class HistoricalDataUtil {
    private static final Logger logger = Logger.getLogger(HistoricalDataUtil.class.getName());
    
    // Default values
    public static final String DEFAULT_SCALE = "1hour";
    public static final String DEFAULT_SENSOR_TYPES = "Temperature,Humidity,Pressure";
    public static final int DEFAULT_DAYS_BACK = 7;
    public static final int DEFAULT_LIMIT = 1024;
    
    /**
     * Calculates the days back from begin and end timestamps
     * 
     * @param dateBegin The begin timestamp in seconds
     * @param dateEnd The end timestamp in seconds
     * @return The number of days between the timestamps
     */
    public static Integer calculateDaysBack(Long dateBegin, Long dateEnd) {
        if (dateBegin != null && dateEnd != null) {
            // Convert seconds to days
            return (int)((dateEnd - dateBegin) / (24 * 60 * 60));
        }
        return DEFAULT_DAYS_BACK;
    }
    
    /**
     * Calculates the begin timestamp based on days back
     * 
     * @param daysBack The number of days to look back
     * @return The begin timestamp in seconds
     */
    public static Long calculateBeginTimestamp(Integer daysBack) {
        if (daysBack == null) {
            daysBack = DEFAULT_DAYS_BACK;
        }
        return Instant.now().minus(daysBack, ChronoUnit.DAYS).getEpochSecond();
    }
    
    /**
     * Gets the current timestamp
     * 
     * @return The current timestamp in seconds
     */
    public static Long getCurrentTimestamp() {
        return Instant.now().getEpochSecond();
    }
    
    /**
     * Normalizes the scale parameter
     * 
     * @param scale The scale parameter
     * @return The normalized scale parameter
     */
    public static String normalizeScale(String scale) {
        if (scale == null || scale.trim().isEmpty()) {
            return DEFAULT_SCALE;
        }
        return scale;
    }
    
    /**
     * Normalizes the sensor types parameter
     * 
     * @param sensorTypes The sensor types parameter
     * @return The normalized sensor types parameter
     */
    public static String normalizeSensorTypes(String sensorTypes) {
        if (sensorTypes == null || sensorTypes.trim().isEmpty()) {
            return DEFAULT_SENSOR_TYPES;
        }
        return sensorTypes;
    }
    
    /**
     * Normalizes the limit parameter
     * 
     * @param limit The limit parameter
     * @return The normalized limit parameter
     */
    public static Integer normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return limit;
    }
    
    /**
     * Logs the historical data parameters
     * 
     * @param deviceId The device ID
     * @param scale The scale parameter
     * @param sensorTypes The sensor types parameter
     * @param dateBegin The begin timestamp
     * @param dateEnd The end timestamp
     * @param limit The limit parameter
     */
    public static void logHistoricalDataParameters(String deviceId, String scale, String sensorTypes, 
                                                 Long dateBegin, Long dateEnd, Integer limit) {
        logger.info("Requesting historical data with parameters: device_id=" + deviceId + 
                   ", scale=" + scale + ", type=" + sensorTypes + 
                   ", date_begin=" + dateBegin + ", date_end=" + dateEnd + ", limit=" + limit);
    }
}
