package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Record for current weather data
 */
public record CurrentWeatherData(
    String stationName,
    Double indoorTemperature,
    Integer indoorHumidity,
    Double pressure,
    Integer co2,
    Integer noise,
    Long timeUtc,
    Double outdoorTemperature,
    Integer outdoorHumidity,
    Double outdoorMaxTemperature,
    Double outdoorMinTemperature
) {
    /**
     * Default constructor for empty data
     */
    public CurrentWeatherData() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Constructor with JsonProperty annotations for deserialization
     */
    @JsonCreator
    public CurrentWeatherData(
        @JsonProperty("stationName") String stationName,
        @JsonProperty("indoorTemperature") Double indoorTemperature,
        @JsonProperty("indoorHumidity") Integer indoorHumidity,
        @JsonProperty("pressure") Double pressure,
        @JsonProperty("co2") Integer co2,
        @JsonProperty("noise") Integer noise,
        @JsonProperty("timeUtc") Long timeUtc,
        @JsonProperty("outdoorTemperature") Double outdoorTemperature,
        @JsonProperty("outdoorHumidity") Integer outdoorHumidity,
        @JsonProperty("outdoorMaxTemperature") Double outdoorMaxTemperature,
        @JsonProperty("outdoorMinTemperature") Double outdoorMinTemperature
    ) {
        this.stationName = stationName;
        this.indoorTemperature = indoorTemperature;
        this.indoorHumidity = indoorHumidity;
        this.pressure = pressure;
        this.co2 = co2;
        this.noise = noise;
        this.timeUtc = timeUtc;
        this.outdoorTemperature = outdoorTemperature;
        this.outdoorHumidity = outdoorHumidity;
        this.outdoorMaxTemperature = outdoorMaxTemperature;
        this.outdoorMinTemperature = outdoorMinTemperature;
    }
    
    /**
     * Create a new instance with updated stationName
     */
    public CurrentWeatherData withStationName(String stationName) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated indoorTemperature
     */
    public CurrentWeatherData withIndoorTemperature(Double indoorTemperature) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated indoorHumidity
     */
    public CurrentWeatherData withIndoorHumidity(Integer indoorHumidity) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated pressure
     */
    public CurrentWeatherData withPressure(Double pressure) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated co2
     */
    public CurrentWeatherData withCo2(Integer co2) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated noise
     */
    public CurrentWeatherData withNoise(Integer noise) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated timeUtc
     */
    public CurrentWeatherData withTimeUtc(Long timeUtc) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated outdoorTemperature
     */
    public CurrentWeatherData withOutdoorTemperature(Double outdoorTemperature) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated outdoorHumidity
     */
    public CurrentWeatherData withOutdoorHumidity(Integer outdoorHumidity) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated outdoorMaxTemperature
     */
    public CurrentWeatherData withOutdoorMaxTemperature(Double outdoorMaxTemperature) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
    
    /**
     * Create a new instance with updated outdoorMinTemperature
     */
    public CurrentWeatherData withOutdoorMinTemperature(Double outdoorMinTemperature) {
        return new CurrentWeatherData(stationName, indoorTemperature, indoorHumidity, pressure, co2, noise,
                                     timeUtc, outdoorTemperature, outdoorHumidity, outdoorMaxTemperature, outdoorMinTemperature);
    }
}
