package org.acme.dto;

import java.util.List;

/**
 * DTO for historical weather data
 */
public class HistoricalWeatherData extends BaseResult {
    public String deviceId;
    public String scale;
    public List<String> sensorTypes;
    public String status;
    public String beginTime;
    public Integer beginTimeTimestamp; // Original timestamp for calculations
    public Integer stepTime;
    public List<Object> values;
    public Integer totalDataPoints;
    public Integer daysBack;
    
    // Outdoor module data
    public String outdoorModuleId;
    public String outdoorModuleName;
    public Double outdoorTemperature;
    public Integer outdoorHumidity;
    
    /**
     * Default constructor
     */
    public HistoricalWeatherData() {
    }
}
