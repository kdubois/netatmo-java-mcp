package org.acme.dto;

import java.util.List;

/**
 * DTO for device list results
 */
public class DeviceListResult extends BaseResult {
    protected final List<DeviceInfo> devices;
    
    /**
     * Get the list of devices
     * @return The list of devices
     */
    public List<DeviceInfo> getDevices() {
        return devices;
    }
    
    /**
     * Default constructor
     */
    public DeviceListResult() {
        this.devices = null;
    }
    
    /**
     * Constructor with devices
     * @param devices The list of devices
     */
    public DeviceListResult(List<DeviceInfo> devices) {
        this.devices = devices;
    }
}
