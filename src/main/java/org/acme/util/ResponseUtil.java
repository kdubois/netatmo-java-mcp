package org.acme.util;

import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.acme.dto.ApiResponse;
import org.acme.exception.WeatherApiException;
import org.acme.exception.WeatherApiException.ErrorType;

/**
 * Utility class for standardizing REST API responses
 */
public class ResponseUtil {
    private static final Logger logger = Logger.getLogger(ResponseUtil.class.getName());
    
    /**
     * Creates a successful response with the provided entity
     * 
     * @param entity The entity to include in the response
     * @return A Response object with 200 OK status
     */
    public static Response ok(Object entity) {
        return ApiResponse.success(entity).toResponse();
    }
    
    /**
     * Creates a successful response with the provided entity and message
     * 
     * @param entity The entity to include in the response
     * @param message A message describing the result
     * @return A Response object with 200 OK status
     */
    public static Response ok(Object entity, String message) {
        return ApiResponse.success(entity, message).toResponse();
    }
    
    /**
     * Creates an error response with the provided message
     * 
     * @param message The error message
     * @param status The HTTP status code
     * @param loggerName The name of the logger to use
     * @param context Additional context information for logging
     * @return A Response object with the specified error status
     */
    public static Response error(String message, Response.Status status, String loggerName, String context) {
        Logger contextLogger = Logger.getLogger(loggerName);
        contextLogger.log(Level.SEVERE, context + ": " + message);
        
        // Map status to error type
        ErrorType errorType;
        switch (status) {
            case UNAUTHORIZED:
                errorType = ErrorType.AUTHENTICATION_ERROR;
                break;
            case NOT_FOUND:
                errorType = ErrorType.NOT_FOUND;
                break;
            case BAD_REQUEST:
                errorType = ErrorType.INVALID_PARAMETERS;
                break;
            case BAD_GATEWAY:
                errorType = ErrorType.API_ERROR;
                break;
            default:
                errorType = ErrorType.SERVER_ERROR;
        }
        
        throw new WeatherApiException(message, errorType);
    }
    
    /**
     * Creates a server error response (500)
     * 
     * @param message The error message
     * @param loggerName The name of the logger to use
     * @param context Additional context information for logging
     * @return A Response object with 500 Internal Server Error status
     */
    public static Response serverError(String message, String loggerName, String context) {
        return error(message, Response.Status.INTERNAL_SERVER_ERROR, loggerName, context);
    }
    
    /**
     * Creates a bad request error response (400)
     * 
     * @param message The error message
     * @param loggerName The name of the logger to use
     * @param context Additional context information for logging
     * @return A Response object with 400 Bad Request status
     */
    public static Response badRequest(String message, String loggerName, String context) {
        return error(message, Response.Status.BAD_REQUEST, loggerName, context);
    }
    
    /**
     * Handles a result object that has success/error fields
     * 
     * @param result The result object with success and errorMessage fields
     * @param successEntity The entity to return on success
     * @param loggerName The name of the logger to use
     * @param context Additional context information for logging
     * @return A Response object based on the result status
     */
    public static Response handleResult(ResultWithStatus result, Object successEntity, String loggerName, String context) {
        if (result.isSuccess()) {
            return ok(successEntity);
        } else {
            return serverError(result.getErrorMessage(), loggerName, context);
        }
    }
    
    /**
     * Interface for objects that have success status and error messages
     */
    public interface ResultWithStatus {
        boolean isSuccess();
        String getErrorMessage();
    }
}
