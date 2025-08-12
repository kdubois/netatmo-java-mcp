package org.acme.dto;

import jakarta.ws.rs.core.Response;

/**
 * Generic API response wrapper for consistent response format
 * @param <T> The type of data contained in the response
 */
public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
    public int status;
    
    /**
     * Default constructor
     */
    public ApiResponse() {
    }
    
    /**
     * Constructor with all fields
     * 
     * @param success Whether the request was successful
     * @param message A message describing the result
     * @param data The data returned by the request
     * @param status The HTTP status code
     */
    public ApiResponse(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status;
    }
    
    /**
     * Creates a successful response with the given data
     * 
     * @param <T> The type of data
     * @param data The data to include in the response
     * @return A successful response with the given data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, Response.Status.OK.getStatusCode());
    }
    
    /**
     * Creates a successful response with the given data and message
     * 
     * @param <T> The type of data
     * @param data The data to include in the response
     * @param message A message describing the result
     * @return A successful response with the given data and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Response.Status.OK.getStatusCode());
    }
    
    /**
     * Creates an error response with the given message
     * 
     * @param <T> The type of data
     * @param message The error message
     * @param status The HTTP status code
     * @return An error response with the given message and status
     */
    public static <T> ApiResponse<T> error(String message, Response.Status status) {
        return new ApiResponse<>(false, message, null, status.getStatusCode());
    }
    
    /**
     * Creates an error response with the given message and data
     * 
     * @param <T> The type of data
     * @param message The error message
     * @param data Additional error data
     * @param status The HTTP status code
     * @return An error response with the given message, data, and status
     */
    public static <T> ApiResponse<T> error(String message, T data, Response.Status status) {
        return new ApiResponse<>(false, message, data, status.getStatusCode());
    }
    
    /**
     * Creates a Response object from this ApiResponse
     * 
     * @return A Response object with the appropriate status and entity
     */
    public Response toResponse() {
        return Response.status(this.status).entity(this).build();
    }
}
