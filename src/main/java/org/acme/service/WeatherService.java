package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.NetatmoApiClient;
import org.acme.dto.*;
import org.acme.exception.WeatherApiException;
import org.acme.exception.WeatherApiException.ErrorType;
import org.acme.util.DateTimeUtil;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class WeatherService {

    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    // Cache for device list to reduce API calls
    private DeviceListResult deviceListCache;
    private long deviceListCacheTimestamp;
    private static final long CACHE_TTL_MS = 60000; // 1 minute cache TTL
    // Default values
    public static final String DEFAULT_SCALE = "1hour";
    public static final String DEFAULT_SENSOR_TYPES = "Temperature,Humidity,Pressure";
    public static final int DEFAULT_DAYS_BACK = 7;
    public static final int DEFAULT_LIMIT = 1024;

    // Cache for station data to reduce API calls
    private Map<String, NetatmoStationsDataResponse> stationDataCache = new HashMap<>();
    private Map<String, Long> stationDataCacheTimestamps = new HashMap<>();

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
     * Fetch data for a specific weather station with caching
     * @param deviceId The device ID of the station to fetch
     * @return The raw Netatmo stations data response
     * @throws WeatherApiException if there's an error fetching the data
     */
    public NetatmoStationsDataResponse fetchStation(String deviceId) {
        // Check if we have a valid cache
        long currentTime = System.currentTimeMillis();
        if (stationDataCache.containsKey(deviceId) && 
            (currentTime - stationDataCacheTimestamps.getOrDefault(deviceId, 0L)) < CACHE_TTL_MS) {
            return stationDataCache.get(deviceId);
        }
        
        try {
            logger.info("Fetching weather station data for device: " + deviceId);
            NetatmoStationsDataResponse response = netatmoApiClient.getStationsData(deviceId);
            
            // Update cache
            stationDataCache.put(deviceId, response);
            stationDataCacheTimestamps.put(deviceId, currentTime);
            
            return response;
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
     * Get a list of available weather station devices with caching
     * @return List of device information
     */
    public DeviceListResult getAvailableDevices() {
        // Check if we have a valid cache
        long currentTime = System.currentTimeMillis();
        if (deviceListCache != null && (currentTime - deviceListCacheTimestamp) < CACHE_TTL_MS) {
            return deviceListCache;
        }
        
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
                
                // Update cache
                deviceListCache = (DeviceListResult) result.withSuccess();
                deviceListCacheTimestamp = currentTime;
                
                return deviceListCache;
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
     * Helper class to store outdoor module data
     */
    private static class OutdoorModuleData {
        String moduleId;
        String moduleName;
        Double currentTemperature;
        Integer currentHumidity;
        List<List<Object>> dataPoints;
    }

    /**
     * Fetches outdoor module data for a specific device
     * 
     * @param deviceId The device ID
     * @param dateBegin The begin timestamp
     * @param dateEnd The end timestamp
     * @param scale The time scale
     * @param sensorTypes The sensor types to retrieve
     * @param limit Maximum number of data points to retrieve
     * @return A tuple containing the outdoor module ID, name, current data, and historical data points
     */
    private OutdoorModuleData fetchOutdoorModuleData(String deviceId, Long dateBegin, Long dateEnd, 
                                                   String scale, String sensorTypes, Integer limit) {
        OutdoorModuleData result = new OutdoorModuleData();
        
        try {
            // Fetch the station data to get the outdoor module ID
            NetatmoStationsDataResponse stationResponse = fetchStation(deviceId);
            
            if (stationResponse.getBody() != null &&
                stationResponse.getBody().getDevices() != null &&
                !stationResponse.getBody().getDevices().isEmpty()) {
                
                var device = stationResponse.getBody().getDevices().get(0);
                
                if (device.getModules() != null && !device.getModules().isEmpty()) {
                    // Get the first outdoor module
                    var outdoorModule = device.getModules().get(0);
                    result.moduleId = outdoorModule.getId();
                    result.moduleName = outdoorModule.getModuleName();
                    
                    // Get current outdoor data
                    if (outdoorModule.getDashboardData() != null) {
                        result.currentTemperature = outdoorModule.getDashboardData().getTemperature();
                        result.currentHumidity = outdoorModule.getDashboardData().getHumidity();
                    }
                    
                    // Get historical data for the outdoor module
                    NetatmoHistoricalDataResponse outdoorResponse = netatmoApiClient.getHistoricalData(
                        deviceId, result.moduleId, scale, sensorTypes, dateBegin, dateEnd, limit, true, true
                    );
                    
                    var outdoorParsedData = outdoorResponse.getParsedMeasurementData();
                    if (outdoorParsedData != null && outdoorParsedData.values != null) {
                        result.dataPoints = new java.util.ArrayList<>();
                        
                        // Process outdoor data points
                        for (int i = 0; i < outdoorParsedData.values.size(); i++) {
                            long timestamp = outdoorParsedData.beginTime + (i * outdoorParsedData.stepTime);
                            Object value = outdoorParsedData.values.get(i);
                            
                            List<Object> outdoorPoint = new java.util.ArrayList<>();
                            outdoorPoint.add(timestamp);
                            
                            if (value instanceof List) {
                                outdoorPoint.addAll((List<Object>) value);
                            } else {
                                outdoorPoint.add(value);
                            }
                            
                            result.dataPoints.add(outdoorPoint);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error fetching outdoor module data: " + e.getMessage());
            // Continue without outdoor data
        }
        
        return result;
    }

    /**
     * Process and combine indoor and outdoor data points
     *
     * @param parsedData The parsed indoor data
     * @param outdoorDataPoints The outdoor data points
     * @param beginTimeTimestamp The begin time timestamp
     * @param stepTime The step time
     * @return The combined data points
     */
    private List<Object> processDataPoints(
            NetatmoHistoricalDataResponse.NetatmoMeasurementData parsedData,
            List<List<Object>> outdoorDataPoints,
            long beginTimeTimestamp,
            int stepTime) {
        
        List<Object> result = new java.util.ArrayList<>();
        
        if (parsedData.values != null) {
            for (int i = 0; i < parsedData.values.size(); i++) {
                long timestamp = beginTimeTimestamp + (i * stepTime);
                Object indoorValue = parsedData.values.get(i);
                
                // Format timestamp as ISO string (yyyy-MM-dd HH:mm) using UTC
                String formattedTimestamp = DateTimeUtil.formatTimestamp(timestamp, "yyyy-MM-dd HH:mm");
                
                // Create a map for this data point
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("timestamp", formattedTimestamp);
                
                // Add indoor values
                if (indoorValue instanceof List) {
                    List<Object> indoorValues = (List<Object>) indoorValue;
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
                    List<Object> outdoorPoint = outdoorDataPoints.get(i);
                    if (outdoorPoint.size() >= 2) { // First element is timestamp
                        dataPoint.put("outdoorTemperature", outdoorPoint.get(1));
                    }
                    if (outdoorPoint.size() >= 3) {
                        dataPoint.put("outdoorHumidity", outdoorPoint.get(2));
                    }
                }
                
                result.add(dataPoint);
            }
        }
        
        return result;
    }

    /**
     * Get historical weather data
     * @param deviceId The device ID (optional)
     * @param moduleId The module ID (optional)
     * @param scale The time scale (e.g., "1hour", "1day")
     * @param sensorTypes The sensor types to retrieve (comma-separated)
     * @param dateBegin Begin timestamp in seconds (optional)
     * @param dateEnd End timestamp in seconds (optional)
     * @param limit Maximum number of data points to retrieve
     * @return Historical weather data result
     */
    public HistoricalWeatherData getHistoricalWeather(String deviceId, String moduleId, String scale,
                                                     String sensorTypes, String beginDate, String endDate, Integer limit) {
        try {
            // Normalize parameters using the utility class
            scale = normalizeScale(scale);
            sensorTypes = normalizeSensorTypes(sensorTypes);
            limit = normalizeLimit(limit);
            
            // Set default values for dateBegin and dateEnd if not provided
            Long dateBegin = parseBeginDate(beginDate);
            Long dateEnd = parseEndDate(endDate);

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

            // Log parameters
            logger.info("Requesting historical data with parameters: device_id=" + deviceId +
                       ", scale=" + scale + ", type=" + sensorTypes +
                       ", date_begin=" + dateBegin + ", date_end=" + dateEnd + ", limit=" + limit);

            // Get indoor data
            NetatmoHistoricalDataResponse response = netatmoApiClient.getHistoricalData(
                deviceId, moduleId, scale, sensorTypes, dateBegin, dateEnd, limit, true, true
            );

            // Initialize result object
            var result = new HistoricalWeatherData();
            result.deviceId = deviceId;
            result.scale = scale;
            result.sensorTypes = List.of(sensorTypes.split(","));
            result.status = response.getStatus();

            // Get outdoor module data
            OutdoorModuleData outdoorData = fetchOutdoorModuleData(deviceId, dateBegin, dateEnd, scale, sensorTypes, limit);
            if (outdoorData != null) {
                result.outdoorModuleId = outdoorData.moduleId;
                result.outdoorModuleName = outdoorData.moduleName;
                result.outdoorTemperature = outdoorData.currentTemperature;
                result.outdoorHumidity = outdoorData.currentHumidity;
            }

            // Process indoor data
            var parsedData = response.getParsedMeasurementData();
            if (parsedData != null) {
                // Store timestamps
                result.beginTimeTimestamp = dateBegin;
                result.endTimeTimestamp = dateEnd;
                result.stepTime = parsedData.stepTime;

                // Process and combine data points
                result.values = processDataPoints(
                    parsedData, 
                    outdoorData != null ? outdoorData.dataPoints : null,
                    parsedData.beginTime,
                    parsedData.stepTime
                );
                
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

    /**
     * Normalizes the scale parameter
     *
     * @param scale The scale parameter
     * @return The normalized scale parameter
     */
    public static String normalizeScale(String scale) {
        if (scale == null || scale.trim().isEmpty()) {
            return DEFAULT_SCALE;
        }
        return scale;
    }

    /**
     * Normalizes the sensor types parameter
     *
     * @param sensorTypes The sensor types parameter
     * @return The normalized sensor types parameter
     */
    public static String normalizeSensorTypes(String sensorTypes) {
        if (sensorTypes == null || sensorTypes.trim().isEmpty()) {
            return DEFAULT_SENSOR_TYPES;
        }
        return sensorTypes;
    }

    /**
     * Normalizes the limit parameter
     *
     * @param limit The limit parameter
     * @return The normalized limit parameter
     */
    public static Integer normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return limit;
    }

    /**
     * Parse the begin date parameter
     * @param beginDate Date string in format YYYY-MM-DD
     * @return Timestamp in seconds since epoch
     */
    private Long parseBeginDate(String beginDate) {
        if (beginDate != null && !beginDate.trim().isEmpty()) {
            // Use the utility method to parse the date
            Long timestamp = DateTimeUtil.parseTimestamp(beginDate.trim(), "yyyy-MM-dd");
            if (timestamp != null) {
                return timestamp;
            }
        }
        // Default to 7 days ago
        return DateTimeUtil.getTimestampDaysAgo(DEFAULT_DAYS_BACK);
    }

    /**
     * Parse the end date parameter
     * @param endDate Date string in format YYYY-MM-DD
     * @return Timestamp in seconds since epoch
     */
    private Long parseEndDate(String endDate) {
        if (endDate != null && !endDate.trim().isEmpty()) {
            // Use the utility method to parse the date
            Long timestamp = DateTimeUtil.parseTimestamp(endDate.trim(), "yyyy-MM-dd");
            if (timestamp != null) {
                // Set to end of day (23:59:59)
                return timestamp + 86399; // Add seconds in a day minus 1
            }
            // If parsing fails, use current time
            return DateTimeUtil.getCurrentTimestamp();
        }
        // Default to current time
        return DateTimeUtil.getCurrentTimestamp();
    }
}
