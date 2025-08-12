package org.acme.dto;

/**
 * Result class for current weather data
 */
public class CurrentWeatherResult extends BaseResult {
    public CurrentWeatherData data;
    
    /**
     * Default constructor
     */
    public CurrentWeatherResult() {
        this.data = new CurrentWeatherData();
    }
    
    /**
     * Creates a successful result with the given data
     *
     * @param data The current weather data
     * @return A successful result with the given data
     */
    public static CurrentWeatherResult success(CurrentWeatherData data) {
        CurrentWeatherResult result = new CurrentWeatherResult();
        result.data = data;
        result.success = true;
        return result;
    }
    
    /**
     * Creates a failed result with the given error message
     *
     * @param errorMessage The error message
     * @return A failed result with the given error message
     */
    public static CurrentWeatherResult error(String errorMessage) {
        CurrentWeatherResult result = new CurrentWeatherResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }
}
