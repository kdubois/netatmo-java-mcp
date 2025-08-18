package com.kevindubois.exception;

import jakarta.ws.rs.core.Response;
import com.kevindubois.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherApiExceptionMapperTest {

    @Test
    void testToResponse() {
        // Create a WeatherApiExceptionMapper
        WeatherApiExceptionMapper mapper = new WeatherApiExceptionMapper();
        
        // Create a WeatherApiException with a specific status
        String errorMessage = "Test error message";
        Response.Status status = Response.Status.BAD_REQUEST;
        WeatherApiException exception = new WeatherApiException(errorMessage, status);
        
        // Map the exception to a response
        Response response = mapper.toResponse(exception);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(status.getStatusCode(), response.getStatus());
        
        // Extract the entity and verify it's an ApiResponse
        Object entity = response.getEntity();
        assertTrue(entity instanceof ApiResponse);
        
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) entity;
        assertFalse(apiResponse.isSuccess());
        assertEquals(errorMessage, apiResponse.getMessage());
        assertEquals(status.getStatusCode(), apiResponse.getStatus());
    }
    
    @Test
    void testToResponseWithCause() {
        // Create a WeatherApiExceptionMapper
        WeatherApiExceptionMapper mapper = new WeatherApiExceptionMapper();
        
        // Create a WeatherApiException with a cause
        String errorMessage = "Test error with cause";
        RuntimeException cause = new RuntimeException("Original cause");
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        WeatherApiException exception = new WeatherApiException(errorMessage, cause, status);
        
        // Map the exception to a response
        Response response = mapper.toResponse(exception);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(status.getStatusCode(), response.getStatus());
        
        // Extract the entity and verify it's an ApiResponse
        Object entity = response.getEntity();
        assertTrue(entity instanceof ApiResponse);
        
        @SuppressWarnings("unchecked")
        ApiResponse<Object> apiResponse = (ApiResponse<Object>) entity;
        assertFalse(apiResponse.isSuccess());
        assertEquals(errorMessage, apiResponse.getMessage());
        assertEquals(status.getStatusCode(), apiResponse.getStatus());
    }
}


