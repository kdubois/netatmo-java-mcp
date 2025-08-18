package com.kevindubois.exception;

import jakarta.ws.rs.core.Response;

/**
 * Custom exception for weather API errors
 */
public class WeatherApiException extends RuntimeException {
    
    private final Response.Status status;
    
    /**
     * Creates a new WeatherApiException with the specified message and status
     *
     * @param message The error message
     * @param status The HTTP status code
     */
    public WeatherApiException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }
    
    /**
     * Creates a new WeatherApiException with the specified message, cause, and status
     *
     * @param message The error message
     * @param cause The cause of the error
     * @param status The HTTP status code
     */
    public WeatherApiException(String message, Throwable cause, Response.Status status) {
        super(message, cause);
        this.status = status;
    }
    
    /**
     * Gets the HTTP status code
     *
     * @return The HTTP status code
     */
    public Response.Status getStatus() {
        return status;
    }
}
