package org.acme.dto;

import org.acme.util.ResponseUtil.ResultWithStatus;

/**
 * Base class for all API result objects
 * Provides common success/error handling functionality
 */
public class BaseResult implements ResultWithStatus {
    public boolean success;
    public String errorMessage;
    
    /**
     * Default constructor - initializes with success=false
     */
    public BaseResult() {
        this.success = false;
    }
    
    /**
     * Sets this result as successful
     * @return this object for method chaining
     */
    public BaseResult withSuccess() {
        this.success = true;
        this.errorMessage = null;
        return this;
    }
    
    /**
     * Sets this result as failed with the given error message
     * @param message The error message
     * @return this object for method chaining
     */
    public BaseResult withError(String message) {
        this.success = false;
        this.errorMessage = message;
        return this;
    }
    
    /**
     * Sets this result as failed with the given exception
     * @param e The exception that caused the failure
     * @return this object for method chaining
     */
    public BaseResult withError(Exception e) {
        this.success = false;
        this.errorMessage = e.getMessage();
        return this;
    }
    
    @Override
    public boolean isSuccess() {
        return success;
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}

