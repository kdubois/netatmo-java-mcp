package com.kevindubois.dto;

import jakarta.ws.rs.core.Response;
import io.quarkiverse.mcp.server.TextContent;

/**
 * Generic API response wrapper for consistent response format
 * @param <T> The type of data contained in the response
 */
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final int status;

    /**
     * Default constructor
     */
    public ApiResponse() {
        this(false, null, null, 200);
    }
    
    /**
     * Constructor with all fields
     */
    public ApiResponse(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.status = status;
    }
    
    /**
     * Creates a successful response with the given data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, Response.Status.OK.getStatusCode());
    }
    
    /**
     * Creates a successful response with the given data and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Response.Status.OK.getStatusCode());
    }
    
    /**
     * Creates an error response with the given message
     */
    public static <T> ApiResponse<T> error(String message, Response.Status status) {
        return new ApiResponse<>(false, message, null, status.getStatusCode());
    }
    
    /**
     * Creates a server error response (500)
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(message, Response.Status.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Creates a bad request error response (400)
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(message, Response.Status.BAD_REQUEST);
    }
    
    /**
     * Creates a Response object from this ApiResponse
     */
    public Response toResponse() {
        return Response.status(this.status).entity(this).build();
    }
    
    /**
     * Creates a TextContent object for MCP tools
     */
    public TextContent toTextContent() {
        if (this.success) {
            if (this.data != null) {
                try {
                    // Convert data to JSON string
                    return new TextContent(toJsonString(this.data));
                } catch (Exception e) {
                    // Fallback to toString if JSON conversion fails
                    return new TextContent(this.data.toString());
                }
            } else {
                return new TextContent(this.message);
            }
        } else {
            return new TextContent("Error: " + this.message);
        }
    }
    
    /**
     * Converts the response to a JSON string
     */
    public String toJsonString() {
        try {
            return toJsonString(this);
        } catch (Exception e) {
            return "{\"success\":" + this.success +
                   ",\"message\":\"" + (this.message != null ? this.message.replace("\"", "\\\"") : "") + "\"}";
        }
    }
    
    /**
     * Converts any object to a JSON string
     */
    public static String toJsonString(Object obj) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper()
            .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(obj);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public int getStatus() { return status; }
    public String getErrorMessage() { return !success ? message : null; }
}
