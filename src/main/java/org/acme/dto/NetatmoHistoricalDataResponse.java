package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class NetatmoHistoricalDataResponse {
    @JsonProperty("body")
    private final Object body;
    
    @JsonProperty("status")
    private final String status;

    @JsonProperty("time_exec")
    private final Double timeExec;

    @JsonProperty("time_server")
    private final Long timeServer;
    
    /**
     * Constructor for deserialization by Jackson
     */
    public NetatmoHistoricalDataResponse(
            @JsonProperty("body") Object body,
            @JsonProperty("status") String status,
            @JsonProperty("time_exec") Double timeExec,
            @JsonProperty("time_server") Long timeServer) {
        this.body = body;
        this.status = status;
        this.timeExec = timeExec;
        this.timeServer = timeServer;
    }

    public Object getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public Double getTimeExec() {
        return timeExec;
    }

    public Long getTimeServer() {
        return timeServer;
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


    // Class to represent the properly parsed Netatmo historical data
    public static class NetatmoMeasurementData {
        public Long beginTime;     // beg_time from the response
        public Integer stepTime;      // step_time from the response
        public List<Object> values;   // value array from the response
        
        public NetatmoMeasurementData(Long beginTime, Integer stepTime, List<Object> values) {
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
            // Convert to Long to handle larger timestamps
            Long beginTime = null;
            Object beginTimeObj = measurementData.get("beg_time");
            if (beginTimeObj instanceof Integer) {
                beginTime = ((Integer) beginTimeObj).longValue();
            } else if (beginTimeObj instanceof Long) {
                beginTime = (Long) beginTimeObj;
            }
            
            Integer stepTime = (Integer) measurementData.get("step_time");
            List<Object> values = (List<Object>) measurementData.get("value");
            
            if (beginTime != null && stepTime != null && values != null) {
                return new NetatmoMeasurementData(beginTime, stepTime, values);
            }
        }
        return null;
    }
}