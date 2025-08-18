package com.kevindubois.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.kevindubois.dto.ApiResponse;

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
        return ApiResponse.error(exception.getMessage(), exception.getStatus()).toResponse();
    }
}
