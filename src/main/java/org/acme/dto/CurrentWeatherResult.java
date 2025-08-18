package org.acme.dto;

/**
 * Result class for current weather data
 */
public class CurrentWeatherResult extends BaseResult {
    protected final CurrentWeatherData data;

    public CurrentWeatherResult() {
        this.data = new CurrentWeatherData();
    }
    
    public CurrentWeatherResult(CurrentWeatherData data) {
        this.data = data;
    }
    
    public static CurrentWeatherResult success(CurrentWeatherData data) {
        CurrentWeatherResult result = new CurrentWeatherResult(data);
        result.success = true;
        return result;
    }
    
    public static CurrentWeatherResult error(String errorMessage) {
        CurrentWeatherResult result = new CurrentWeatherResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public CurrentWeatherData getData() {
        return data;
    }
}
