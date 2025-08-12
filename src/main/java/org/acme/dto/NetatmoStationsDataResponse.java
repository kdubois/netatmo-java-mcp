package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NetatmoStationsDataResponse {
    @JsonProperty("body")
    private Body body;
    
    @JsonProperty("status")
    private String status;

    @JsonProperty("time_exec")
    private Double timeExec;

    @JsonProperty("time_server")
    private Long timeServer;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTimeExec() {
        return timeExec;
    }

    public void setTimeExec(Double timeExec) {
        this.timeExec = timeExec;
    }

    public Long getTimeServer() {
        return timeServer;
    }

    public void setTimeServer(Long timeServer) {
        this.timeServer = timeServer;
    }

    public static class Body {
        @JsonProperty("devices")
        private List<Device> devices;

        @JsonProperty("user")
        private User user;

        public List<Device> getDevices() {
            return devices;
        }

        public void setDevices(List<Device> devices) {
            this.devices = devices;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public static class User {
        @JsonProperty("administrative")
        private Administrative administrative;

        @JsonProperty("mail")
        private String mail;

        public Administrative getAdministrative() {
            return administrative;
        }

        public void setAdministrative(Administrative administrative) {
            this.administrative = administrative;
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }
    }

    public static class Administrative {
        @JsonProperty("country")
        private String country;

        @JsonProperty("feel_like_algo")
        private Integer feelLikeAlgo;

        @JsonProperty("lang")
        private String lang;

        @JsonProperty("pressureunit")
        private Integer pressureUnit;

        @JsonProperty("reg_locale")
        private String regLocale;

        public String getCountry() {
            return country;
        }

        public Integer getFeelLikeAlgo() {
            return feelLikeAlgo;
        }

        public String getLang() {
            return lang;
        }

        public Integer getPressureUnit() {
            return pressureUnit;
        }

        public String getRegLocale() {
            return regLocale;
        }
    }
    
    // Import Device class definition
    public static class Device {
        @JsonProperty("_id")
        private String id;

        @JsonProperty("cipher_id")
        private String cipherId;

        @JsonProperty("date_setup")
        private Long dateSetup;

        @JsonProperty("last_setup")
        private Long lastSetup;

        @JsonProperty("type")
        private String type;

        @JsonProperty("last_status_store")
        private Long lastStatusStore;

        @JsonProperty("module_name")
        private String moduleName;

        @JsonProperty("firmware")
        private Integer firmware;

        @JsonProperty("last_upgrade")
        private Long lastUpgrade;

        @JsonProperty("wifi_status")
        private Integer wifiStatus;

        @JsonProperty("co2_calibrating")
        private Boolean co2Calibrating;

        @JsonProperty("station_name")
        private String stationName;

        @JsonProperty("data_type")
        private List<String> dataType;

        @JsonProperty("place")
        private Place place;

        @JsonProperty("dashboard_data")
        private DashboardData dashboardData;

        @JsonProperty("modules")
        private List<Module> modules;

        @JsonProperty("reachable")
        private Boolean reachable;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCipherId() {
            return cipherId;
        }

        public void setCipherId(String cipherId) {
            this.cipherId = cipherId;
        }

        public Long getDateSetup() {
            return dateSetup;
        }

        public void setDateSetup(Long dateSetup) {
            this.dateSetup = dateSetup;
        }

        public Long getLastSetup() {
            return lastSetup;
        }

        public void setLastSetup(Long lastSetup) {
            this.lastSetup = lastSetup;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getLastStatusStore() {
            return lastStatusStore;
        }

        public void setLastStatusStore(Long lastStatusStore) {
            this.lastStatusStore = lastStatusStore;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public Integer getFirmware() {
            return firmware;
        }

        public void setFirmware(Integer firmware) {
            this.firmware = firmware;
        }

        public Long getLastUpgrade() {
            return lastUpgrade;
        }

        public void setLastUpgrade(Long lastUpgrade) {
            this.lastUpgrade = lastUpgrade;
        }

        public Integer getWifiStatus() {
            return wifiStatus;
        }

        public void setWifiStatus(Integer wifiStatus) {
            this.wifiStatus = wifiStatus;
        }

        public Boolean getCo2Calibrating() {
            return co2Calibrating;
        }

        public void setCo2Calibrating(Boolean co2Calibrating) {
            this.co2Calibrating = co2Calibrating;
        }

        public String getStationName() {
            return stationName;
        }

        public void setStationName(String stationName) {
            this.stationName = stationName;
        }

        public List<String> getDataType() {
            return dataType;
        }

        public void setDataType(List<String> dataType) {
            this.dataType = dataType;
        }

        public Place getPlace() {
            return place;
        }

        public void setPlace(Place place) {
            this.place = place;
        }

        public DashboardData getDashboardData() {
            return dashboardData;
        }

        public void setDashboardData(DashboardData dashboardData) {
            this.dashboardData = dashboardData;
        }

        public List<Module> getModules() {
            return modules;
        }

        public void setModules(List<Module> modules) {
            this.modules = modules;
        }

        public Boolean getReachable() {
            return reachable;
        }

        public void setReachable(Boolean reachable) {
            this.reachable = reachable;
        }
    }
    
    public static class Place {
        @JsonProperty("altitude")
        private Integer altitude;

        @JsonProperty("city")
        private String city;

        @JsonProperty("country")
        private String country;

        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("location")
        private List<Double> location;

        public Integer getAltitude() {
            return altitude;
        }

        public void setAltitude(Integer altitude) {
            this.altitude = altitude;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public List<Double> getLocation() {
            return location;
        }

        public void setLocation(List<Double> location) {
            this.location = location;
        }
    }
    
    public static class DashboardData {
        @JsonProperty("time_utc")
        private Long timeUtc;

        @JsonProperty("Temperature")
        private Double temperature;

        @JsonProperty("CO2")
        private Integer co2;

        @JsonProperty("Humidity")
        private Integer humidity;

        @JsonProperty("Noise")
        private Integer noise;

        @JsonProperty("Pressure")
        private Double pressure;

        @JsonProperty("AbsolutePressure")
        private Double absolutePressure;

        @JsonProperty("min_temp")
        private Double minTemp;

        @JsonProperty("max_temp")
        private Double maxTemp;

        @JsonProperty("date_min_temp")
        private Long dateMinTemp;

        @JsonProperty("date_max_temp")
        private Long dateMaxTemp;

        @JsonProperty("temp_trend")
        private String tempTrend;

        @JsonProperty("pressure_trend")
        private String pressureTrend;

        @JsonProperty("Rain")
        private Double rain;

        @JsonProperty("sum_rain_1")
        private Double sumRain1;

        @JsonProperty("sum_rain_24")
        private Double sumRain24;

        @JsonProperty("WindStrength")
        private Double windStrength;

        @JsonProperty("WindAngle")
        private Integer windAngle;

        @JsonProperty("GustStrength")
        private Double gustStrength;

        @JsonProperty("GustAngle")
        private Integer gustAngle;

        @JsonProperty("max_wind_str")
        private Double maxWindStr;

        @JsonProperty("max_wind_angle")
        private Integer maxWindAngle;

        @JsonProperty("date_max_wind_str")
        private Long dateMaxWindStr;

        // Getters and setters
        public Long getTimeUtc() {
            return timeUtc;
        }

        public void setTimeUtc(Long timeUtc) {
            this.timeUtc = timeUtc;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getCo2() {
            return co2;
        }

        public void setCo2(Integer co2) {
            this.co2 = co2;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public void setHumidity(Integer humidity) {
            this.humidity = humidity;
        }

        public Integer getNoise() {
            return noise;
        }

        public void setNoise(Integer noise) {
            this.noise = noise;
        }

        public Double getPressure() {
            return pressure;
        }

        public void setPressure(Double pressure) {
            this.pressure = pressure;
        }

        public Double getAbsolutePressure() {
            return absolutePressure;
        }

        public void setAbsolutePressure(Double absolutePressure) {
            this.absolutePressure = absolutePressure;
        }

        public Double getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(Double minTemp) {
            this.minTemp = minTemp;
        }

        public Double getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(Double maxTemp) {
            this.maxTemp = maxTemp;
        }

        public Long getDateMinTemp() {
            return dateMinTemp;
        }

        public void setDateMinTemp(Long dateMinTemp) {
            this.dateMinTemp = dateMinTemp;
        }

        public Long getDateMaxTemp() {
            return dateMaxTemp;
        }

        public void setDateMaxTemp(Long dateMaxTemp) {
            this.dateMaxTemp = dateMaxTemp;
        }

        public String getTempTrend() {
            return tempTrend;
        }

        public void setTempTrend(String tempTrend) {
            this.tempTrend = tempTrend;
        }

        public String getPressureTrend() {
            return pressureTrend;
        }

        public void setPressureTrend(String pressureTrend) {
            this.pressureTrend = pressureTrend;
        }

        public Double getRain() {
            return rain;
        }

        public void setRain(Double rain) {
            this.rain = rain;
        }

        public Double getSumRain1() {
            return sumRain1;
        }

        public void setSumRain1(Double sumRain1) {
            this.sumRain1 = sumRain1;
        }

        public Double getSumRain24() {
            return sumRain24;
        }

        public void setSumRain24(Double sumRain24) {
            this.sumRain24 = sumRain24;
        }

        public Double getWindStrength() {
            return windStrength;
        }

        public void setWindStrength(Double windStrength) {
            this.windStrength = windStrength;
        }

        public Integer getWindAngle() {
            return windAngle;
        }

        public void setWindAngle(Integer windAngle) {
            this.windAngle = windAngle;
        }

        public Double getGustStrength() {
            return gustStrength;
        }

        public void setGustStrength(Double gustStrength) {
            this.gustStrength = gustStrength;
        }

        public Integer getGustAngle() {
            return gustAngle;
        }

        public void setGustAngle(Integer gustAngle) {
            this.gustAngle = gustAngle;
        }

        public Double getMaxWindStr() {
            return maxWindStr;
        }

        public void setMaxWindStr(Double maxWindStr) {
            this.maxWindStr = maxWindStr;
        }

        public Integer getMaxWindAngle() {
            return maxWindAngle;
        }

        public void setMaxWindAngle(Integer maxWindAngle) {
            this.maxWindAngle = maxWindAngle;
        }

        public Long getDateMaxWindStr() {
            return dateMaxWindStr;
        }

        public void setDateMaxWindStr(Long dateMaxWindStr) {
            this.dateMaxWindStr = dateMaxWindStr;
        }
    }
    
    public static class Module {
        @JsonProperty("_id")
        private String id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("module_name")
        private String moduleName;

        @JsonProperty("data_type")
        private List<String> dataType;

        @JsonProperty("last_setup")
        private Long lastSetup;

        @JsonProperty("dashboard_data")
        private DashboardData dashboardData;

        @JsonProperty("firmware")
        private Integer firmware;

        @JsonProperty("last_message")
        private Long lastMessage;

        @JsonProperty("last_seen")
        private Long lastSeen;

        @JsonProperty("rf_status")
        private Integer rfStatus;

        @JsonProperty("battery_vp")
        private Integer batteryVp;

        @JsonProperty("battery_percent")
        private Integer batteryPercent;

        @JsonProperty("reachable")
        private Boolean reachable;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public List<String> getDataType() {
            return dataType;
        }

        public void setDataType(List<String> dataType) {
            this.dataType = dataType;
        }

        public Long getLastSetup() {
            return lastSetup;
        }

        public void setLastSetup(Long lastSetup) {
            this.lastSetup = lastSetup;
        }

        public DashboardData getDashboardData() {
            return dashboardData;
        }

        public void setDashboardData(DashboardData dashboardData) {
            this.dashboardData = dashboardData;
        }

        public Integer getFirmware() {
            return firmware;
        }

        public void setFirmware(Integer firmware) {
            this.firmware = firmware;
        }

        public Long getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(Long lastMessage) {
            this.lastMessage = lastMessage;
        }

        public Long getLastSeen() {
            return lastSeen;
        }

        public void setLastSeen(Long lastSeen) {
            this.lastSeen = lastSeen;
        }

        public Integer getRfStatus() {
            return rfStatus;
        }

        public void setRfStatus(Integer rfStatus) {
            this.rfStatus = rfStatus;
        }

        public Integer getBatteryVp() {
            return batteryVp;
        }

        public void setBatteryVp(Integer batteryVp) {
            this.batteryVp = batteryVp;
        }

        public Integer getBatteryPercent() {
            return batteryPercent;
        }

        public void setBatteryPercent(Integer batteryPercent) {
            this.batteryPercent = batteryPercent;
        }

        public Boolean getReachable() {
            return reachable;
        }

        public void setReachable(Boolean reachable) {
            this.reachable = reachable;
        }
    }
}