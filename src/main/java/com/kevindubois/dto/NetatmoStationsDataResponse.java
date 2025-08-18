package com.kevindubois.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NetatmoStationsDataResponse extends BaseResult {
    @JsonProperty("body")
    private final Body body;
    
    @JsonProperty("status")
    private final String status;

    @JsonProperty("time_exec")
    private final Double timeExec;

    @JsonProperty("time_server")
    private final Long timeServer;
    
    /**
     * Constructor for deserialization by Jackson
     */
    public NetatmoStationsDataResponse(
            @JsonProperty("body") Body body,
            @JsonProperty("status") String status,
            @JsonProperty("time_exec") Double timeExec,
            @JsonProperty("time_server") Long timeServer) {
        this.body = body;
        this.status = status;
        this.timeExec = timeExec;
        this.timeServer = timeServer;
    }

    public Body getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public Double getTimeExec() {
        return timeExec;
    }

    public Long getTimeServer() {
        return timeServer;
    }

    /**
     * Body class for Netatmo stations data response
     */
    public static class Body {
        @JsonProperty("devices")
        private final List<WeatherStation> devices;

        public Body(@JsonProperty("devices") List<WeatherStation> devices) {
            this.devices = devices;
        }

        public List<WeatherStation> getDevices() {
            return devices;
        }
    }

    /**
     * Weather station device class
     */
    public static class WeatherStation {
        @JsonProperty("_id")
        private final String id;

        @JsonProperty("station_name")
        private final String stationName;

        @JsonProperty("type")
        private final String type;

        @JsonProperty("data_type")
        private final List<String> dataType;

        @JsonProperty("dashboard_data")
        private final DashboardData dashboardData;

        @JsonProperty("modules")
        private final List<Module> modules;
        
        public WeatherStation(
                @JsonProperty("_id") String id,
                @JsonProperty("station_name") String stationName,
                @JsonProperty("type") String type,
                @JsonProperty("data_type") List<String> dataType,
                @JsonProperty("dashboard_data") DashboardData dashboardData,
                @JsonProperty("modules") List<Module> modules) {
            this.id = id;
            this.stationName = stationName;
            this.type = type;
            this.dataType = dataType;
            this.dashboardData = dashboardData;
            this.modules = modules;
        }

        public String getId() {
            return id;
        }

        public String getStationName() {
            return stationName;
        }

        public String getType() {
            return type;
        }

        public List<String> getDataType() {
            return dataType;
        }

        public DashboardData getDashboardData() {
            return dashboardData;
        }

        public List<Module> getModules() {
            return modules;
        }
    }

    /**
     * Module class for weather station modules
     */
    public static class Module {
        @JsonProperty("_id")
        private final String id;

        @JsonProperty("module_name")
        private final String moduleName;

        @JsonProperty("type")
        private final String type;

        @JsonProperty("data_type")
        private final List<String> dataType;

        @JsonProperty("dashboard_data")
        private final DashboardData dashboardData;
        
        public Module(
                @JsonProperty("_id") String id,
                @JsonProperty("module_name") String moduleName,
                @JsonProperty("type") String type,
                @JsonProperty("data_type") List<String> dataType,
                @JsonProperty("dashboard_data") DashboardData dashboardData) {
            this.id = id;
            this.moduleName = moduleName;
            this.type = type;
            this.dataType = dataType;
            this.dashboardData = dashboardData;
        }

        public String getId() {
            return id;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getType() {
            return type;
        }

        public List<String> getDataType() {
            return dataType;
        }

        public DashboardData getDashboardData() {
            return dashboardData;
        }
    }

    /**
     * Dashboard data class for weather measurements
     */
    public static class DashboardData {
        @JsonProperty("Temperature")
        private final Double temperature;

        @JsonProperty("Humidity")
        private final Integer humidity;

        @JsonProperty("Pressure")
        private final Double pressure;

        @JsonProperty("CO2")
        private final Integer co2;

        @JsonProperty("Noise")
        private final Integer noise;

        @JsonProperty("time_utc")
        private final Long timeUtc;

        @JsonProperty("min_temp")
        private final Double minTemp;

        @JsonProperty("max_temp")
        private final Double maxTemp;
        
        public DashboardData(
                @JsonProperty("Temperature") Double temperature,
                @JsonProperty("Humidity") Integer humidity,
                @JsonProperty("Pressure") Double pressure,
                @JsonProperty("CO2") Integer co2,
                @JsonProperty("Noise") Integer noise,
                @JsonProperty("time_utc") Long timeUtc,
                @JsonProperty("min_temp") Double minTemp,
                @JsonProperty("max_temp") Double maxTemp) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.pressure = pressure;
            this.co2 = co2;
            this.noise = noise;
            this.timeUtc = timeUtc;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public Double getPressure() {
            return pressure;
        }

        public Integer getCo2() {
            return co2;
        }

        public Integer getNoise() {
            return noise;
        }

        public Long getTimeUtc() {
            return timeUtc;
        }

        public Double getMinTemp() {
            return minTemp;
        }

        public Double getMaxTemp() {
            return maxTemp;
        }
    }
}


