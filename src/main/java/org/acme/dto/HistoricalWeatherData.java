package org.acme.dto;

import java.util.List;
import org.acme.util.DateTimeUtil;

/**
 * DTO for historical weather data
 */
public class HistoricalWeatherData extends BaseResult {
    public String deviceId;
    public String scale;
    public List<String> sensorTypes;
    public String status;
    public Long beginTimeTimestamp; // Begin timestamp for calculations
    public Long endTimeTimestamp; // End timestamp for calculations
    public Integer stepTime;
    public List<Object> values;
    public Integer totalDataPoints;
    
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
    
    /**
     * Get formatted begin time
     * @return Formatted begin time string in UTC
     */
    public String getBeginTime() {
        if (beginTimeTimestamp == null) {
            return null;
        }
        return DateTimeUtil.formatTimestamp(beginTimeTimestamp, "yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * Get formatted end time
     * @return Formatted end time string in UTC
     */
    public String getEndTime() {
        if (endTimeTimestamp == null) {
            return null;
        }
        return DateTimeUtil.formatTimestamp(endTimeTimestamp, "yyyy-MM-dd HH:mm:ss");
    }
}
