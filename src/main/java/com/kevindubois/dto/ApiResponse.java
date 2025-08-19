package com.kevindubois.dto;

import jakarta.ws.rs.core.Response;
import io.quarkiverse.mcp.server.TextContent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Generic API response wrapper for consistent response format
 * @param <T> The type of data contained in the response
 */
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @JsonProperty("success")
    private final boolean success;
    
    @JsonProperty("message")
    private final String message;
    
    @JsonProperty("data")
    private final T data;
    
    @JsonProperty("status")
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
    @JsonCreator
    public ApiResponse(
            @JsonProperty("success") boolean success, 
            @JsonProperty("message") String message, 
            @JsonProperty("data") T data, 
            @JsonProperty("status") int status) {
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
    @JsonProperty("success")
    public boolean isSuccess() { return success; }
    
    @JsonProperty("message")
    public String getMessage() { return message; }
    
    @JsonProperty("data")
    public T getData() { return data; }
    
    @JsonProperty("status")
    public int getStatus() { return status; }
    
    public String getErrorMessage() { return !success ? message : null; }
}
