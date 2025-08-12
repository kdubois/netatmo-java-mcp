package org.acme.dto;

/**
 * DTO for current weather data
 */
public class CurrentWeatherData {
    public String stationName;
    public Double indoorTemperature;
    public Integer indoorHumidity;
    public Double pressure;
    public Integer co2;
    public Integer noise;
    public Long timeUtc;
    public Double outdoorTemperature;
    public Integer outdoorHumidity;
    public Double outdoorMaxTemperature;
    public Double outdoorMinTemperature;
    
    /**
     * Default constructor
     */
    public CurrentWeatherData() {
    }
}
