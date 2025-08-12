package org.acme.exception;

/**
 * Custom exception for weather API errors
 */
public class WeatherApiException extends RuntimeException {
    
    private final ErrorType errorType;
    
    /**
     * Creates a new WeatherApiException with the specified message and error type
     * 
     * @param message The error message
     * @param errorType The type of error
     */
    public WeatherApiException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    /**
     * Creates a new WeatherApiException with the specified message, cause, and error type
     * 
     * @param message The error message
     * @param cause The cause of the error
     * @param errorType The type of error
     */
    public WeatherApiException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    /**
     * Gets the error type
     * 
     * @return The error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Enum representing different types of errors
     */
    public enum ErrorType {
        /**
         * Error when authenticating with the Netatmo API
         */
        AUTHENTICATION_ERROR,
        
        /**
         * Error when the requested resource was not found
         */
        NOT_FOUND,
        
        /**
         * Error when the request parameters are invalid
         */
        INVALID_PARAMETERS,
        
        /**
         * Error when the Netatmo API returns an error
         */
        API_ERROR,
        
        /**
         * Error when there is a problem with the server
         */
        SERVER_ERROR
    }
}
