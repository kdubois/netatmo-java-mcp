package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class NetatmoHistoricalDataResponse {
    @JsonProperty("body")
    private Object body;
    
    @JsonProperty("status")
    private String status;

    @JsonProperty("time_exec")
    private Double timeExec;

    @JsonProperty("time_server")
    private Long timeServer;

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTimeExec() {
        return timeExec;
    }

    public void setTimeExec(Double timeExec) {
        this.timeExec = timeExec;
    }

    public Long getTimeServer() {
        return timeServer;
    }

    public void setTimeServer(Long timeServer) {
        this.timeServer = timeServer;
    }

    // Helper method to extract measurement data
    @SuppressWarnings("unchecked")
    public List<List<Object>> getMeasurements() {
        if (body != null && body instanceof List) {
            List<?> bodyList = (List<?>) body;
            
            // Based on the debug info: Body is List with 1 element, First element type: LinkedHashMap
            if (!bodyList.isEmpty() && bodyList.get(0) instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) bodyList.get(0);
                
                // Look for arrays in the LinkedHashMap values
                for (Object value : dataMap.values()) {
                    if (value instanceof List) {
                        return (List<List<Object>>) value;
                    }
                }
                
                // If no nested arrays found, return the map values as a list
                // This might be the case where each key-value pair represents different data
                return null;
            }
        }
        return null;
    }

    // Helper method to get the LinkedHashMap containing the measurement data
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMeasurementData() {
        if (body != null && body instanceof List) {
            List<?> bodyList = (List<?>) body;
            
            // Based on the debug info: Body is List with 1 element, First element type: LinkedHashMap
            if (!bodyList.isEmpty() && bodyList.get(0) instanceof Map) {
                return (Map<String, Object>) bodyList.get(0);
            }
        }
        return null;
    }

    // Helper method to get measurement times and values in a more structured format
    public HistoricalMeasurement getStructuredMeasurements() {
        List<List<Object>> measurements = getMeasurements();
        if (measurements != null && !measurements.isEmpty()) {
            HistoricalMeasurement result = new HistoricalMeasurement();
            
            // First array typically contains timestamps
            if (measurements.size() > 0 && measurements.get(0) instanceof List) {
                List<Object> timestampObjects = measurements.get(0);
                result.timestamps = timestampObjects.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        }
                        return null;
                    })
                    .filter(val -> val != null)
                    .toList();
            }
            
            // Subsequent arrays contain sensor values
            if (measurements.size() > 1) {
                result.values = measurements.subList(1, measurements.size());
            }
            
            return result;
        }
        return null;
    }

    public static class HistoricalMeasurement {
        public List<Long> timestamps;
        public Object values;  // Keep flexible to handle various data structures
    }

    // Class to represent the properly parsed Netatmo historical data
    public static class NetatmoMeasurementData {
        public Integer beginTime;     // beg_time from the response
        public Integer stepTime;      // step_time from the response  
        public List<Object> values;   // value array from the response
        
        public NetatmoMeasurementData(Integer beginTime, Integer stepTime, List<Object> values) {
            this.beginTime = beginTime;
            this.stepTime = stepTime;
            this.values = values;
        }
    }

    // Helper method to get properly parsed measurement data
    @SuppressWarnings("unchecked")
    public NetatmoMeasurementData getParsedMeasurementData() {
        Map<String, Object> measurementData = getMeasurementData();
        if (measurementData != null) {
            Integer beginTime = (Integer) measurementData.get("beg_time");
            Integer stepTime = (Integer) measurementData.get("step_time");
            List<Object> values = (List<Object>) measurementData.get("value");
            
            if (beginTime != null && stepTime != null && values != null) {
                return new NetatmoMeasurementData(beginTime, stepTime, values);
            }
        }
        return null;
    }
}