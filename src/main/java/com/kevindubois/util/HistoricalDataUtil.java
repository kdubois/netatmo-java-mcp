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
        return processDataPoints(parsedData, outdoorDataPoints, beginTimeTimestamp, stepTime, null);
    }

    /**
     * Process and combine data points from historical measurements
     *
     * @param parsedData The parsed measurement data
     * @param outdoorDataPoints List of outdoor data points
     * @param beginTimeTimestamp The beginning timestamp
     * @param stepTime The time step between measurements
     * @param outdoorSensorTypes Ordered list of sensor types requested for the outdoor module (e.g. ["min_temp","max_temp"])
     * @return List of processed data points
     */
    public static List<Object> processDataPoints(
            NetatmoMeasurementData parsedData,
            List<List<Object>> outdoorDataPoints,
            long beginTimeTimestamp,
            int stepTime,
            List<String> outdoorSensorTypes) {

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
                processOutdoorValues(dataPoint, outdoorDataPoints.get(i), outdoorSensorTypes);
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
     * Process outdoor values and add them to the data point.
     * outdoorPoint layout: [timestamp, value0, value1, ...] where value order
     * matches the sensorTypes list that was requested.
     *
     * @param dataPoint The data point to update
     * @param outdoorPoint The outdoor values from the measurement data
     * @param sensorTypes Ordered sensor type names, or null to fall back to positional defaults
     */
    private static void processOutdoorValues(WeatherDataPoint dataPoint, List<Object> outdoorPoint,
                                             List<String> sensorTypes) {
        // outdoorPoint[0] is the timestamp; measured values start at index 1
        for (int idx = 1; idx < outdoorPoint.size(); idx++) {
            Object raw = outdoorPoint.get(idx);
            if (!(raw instanceof Number)) continue;
            double value = ((Number) raw).doubleValue();

            String sensorType = (sensorTypes != null && (idx - 1) < sensorTypes.size())
                    ? sensorTypes.get(idx - 1).trim().toLowerCase()
                    : null;

            if ("min_temp".equals(sensorType)) {
                dataPoint.setOutdoorMinTemperature(value);
            } else if ("max_temp".equals(sensorType)) {
                dataPoint.setOutdoorMaxTemperature(value);
            } else if ("temperature".equals(sensorType)) {
                dataPoint.setOutdoorTemperature(value);
            } else if ("humidity".equals(sensorType) || "min_hum".equals(sensorType) || "max_hum".equals(sensorType)) {
                dataPoint.setOutdoorHumidity(value);
            } else {
                // No sensor type info — fall back to positional defaults
                if (idx == 1) dataPoint.setOutdoorTemperature(value);
                else if (idx == 2) dataPoint.setOutdoorHumidity(value);
            }
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
        private Double outdoorMinTemperature;
        private Double outdoorMaxTemperature;
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

        public Double getOutdoorMinTemperature() {
            return outdoorMinTemperature;
        }

        public Double getOutdoorMaxTemperature() {
            return outdoorMaxTemperature;
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

        public void setOutdoorMinTemperature(Double outdoorMinTemperature) {
            this.outdoorMinTemperature = outdoorMinTemperature;
        }

        public void setOutdoorMaxTemperature(Double outdoorMaxTemperature) {
            this.outdoorMaxTemperature = outdoorMaxTemperature;
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
            if (outdoorMinTemperature != null) map.put("outdoorMinTemperature", outdoorMinTemperature);
            if (outdoorMaxTemperature != null) map.put("outdoorMaxTemperature", outdoorMaxTemperature);
            if (outdoorHumidity != null) map.put("outdoorHumidity", outdoorHumidity);
            return map;
        }
    }
    
    // Removed normalizeParameter method as it's available in WeatherUtil
}


