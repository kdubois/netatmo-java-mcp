package org.acme.mcp;

import io.quarkiverse.mcp.server.TextContent;
import org.acme.exception.WeatherApiException;
import org.acme.util.ResponseUtil.ResultWithStatus;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for handling MCP tool responses
 */
public class McpResponseUtil {
    private static final Logger logger = Logger.getLogger(McpResponseUtil.class.getName());
    
    /**
     * Handles a result object and returns appropriate TextContent
     * 
     * @param result The result object with success/error status
     * @param successFormatter Function to format successful result into text
     * @return TextContent with either success message or error message
     */
    public static TextContent handleResult(ResultWithStatus result, ResultFormatter formatter) {
        if (result.isSuccess()) {
            return new TextContent(formatter.format());
        } else {
            return new TextContent(result.getErrorMessage());
        }
    }
    
    /**
     * Executes a function that might throw exceptions and returns appropriate TextContent
     * 
     * @param operation The operation to execute
     * @return TextContent with either success message or error message
     */
    public static TextContent executeWithExceptionHandling(ExceptionHandlingOperation operation) {
        try {
            return operation.execute();
        } catch (WeatherApiException e) {
            logger.log(Level.SEVERE, "Weather API error: " + e.getMessage(), e);
            return new TextContent("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            return new TextContent("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Functional interface for formatting result objects into text
     */
    @FunctionalInterface
    public interface ResultFormatter {
        String format();
    }
    
    /**
     * Functional interface for operations that might throw exceptions
     */
    @FunctionalInterface
    public interface ExceptionHandlingOperation {
        TextContent execute() throws Exception;
    }
}
