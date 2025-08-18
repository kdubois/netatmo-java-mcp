package com.kevindubois.dto;

import java.util.List;

/**
 * Record for weather station device information
 */
public record DeviceInfo(
    String id,
    String name,
    String type,
    List<String> dataTypes
) {
    /**
     * Default constructor
     */
    public DeviceInfo() {
        this(null, null, null, null);
    }
}
