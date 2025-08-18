package com.kevindubois.service;

import com.kevindubois.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import com.kevindubois.client.NetatmoApiClient;
import com.kevindubois.exception.WeatherApiException;
import com.kevindubois.util.WeatherUtil;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class WeatherService {

    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    // Cache to reduce API calls
    private Map<String, CacheEntry<?>> cache = new HashMap<>();
    private static final long CACHE_TTL_MS = 60000; // 1 minute cache TTL
    
    // Default values
    public static final String DEFAULT_SCALE = "1hour";
    public static final String DEFAULT_SENSOR_TYPES = "Temperature,Humidity,Pressure";
    public static final int DEFAULT_DAYS_BACK = 7;
    public static final int DEFAULT_LIMIT = 1024;
    
    // Cache keys
    private static final String DEVICE_LIST_CACHE_KEY = "device_list";
    private static final String STATION_DATA_CACHE_PREFIX = "station_";
    
    /**
     * Simple cache entry class to store data with timestamp
     */
    private static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        
        public CacheEntry(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public T getData() {
            return data;
        }
        
        public boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < CACHE_TTL_MS;
        }
    }

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
                                         e, Response.Status.BAD_GATEWAY);
        }
    }

    /**
     * Fetch data for a specific weather station with caching
     */
    public NetatmoStationsDataResponse fetchStation(String deviceId) {
        // Check cache first
        String cacheKey = STATION_DATA_CACHE_PREFIX + deviceId;
        
        CacheEntry<NetatmoStationsDataResponse> entry = getCacheEntry(cacheKey, NetatmoStationsDataResponse.class);
        
        if (entry != null && entry.isValid()) {
            return entry.getData();
        }
        
        try {
            logger.info("Fetching weather station data for device: " + deviceId);
            NetatmoStationsDataResponse response = netatmoApiClient.getStationsData(deviceId);
            
            // Update cache
            cache.put(cacheKey, new CacheEntry<>(response));
            
            return response;
        } catch (Exception e) {
            logger.severe("Error fetching station " + deviceId + ": " + e.getMessage());
            throw new WeatherApiException("Error fetching station " + deviceId + ": " + e.getMessage(),
                                         e, Response.Status.BAD_GATEWAY);
        }
    }

    /**
     * Helper method to get a typed cache entry
     */
    @SuppressWarnings("unchecked")
    private <T> CacheEntry<T> getCacheEntry(String key, Class<?> type) {
        Object entry = cache.get(key);
        if (entry instanceof CacheEntry<?>) {
            try {
                return (CacheEntry<T>) entry;
            } catch (ClassCastException e) {
                logger.warning("Cache entry type mismatch for key: " + key);
                return null;
            }
        }
        return null;
    }

    /**
     * Get current weather data from the first available station
     * @return Processed current weather data or null if error
     */
    public ApiResponse<CurrentWeatherData> getCurrentWeather() {
        try {
            NetatmoStationsDataResponse response = netatmoApiClient.getStationsData();
            
            if (response.getBody() == null ||
                response.getBody().getDevices() == null ||
                response.getBody().getDevices().isEmpty()) {
                return ApiResponse.error("No weather station data available", Response.Status.NOT_FOUND);
            }
            
            var device = response.getBody().getDevices().get(0);
            
            if (device.getDashboardData() == null) {
                return ApiResponse.error("No dashboard data available", Response.Status.NOT_FOUND);
            }
            
            var dashboardData = device.getDashboardData();
            var data = new CurrentWeatherData()
                .withStationName(device.getStationName())
                .withIndoorTemperature(dashboardData.getTemperature())
                .withIndoorHumidity(dashboardData.getHumidity())
                .withPressure(dashboardData.getPressure())
                .withCo2(dashboardData.getCo2())
                .withNoise(dashboardData.getNoise())
                .withTimeUtc(dashboardData.getTimeUtc());
            
            // Set outdoor data if available
            if (device.getModules() != null && !device.getModules().isEmpty()) {
                var outdoorModule = device.getModules().get(0);
                if (outdoorModule.getDashboardData() != null) {
                    var outdoorData = outdoorModule.getDashboardData();
                    data = data
                        .withOutdoorTemperature(outdoorData.getTemperature())
                        .withOutdoorHumidity(outdoorData.getHumidity())
                        .withOutdoorMaxTemperature(outdoorData.getMaxTemp())
                        .withOutdoorMinTemperature(outdoorData.getMinTemp());
                }
            }
            
            return ApiResponse.success(data);
            
        } catch (Exception e) {
            logger.severe("Error getting current weather: " + e.getMessage());
            return ApiResponse.error("Error retrieving current weather data: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a list of available weather station devices with caching
     * @return List of device information
     */
    public ApiResponse<List<DeviceInfo>> getAvailableDevices() {
        // Check if we have a valid cache
        CacheEntry<ApiResponse<List<DeviceInfo>>> entry = 
            getCacheEntry(DEVICE_LIST_CACHE_KEY, ApiResponse.class);
        
        if (entry != null && entry.isValid()) {
            return entry.getData();
        }
        
        try {
            NetatmoStationsDataResponse response = netatmoApiClient.getStationsData();
            
            if (response.getBody() == null ||
                response.getBody().getDevices() == null ||
                response.getBody().getDevices().isEmpty()) {
                return ApiResponse.error("No weather stations found", Response.Status.NOT_FOUND);
            }
            
            // Map devices to DeviceInfo objects
            var devices = response.getBody().getDevices().stream()
                .map(device -> new DeviceInfo(
                    device.getId(),
                    device.getStationName(),
                    device.getType(),
                    device.getDataType()
                ))
                .collect(Collectors.toList());
            
            // Create result and update cache
            ApiResponse<List<DeviceInfo>> result = ApiResponse.success(devices);
            cache.put(DEVICE_LIST_CACHE_KEY, new CacheEntry<>(result));
            
            return result;
            
        } catch (Exception e) {
            logger.severe("Error getting available devices: " + e.getMessage());
            return ApiResponse.error("Error retrieving device list: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Simple record to store outdoor module data
     */
    private record OutdoorModuleData(
        String moduleId,
        String moduleName,
        Double currentTemperature,
        Integer currentHumidity,
        List<List<Object>> dataPoints
    ) {}

    /**
     * Fetches outdoor module data for a specific device
     */
    private OutdoorModuleData fetchOutdoorModuleData(String deviceId, Long dateBegin, Long dateEnd,
                                                    String scale, String sensorTypes, Integer limit) {
        try {
            // Fetch the station data to get the outdoor module ID
            NetatmoStationsDataResponse stationResponse = fetchStation(deviceId);
            
            if (stationResponse.getBody() == null ||
                stationResponse.getBody().getDevices() == null ||
                stationResponse.getBody().getDevices().isEmpty()) {
                return null;
            }
            
            var device = stationResponse.getBody().getDevices().get(0);
            
            if (device.getModules() == null || device.getModules().isEmpty()) {
                return null;
            }
            
            // Get the first outdoor module
            var outdoorModule = device.getModules().get(0);
            String moduleId = outdoorModule.getId();
            String moduleName = outdoorModule.getModuleName();
            
            // Get current outdoor data
            Double currentTemperature = null;
            Integer currentHumidity = null;
            if (outdoorModule.getDashboardData() != null) {
                currentTemperature = outdoorModule.getDashboardData().getTemperature();
                currentHumidity = outdoorModule.getDashboardData().getHumidity();
            }
            
            // Get historical data for the outdoor module
            NetatmoHistoricalDataResponse outdoorResponse = netatmoApiClient.getHistoricalData(
                deviceId, moduleId, scale, sensorTypes, dateBegin, dateEnd, limit, true, true
            );
            
            var outdoorParsedData = outdoorResponse.getParsedMeasurementData();
            List<List<Object>> dataPoints = null;
            
            if (outdoorParsedData != null && outdoorParsedData.values != null) {
                dataPoints = new ArrayList<>();
                
                // Process outdoor data points
                for (int i = 0; i < outdoorParsedData.values.size(); i++) {
                    long timestamp = outdoorParsedData.beginTime + (i * outdoorParsedData.stepTime);
                    Object value = outdoorParsedData.values.get(i);
                    
                    List<Object> outdoorPoint = new ArrayList<>();
                    outdoorPoint.add(timestamp);
                    
                    if (value instanceof List<?>) {
                        List<?> valueList = (List<?>) value;
                        for (Object item : valueList) {
                            outdoorPoint.add(item);
                        }
                    } else {
                        outdoorPoint.add(value);
                    }
                    
                    dataPoints.add(outdoorPoint);
                }
            }
            
            return new OutdoorModuleData(moduleId, moduleName, currentTemperature, currentHumidity, dataPoints);
            
        } catch (Exception e) {
            logger.warning("Error fetching outdoor module data: " + e.getMessage());
            return null;
        }
    }

    // Using HistoricalDataUtil.processDataPoints instead of duplicate method

    /**
     * Get historical weather data
     * @param deviceId The device ID (optional)
     * @param moduleId The module ID (optional)
     * @param scale The time scale (e.g., "1hour", "1day")
     * @param sensorTypes The sensor types to retrieve (comma-separated)
     * @param beginDate Begin timestamp in seconds (optional)
     * @param endDate End timestamp in seconds (optional)
     * @param limit Maximum number of data points to retrieve
     * @return Historical weather data result
     */
    public ApiResponse<Map<String, Object>> getHistoricalWeather(String deviceId, String moduleId, String scale,
                                                       String sensorTypes, String beginDate, String endDate, Integer limit) {
        try {
            // Normalize parameters
            scale = WeatherUtil.normalizeParameter(scale, DEFAULT_SCALE);
            sensorTypes = WeatherUtil.normalizeParameter(sensorTypes, DEFAULT_SENSOR_TYPES);
            limit = WeatherUtil.normalizeParameter(limit, DEFAULT_LIMIT);
            
            Long dateBegin = parseBeginDate(beginDate);
            Long dateEnd = parseEndDate(endDate);

            // If no device_id provided, get first available device
            if (deviceId == null || deviceId.trim().isEmpty()) {
                var devicesResult = getAvailableDevices();
                if (!devicesResult.isSuccess() || devicesResult.getData().isEmpty()) {
                    throw new WeatherApiException("No weather stations found. Please provide a valid device_id.",
                                                Response.Status.NOT_FOUND);
                }
                deviceId = devicesResult.getData().get(0).id();
                logger.info("Using device_id: " + deviceId);
            }

            logger.info("Requesting historical data with parameters: device_id=" + deviceId +
                       ", scale=" + scale + ", type=" + sensorTypes +
                       ", date_begin=" + dateBegin + ", date_end=" + dateEnd + ", limit=" + limit);

            // Get indoor data
            NetatmoHistoricalDataResponse response = netatmoApiClient.getHistoricalData(
                deviceId, moduleId, scale, sensorTypes, dateBegin, dateEnd, limit, true, true
            );

            var parsedData = response.getParsedMeasurementData();
            if (parsedData == null) {
                throw new WeatherApiException("Could not parse measurement data from Netatmo response",
                                            Response.Status.BAD_GATEWAY);
            }

            // Get outdoor module data
            OutdoorModuleData outdoorData = fetchOutdoorModuleData(deviceId, dateBegin, dateEnd, scale, sensorTypes, limit);
            
            // Process and combine data points
            List<Object> dataPoints = WeatherUtil.processDataPoints(
                parsedData,
                outdoorData != null ? outdoorData.dataPoints() : null,
                parsedData.beginTime,
                parsedData.stepTime
            );

            // Build result map
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("deviceId", deviceId);
            resultMap.put("scale", scale);
            resultMap.put("sensorTypes", List.of(sensorTypes.split(",")));
            resultMap.put("status", response.getStatus());
            resultMap.put("beginTimeTimestamp", dateBegin);
            resultMap.put("endTimeTimestamp", dateEnd);
            resultMap.put("beginTime", WeatherUtil.formatTimestamp(dateBegin, "yyyy-MM-dd HH:mm:ss"));
            resultMap.put("endTime", WeatherUtil.formatTimestamp(dateEnd, "yyyy-MM-dd HH:mm:ss"));
            resultMap.put("stepTime", parsedData.stepTime);
            resultMap.put("values", dataPoints);
            resultMap.put("totalDataPoints", dataPoints != null ? dataPoints.size() : 0);
            
            // Add outdoor data if available
            if (outdoorData != null) {
                resultMap.put("outdoorModuleId", outdoorData.moduleId());
                resultMap.put("outdoorModuleName", outdoorData.moduleName());
                resultMap.put("outdoorTemperature", outdoorData.currentTemperature());
                resultMap.put("outdoorHumidity", outdoorData.currentHumidity());
            }
            
            return ApiResponse.success(resultMap);
            
        } catch (WeatherApiException e) {
            return ApiResponse.error(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            logger.severe("Error getting historical weather: " + e.getMessage());
            return ApiResponse.serverError("Error retrieving historical weather data: " + e.getMessage());
        }
    }

    /**
     * Parse the begin date parameter
     * @param beginDate Date string in format YYYY-MM-DD
     * @return Timestamp in seconds since epoch
     */
    private Long parseBeginDate(String beginDate) {
        if (beginDate != null && !beginDate.trim().isEmpty()) {
            // Use the utility method to parse the date
            Long timestamp = WeatherUtil.parseTimestamp(beginDate.trim(), "yyyy-MM-dd");
            if (timestamp != null) {
                return timestamp;
            }
        }
        // Default to 7 days ago
        return WeatherUtil.getTimestampDaysAgo(DEFAULT_DAYS_BACK);
    }

    /**
     * Parse the end date parameter
     * @param endDate Date string in format YYYY-MM-DD
     * @return Timestamp in seconds since epoch
     */
    private Long parseEndDate(String endDate) {
        if (endDate != null && !endDate.trim().isEmpty()) {
            // Use the utility method to parse the date
            Long timestamp = WeatherUtil.parseTimestamp(endDate.trim(), "yyyy-MM-dd");
            if (timestamp != null) {
                // Set to end of day (23:59:59)
                return timestamp + 86399; // Add seconds in a day minus 1
            }
            // If parsing fails, use current time
            return WeatherUtil.getCurrentTimestamp();
        }
        // Default to current time
        return WeatherUtil.getCurrentTimestamp();
    }
}


