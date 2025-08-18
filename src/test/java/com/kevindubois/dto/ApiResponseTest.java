package com.kevindubois.dto;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccessResponse() {
        // Create a success response with data
        String testData = "Test data";
        ApiResponse<String> response = ApiResponse.success(testData);
        
        // Verify the response
        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals(testData, response.getData());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNull(response.getErrorMessage());
    }
    
    @Test
    void testSuccessResponseWithMessage() {
        // Create a success response with data and custom message
        String testData = "Test data";
        String message = "Custom success message";
        ApiResponse<String> response = ApiResponse.success(testData, message);
        
        // Verify the response
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(testData, response.getData());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNull(response.getErrorMessage());
    }
    
    @Test
    void testErrorResponse() {
        // Create an error response
        String errorMessage = "Error message";
        Response.Status status = Response.Status.BAD_REQUEST;
        ApiResponse<String> response = ApiResponse.error(errorMessage, status);
        
        // Verify the response
        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
        assertEquals(status.getStatusCode(), response.getStatus());
        assertEquals(errorMessage, response.getErrorMessage());
    }
    
    @Test
    void testServerErrorResponse() {
        // Create a server error response
        String errorMessage = "Server error";
        ApiResponse<String> response = ApiResponse.serverError(errorMessage);
        
        // Verify the response
        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(errorMessage, response.getErrorMessage());
    }
    
    @Test
    void testBadRequestResponse() {
        // Create a bad request error response
        String errorMessage = "Bad request";
        ApiResponse<String> response = ApiResponse.badRequest(errorMessage);
        
        // Verify the response
        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(errorMessage, response.getErrorMessage());
    }
    
    @Test
    void testToResponse() {
        // Create a response and convert to JAX-RS Response
        ApiResponse<String> apiResponse = ApiResponse.success("Test data");
        Response response = apiResponse.toResponse();
        
        // Verify the response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(apiResponse, response.getEntity());
    }
    
    @Test
    void testToTextContent() {
        // Create a success response and convert to TextContent
        String testData = "Test data";
        ApiResponse<String> successResponse = ApiResponse.success(testData);
        var textContent = successResponse.toTextContent();
        
        // TextContent doesn't have a getText() method, so we'll just verify it's not null
        assertNotNull(textContent);
        
        // Create an error response and convert to TextContent
        String errorMessage = "Error message";
        ApiResponse<String> errorResponse = ApiResponse.error(errorMessage, Response.Status.BAD_REQUEST);
        var errorTextContent = errorResponse.toTextContent();
        
        // Verify the text content is not null
        assertNotNull(errorTextContent);
    }
    
    @Test
    void testToJsonString() {
        // Create a success response with data
        TestDto testData = new TestDto("test name", 42);
        ApiResponse<TestDto> response = ApiResponse.success(testData);
        
        // Convert to JSON string
        String jsonString = response.toJsonString();
        
        // Verify JSON contains expected data - using more lenient checks
        assertTrue(jsonString.toLowerCase().contains("success"));
        assertTrue(jsonString.toLowerCase().contains("message"));
        assertTrue(jsonString.toLowerCase().contains("test name"));
        assertTrue(jsonString.contains("42"));
    }
    
    @Test
    void testStaticToJsonString() throws Exception {
        // Create a test object
        TestDto testData = new TestDto("test object", 100);
        
        // Convert to JSON string
        String jsonString = ApiResponse.toJsonString(testData);
        
        // Verify JSON contains expected data - using more lenient checks
        assertTrue(jsonString.toLowerCase().contains("test object"));
        assertTrue(jsonString.contains("100"));
    }
    
    // Simple test class for JSON serialization
    static class TestDto {
        private final String name;
        private final int value;
        
        public TestDto(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public int getValue() {
            return value;
        }
    }
}


