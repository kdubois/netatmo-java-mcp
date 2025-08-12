package org.acme.dto;

import java.util.List;

/**
 * DTO for weather station device information
 */
public class DeviceInfo {
    public String deviceId;
    public String stationName;
    public String type;
    public List<String> dataTypes;

    /**
     * Default constructor
     */
    public DeviceInfo() {
    }

    /**
     * Constructor with all fields
     * 
     * @param deviceId The device ID
     * @param stationName The station name
     * @param type The device type
     * @param dataTypes The data types supported by the device
     */
    public DeviceInfo(String deviceId, String stationName, String type, List<String> dataTypes) {
        this.deviceId = deviceId;
        this.stationName = stationName;
        this.type = type;
        this.dataTypes = dataTypes;
    }
}
