package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.NetatmoApiClient;
import org.acme.dto.*;
import org.acme.exception.WeatherApiException;
import org.acme.exception.WeatherApiException.ErrorType;
import org.acme.util.HistoricalDataUtil;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class WeatherService {

    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    @Inject
    @RestClient
    NetatmoApiClient netatmoApiClient;

    /**
     * Fetch all weather stations data
     * @return The raw Netatmo stations data response
     * @throws WeatherApiException if there's an error fetching the data
     */
    public NetatmoStationsDataResponse fetchAllStations() {
        try {
            logger.info("Fetching all weather station data");
            return netatmoApiClient.getStationsData();
        } catch (Exception e) {
            logger.severe("Error fetching all stations: " + e.getMessage());
            throw new WeatherApiException("Error fetching all stations: " + e.getMessage(),
                                         e, ErrorType.API_ERROR);
        }
    }

    /**
     * Fetch data for a specific weather station
     * @param deviceId The device ID of the station to fetch
     * @return The raw Netatmo stations data response
     * @throws WeatherApiException if there's an error fetching the data
     */
    public NetatmoStationsDataResponse fetchStation(String deviceId) {
        try {
            logger.info("Fetching weather station data for device: " + deviceId);
            return netatmoApiClient.getStationsData(deviceId);
        } catch (Exception e) {
            logger.severe("Error fetching station " + deviceId + ": " + e.getMessage());
            throw new WeatherApiException("Error fetching station " + deviceId + ": " + e.getMessage(),
                                         e, ErrorType.API_ERROR);
        }
    }

    /**
     * Get current weather data from the first available station
     * @return Processed current weather data
     */
    public CurrentWeatherResult getCurrentWeather() {
        try {
            NetatmoStationsDataResponse response = netatmoApiClient.getStationsData();
            
            if (response.getBody() != null &&
                response.getBody().getDevices() != null &&
                !response.getBody().getDevices().isEmpty()) {
                
                var device = response.getBody().getDevices().get(0);
                
                if (device.getDashboardData() != null) {
                    var dashboardData = device.getDashboardData();
                    var data = new CurrentWeatherData();
                    
                    data.stationName = device.getStationName();
                    data.indoorTemperature = dashboardData.getTemperature();
                    data.indoorHumidity = dashboardData.getHumidity();
                    data.pressure = dashboardData.getPressure();
                    data.co2 = dashboardData.getCo2();
                    data.noise = dashboardData.getNoise();
                    data.timeUtc = dashboardData.getTimeUtc();
                    
                    if (device.getModules() != null && !device.getModules().isEmpty()) {
                        var outdoorModule = device.getModules().get(0);
                        if (outdoorModule.getDashboardData() != null) {
                            var outdoorData = outdoorModule.getDashboardData();
                            data.outdoorTemperature = outdoorData.getTemperature();
                            data.outdoorHumidity = outdoorData.getHumidity();
                            data.outdoorMaxTemperature = outdoorData.getMaxTemp();
                            data.outdoorMinTemperature = outdoorData.getMinTemp();
                        }
                    }
                    
                    return CurrentWeatherResult.success(data);
                }
            }
            
            return CurrentWeatherResult.error("No weather station data available");
        } catch (Exception e) {
            logger.severe("Error getting current weather: " + e.getMessage());
            return CurrentWeatherResult.error("Error retrieving current weather data: " + e.getMessage());
        }
    }

    /**
     * Get a list of available weather station devices
     * @return List of device information
     */
    public DeviceListResult getAvailableDevices() {
        try {
            NetatmoStationsDataResponse response = netatmoApiClient.getStationsData();
            
            if (response.getBody() != null &&
                response.getBody().getDevices() != null &&
                !response.getBody().getDevices().isEmpty()) {
                
                var result = new DeviceListResult();
                result.devices = response.getBody().getDevices().stream()
                    .map(device -> new DeviceInfo(
                        device.getId(),
                        device.getStationName(),
                        device.getType(),
                        device.getDataType()
                    ))
                    .collect(Collectors.toList());
                
                return (DeviceListResult) result.withSuccess();
            }
            
            return (DeviceListResult) new DeviceListResult()
                .withError("No weather stations found");
        } catch (Exception e) {
            logger.severe("Error getting available devices: " + e.getMessage());
            return (DeviceListResult) new DeviceListResult()
                .withError("Error retrieving device list: " + e.getMessage());
        }
    }

    /**
     * Get historical weather data
     * @param deviceId The device ID (optional)
     * @param moduleId The module ID (optional)
     * @param scale The time scale (e.g., "1hour", "1day")
     * @param sensorTypes The sensor types to retrieve (comma-separated)
     * @param daysBack Number of days to look back
     * @param limit Maximum number of data points to retrieve
     * @return Historical weather data result
     */
    public HistoricalWeatherData getHistoricalWeather(String deviceId, String moduleId, String scale,
                                                      String sensorTypes, Integer daysBack, Integer limit) {
        try {
            // Normalize parameters using the utility class
            scale = HistoricalDataUtil.normalizeScale(scale);
            sensorTypes = HistoricalDataUtil.normalizeSensorTypes(sensorTypes);
            limit = HistoricalDataUtil.normalizeLimit(limit);
            
            if (daysBack == null) {
                daysBack = HistoricalDataUtil.DEFAULT_DAYS_BACK;
            }

            // If no device_id provided, get first available device
            if (deviceId == null || deviceId.trim().isEmpty()) {
                var devicesResult = getAvailableDevices();
                if (devicesResult.success && !devicesResult.devices.isEmpty()) {
                    deviceId = devicesResult.devices.get(0).deviceId;
                    logger.info("Using device_id: " + deviceId);
                } else {
                    throw new WeatherApiException("No weather stations found. Please provide a valid device_id.",
                                                ErrorType.NOT_FOUND);
                }
            }

            // Calculate date range
            Long dateEnd = HistoricalDataUtil.getCurrentTimestamp();
            Long dateBegin = HistoricalDataUtil.calculateBeginTimestamp(daysBack);

            // Log parameters
            HistoricalDataUtil.logHistoricalDataParameters(deviceId, scale, sensorTypes, dateBegin, dateEnd, limit);

            // Get indoor data
            NetatmoHistoricalDataResponse response = netatmoApiClient.getHistoricalData(
                deviceId, moduleId, scale, sensorTypes, dateBegin, dateEnd, limit, true, true
            );

            var result = new HistoricalWeatherData();
            result.deviceId = deviceId;
            result.scale = scale;
            result.sensorTypes = List.of(sensorTypes.split(","));
            result.status = response.getStatus();
            result.daysBack = daysBack;

            var parsedData = response.getParsedMeasurementData();
            if (parsedData != null) {
                // Store original timestamp for calculations
                result.beginTimeTimestamp = parsedData.beginTime;
                
                // Format beginTime as YYYY-MM-dd HH:mm:ss
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                result.beginTime = sdf.format(new Date(parsedData.beginTime * 1000L));
                result.stepTime = parsedData.stepTime;

                // Initialize values list for the structured format
                result.values = new java.util.ArrayList<>();
                
                // Get outdoor module data first so we can combine the data points
                String outdoorModuleId = null;
                java.util.List<java.util.List<Object>> outdoorDataPoints = null;
                
                try {
                    // Fetch the station data to get the outdoor module ID
                    NetatmoStationsDataResponse stationResponse = netatmoApiClient.getStationsData(deviceId);
                    
                    if (stationResponse.getBody() != null &&
                        stationResponse.getBody().getDevices() != null &&
                        !stationResponse.getBody().getDevices().isEmpty()) {
                        
                        var device = stationResponse.getBody().getDevices().get(0);
                        
                        if (device.getModules() != null && !device.getModules().isEmpty()) {
                            // Get the first outdoor module
                            var outdoorModule = device.getModules().get(0);
                            outdoorModuleId = outdoorModule.getId();
                            result.outdoorModuleId = outdoorModuleId;
                            result.outdoorModuleName = outdoorModule.getModuleName();
                            
                            // Get current outdoor data
                            if (outdoorModule.getDashboardData() != null) {
                                result.outdoorTemperature = outdoorModule.getDashboardData().getTemperature();
                                result.outdoorHumidity = outdoorModule.getDashboardData().getHumidity();
                            }
                            
                            // Get historical data for the outdoor module
                            NetatmoHistoricalDataResponse outdoorResponse = netatmoApiClient.getHistoricalData(
                                deviceId, outdoorModuleId, scale, sensorTypes, dateBegin, dateEnd, limit, true, true
                            );
                            
                            var outdoorParsedData = outdoorResponse.getParsedMeasurementData();
                            if (outdoorParsedData != null && outdoorParsedData.values != null) {
                                outdoorDataPoints = new java.util.ArrayList<>();
                                
                                // Process outdoor data points
                                for (int i = 0; i < outdoorParsedData.values.size(); i++) {
                                    long timestamp = outdoorParsedData.beginTime + (i * outdoorParsedData.stepTime);
                                    Object value = outdoorParsedData.values.get(i);
                                    
                                    java.util.List<Object> outdoorPoint = new java.util.ArrayList<>();
                                    outdoorPoint.add(timestamp);
                                    
                                    if (value instanceof java.util.List) {
                                        outdoorPoint.addAll((java.util.List<Object>) value);
                                    } else {
                                        outdoorPoint.add(value);
                                    }
                                    
                                    outdoorDataPoints.add(outdoorPoint);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Error fetching outdoor module data: " + e.getMessage());
                    // Continue without outdoor data
                }
                
                // Process indoor data and combine with outdoor data
                if (parsedData.values != null) {
                    for (int i = 0; i < parsedData.values.size(); i++) {
                        long timestamp = result.beginTimeTimestamp + (i * result.stepTime);
                        Object indoorValue = parsedData.values.get(i);
                        
                        // Format timestamp as ISO string (yyyy-MM-dd HH:mm)
                        java.text.SimpleDateFormat dataPointSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String formattedTimestamp = dataPointSdf.format(new java.util.Date(timestamp * 1000L));
                        
                        // Create a map for this data point
                        java.util.Map<String, Object> dataPoint = new java.util.HashMap<>();
                        dataPoint.put("timestamp", formattedTimestamp);
                        
                        // Add indoor values
                        if (indoorValue instanceof java.util.List) {
                            java.util.List<Object> indoorValues = (java.util.List<Object>) indoorValue;
                            if (indoorValues.size() >= 1) {
                                dataPoint.put("indoorTemperature", indoorValues.get(0));
                            }
                            if (indoorValues.size() >= 2) {
                                dataPoint.put("indoorHumidity", indoorValues.get(1));
                            }
                            if (indoorValues.size() >= 3) {
                                dataPoint.put("indoorPressure", indoorValues.get(2));
                            }
                        }
                        
                        // Add outdoor values if available
                        if (outdoorDataPoints != null && i < outdoorDataPoints.size()) {
                            java.util.List<Object> outdoorPoint = outdoorDataPoints.get(i);
                            if (outdoorPoint.size() >= 2) { // First element is timestamp
                                dataPoint.put("outdoorTemperature", outdoorPoint.get(1));
                            }
                            if (outdoorPoint.size() >= 3) {
                                dataPoint.put("outdoorHumidity", outdoorPoint.get(2));
                            }
                        }
                        
                        result.values.add(dataPoint);
                    }
                }
                
                result.totalDataPoints = result.values != null ? result.values.size() : 0;
                
                return (HistoricalWeatherData) result.withSuccess();
            } else {
                throw new WeatherApiException("Could not parse measurement data from Netatmo response",
                                            ErrorType.API_ERROR);
            }
            
        } catch (WeatherApiException e) {
            // Re-throw WeatherApiExceptions
            throw e;
        } catch (Exception e) {
            logger.severe("Error getting historical weather: " + e.getMessage());
            throw new WeatherApiException("Error retrieving historical weather data: " + e.getMessage(),
                                        e, ErrorType.API_ERROR);
        }
    }
}
