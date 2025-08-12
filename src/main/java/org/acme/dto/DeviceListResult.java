package org.acme.dto;

import java.util.List;

/**
 * DTO for device list results
 */
public class DeviceListResult extends BaseResult {
    public List<DeviceInfo> devices;
    
    /**
     * Default constructor
     */
    public DeviceListResult() {
    }
}
