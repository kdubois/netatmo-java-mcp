package com.kevindubois.dto;

/**
 * Simple base class for service layer results
 */
public class BaseResult {
    protected boolean success;
    protected String errorMessage;

    public BaseResult() {
        this.success = false;
    }
    
    // For backward compatibility
    public BaseResult withSuccess() {
        this.success = true;
        this.errorMessage = null;
        return this;
    }
    
    public BaseResult withError(String message) {
        this.success = false;
        this.errorMessage = message;
        return this;
    }
    
    public BaseResult withError(Exception e) {
        this.success = false;
        this.errorMessage = e.getMessage();
        return this;
    }
    
    // Static factory methods
    public static BaseResult success() {
        BaseResult result = new BaseResult();
        result.success = true;
        return result;
    }
    
    public static BaseResult error(String message) {
        BaseResult result = new BaseResult();
        result.success = false;
        result.errorMessage = message;
        return result;
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}


