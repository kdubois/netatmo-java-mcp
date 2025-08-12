package org.acme.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.acme.dto.ApiResponse;
import org.acme.exception.WeatherApiException.ErrorType;

import java.util.logging.Logger;

/**
 * Exception mapper for WeatherApiException
 * Maps exceptions to appropriate HTTP responses
 */
@Provider
public class WeatherApiExceptionMapper implements ExceptionMapper<WeatherApiException> {
    
    private static final Logger logger = Logger.getLogger(WeatherApiExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(WeatherApiException exception) {
        logger.severe("Weather API error: " + exception.getMessage());
        
        Response.Status status = mapErrorTypeToStatus(exception.getErrorType());
        
        return ApiResponse.error(exception.getMessage(), status).toResponse();
    }
    
    /**
     * Maps error types to HTTP status codes
     * 
     * @param errorType The error type
     * @return The corresponding HTTP status
     */
    private Response.Status mapErrorTypeToStatus(ErrorType errorType) {
        switch (errorType) {
            case AUTHENTICATION_ERROR:
                return Response.Status.UNAUTHORIZED;
            case NOT_FOUND:
                return Response.Status.NOT_FOUND;
            case INVALID_PARAMETERS:
                return Response.Status.BAD_REQUEST;
            case API_ERROR:
                return Response.Status.BAD_GATEWAY;
            case SERVER_ERROR:
            default:
                return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
}
