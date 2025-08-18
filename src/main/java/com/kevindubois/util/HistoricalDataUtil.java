package com.kevindubois.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.kevindubois.dto.NetatmoHistoricalDataResponse.NetatmoMeasurementData;

/**
 * Utility class for processing historical weather data
 */
public class HistoricalDataUtil {
    
    private static final Logger logger = Logger.getLogger(HistoricalDataUtil.class.getName());
    
    /**
     * Process and combine data points from historical measurements
     * 
     * @param parsedData The parsed measurement data
     * @param outdoorDataPoints List of outdoor data points
     * @param beginTimeTimestamp The beginning timestamp
     * @param stepTime The time step between measurements
     * @return List of processed data points
     */
    public static List<Object> processDataPoints(
            NetatmoMeasurementData parsedData,
            List<List<Object>> outdoorDataPoints,
            long beginTimeTimestamp,
            int stepTime) {
        
        List<Object> result = new ArrayList<>();
        
        if (parsedData == null || parsedData.values == null) {
            return result;
        }
        
        for (int i = 0; i < parsedData.values.size(); i++) {
            // Calculate timestamp for this data point
            long timestamp = beginTimeTimestamp + (i * stepTime);
            String formattedTimestamp = WeatherUtil.formatTimestamp(timestamp, "yyyy-MM-dd HH:mm");
            
            // Create a new data point
            WeatherDataPoint dataPoint = new WeatherDataPoint(formattedTimestamp);
            
            // Process indoor values
            Object indoorValue = parsedData.values.get(i);
            processIndoorValues(dataPoint, indoorValue);
            
            // Process outdoor values if available
            if (outdoorDataPoints != null && i < outdoorDataPoints.size()) {
                processOutdoorValues(dataPoint, outdoorDataPoints.get(i));
            }
            
            // Convert to Map for backward compatibility and add to result
            result.add(dataPoint.toMap());
        }
        
        return result;
    }
    
    /**
     * Process indoor values and add them to the data point
     * 
     * @param dataPoint The data point to update
     * @param indoorValue The indoor values from the measurement data
     */
    private static void processIndoorValues(WeatherDataPoint dataPoint, Object indoorValue) {
        if (indoorValue instanceof List<?>) {
            List<?> indoorValues = (List<?>) indoorValue;
            
            if (indoorValues.size() >= 1 && indoorValues.get(0) instanceof Number) {
                dataPoint.setIndoorTemperature(((Number) indoorValues.get(0)).doubleValue());
            }
            
            if (indoorValues.size() >= 2 && indoorValues.get(1) instanceof Number) {
                dataPoint.setIndoorHumidity(((Number) indoorValues.get(1)).doubleValue());
            }
            
            if (indoorValues.size() >= 3 && indoorValues.get(2) instanceof Number) {
                dataPoint.setIndoorPressure(((Number) indoorValues.get(2)).doubleValue());
            }
        }
    }

    /**
     * Process outdoor values and add them to the data point
     * 
     * @param dataPoint The data point to update
     * @param outdoorPoint The outdoor values from the measurement data
     */
    private static void processOutdoorValues(WeatherDataPoint dataPoint, List<Object> outdoorPoint) {
        if (outdoorPoint.size() >= 2 && outdoorPoint.get(1) instanceof Number) {
            dataPoint.setOutdoorTemperature(((Number) outdoorPoint.get(1)).doubleValue());
        }
        
        if (outdoorPoint.size() >= 3 && outdoorPoint.get(2) instanceof Number) {
            dataPoint.setOutdoorHumidity(((Number) outdoorPoint.get(2)).doubleValue());
        }
    }
    
    /**
     * Represents a single weather data point with timestamp and measurements
     */
    public static class WeatherDataPoint {
        private final String timestamp;
        private Double indoorTemperature;
        private Double indoorHumidity;
        private Double indoorPressure;
        private Double outdoorTemperature;
        private Double outdoorHumidity;

        public WeatherDataPoint(String timestamp) {
            this.timestamp = timestamp;
        }

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public Double getIndoorTemperature() {
            return indoorTemperature;
        }

        public Double getIndoorHumidity() {
            return indoorHumidity;
        }

        public Double getIndoorPressure() {
            return indoorPressure;
        }

        public Double getOutdoorTemperature() {
            return outdoorTemperature;
        }

        public Double getOutdoorHumidity() {
            return outdoorHumidity;
        }

        // Setters
        public void setIndoorTemperature(Double indoorTemperature) {
            this.indoorTemperature = indoorTemperature;
        }

        public void setIndoorHumidity(Double indoorHumidity) {
            this.indoorHumidity = indoorHumidity;
        }

        public void setIndoorPressure(Double indoorPressure) {
            this.indoorPressure = indoorPressure;
        }

        public void setOutdoorTemperature(Double outdoorTemperature) {
            this.outdoorTemperature = outdoorTemperature;
        }

        public void setOutdoorHumidity(Double outdoorHumidity) {
            this.outdoorHumidity = outdoorHumidity;
        }

        /**
         * Convert to Map representation for backward compatibility
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("timestamp", timestamp);
            if (indoorTemperature != null) map.put("indoorTemperature", indoorTemperature);
            if (indoorHumidity != null) map.put("indoorHumidity", indoorHumidity);
            if (indoorPressure != null) map.put("indoorPressure", indoorPressure);
            if (outdoorTemperature != null) map.put("outdoorTemperature", outdoorTemperature);
            if (outdoorHumidity != null) map.put("outdoorHumidity", outdoorHumidity);
            return map;
        }
    }
    
    // Removed normalizeParameter method as it's available in WeatherUtil
}


