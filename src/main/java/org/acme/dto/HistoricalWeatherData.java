package org.acme.dto;

import java.util.List;
import org.acme.util.WeatherUtil;

/**
 * DTO for historical weather data
 */
public class HistoricalWeatherData extends BaseResult {
    protected final String deviceId;
    protected final String scale;
    protected final List<String> sensorTypes;
    protected final String status;
    protected final Long beginTimeTimestamp;
    protected final Long endTimeTimestamp;
    protected final Integer stepTime;
    protected final List<Object> values;
    protected final Integer totalDataPoints;

    // Outdoor module data
    protected final String outdoorModuleId;
    protected final String outdoorModuleName;
    protected final Double outdoorTemperature;
    protected final Integer outdoorHumidity;

    public HistoricalWeatherData() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    public HistoricalWeatherData(
            String deviceId,
            String scale,
            List<String> sensorTypes,
            String status,
            Long beginTimeTimestamp,
            Long endTimeTimestamp,
            Integer stepTime,
            List<Object> values,
            Integer totalDataPoints,
            String outdoorModuleId,
            String outdoorModuleName,
            Double outdoorTemperature,
            Integer outdoorHumidity) {
        this.deviceId = deviceId;
        this.scale = scale;
        this.sensorTypes = sensorTypes;
        this.status = status;
        this.beginTimeTimestamp = beginTimeTimestamp;
        this.endTimeTimestamp = endTimeTimestamp;
        this.stepTime = stepTime;
        this.values = values;
        this.totalDataPoints = totalDataPoints;
        this.outdoorModuleId = outdoorModuleId;
        this.outdoorModuleName = outdoorModuleName;
        this.outdoorTemperature = outdoorTemperature;
        this.outdoorHumidity = outdoorHumidity;
    }
    
    public String getDeviceId(){return deviceId;}
    public String getScale(){return scale;}
    public List<String> getSensorTypes(){return sensorTypes;}
    public String getStatus(){return status;}
    public Long getBeginTimeTimestamp(){return beginTimeTimestamp;}
    public Long getEndTimeTimestamp(){return endTimeTimestamp;}
    public Integer getStepTime(){return stepTime;}
    public List<Object> getValues(){return values;}
    public Integer getTotalDataPoints(){return totalDataPoints;}
    public String getOutdoorModuleId(){return outdoorModuleId;}
    public String getOutdoorModuleName(){return outdoorModuleName;}
    public Double getOutdoorTemperature(){return outdoorTemperature;}
    public Integer getOutdoorHumidity(){return outdoorHumidity;}

    /**
     * Get formatted begin time
     * @return Formatted begin time string in UTC
     */
    public String getBeginTime() {
        if (beginTimeTimestamp == null) {
            return null;
        }
        return WeatherUtil.formatTimestamp(beginTimeTimestamp, "yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * Get formatted end time
     * @return Formatted end time string in UTC
     */
    public String getEndTime() {
        if (endTimeTimestamp == null) {
            return null;
        }
        return WeatherUtil.formatTimestamp(endTimeTimestamp, "yyyy-MM-dd HH:mm:ss");
    }
}


