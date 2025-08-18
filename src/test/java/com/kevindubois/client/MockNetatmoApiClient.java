package com.kevindubois.client;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import com.kevindubois.dto.NetatmoHistoricalDataResponse;
import com.kevindubois.dto.NetatmoStationsDataResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of NetatmoApiClient for testing
 */
@Mock
@ApplicationScoped
@RestClient
public class MockNetatmoApiClient implements NetatmoApiClient {

    @Override
    public NetatmoStationsDataResponse getStationsData(String deviceId) {
        // Create mock dashboard data for main device
        NetatmoStationsDataResponse.DashboardData mainDashboardData = new NetatmoStationsDataResponse.DashboardData(
                22.5, // temperature
                45,   // humidity
                1013.2, // pressure
                800,  // co2
                40,   // noise
                System.currentTimeMillis() / 1000, // timeUtc
                21.0, // minTemp
                24.0  // maxTemp
        );

        // Create mock dashboard data for outdoor module
        NetatmoStationsDataResponse.DashboardData outdoorDashboardData = new NetatmoStationsDataResponse.DashboardData(
                18.5, // temperature
                65,   // humidity
                null, // pressure (not applicable for outdoor module)
                null, // co2 (not applicable for outdoor module)
                null, // noise (not applicable for outdoor module)
                System.currentTimeMillis() / 1000, // timeUtc
                16.0, // minTemp
                20.0  // maxTemp
        );

        // Create mock outdoor module
        NetatmoStationsDataResponse.Module outdoorModule = new NetatmoStationsDataResponse.Module(
                "module1",
                "Outdoor Module",
                "NAModule1",
                List.of("Temperature", "Humidity"),
                outdoorDashboardData
        );

        // Create mock weather station with the outdoor module
        NetatmoStationsDataResponse.WeatherStation station = new NetatmoStationsDataResponse.WeatherStation(
                deviceId != null ? deviceId : "station1",
                "Home Weather Station",
                "NAMain",
                List.of("Temperature", "Humidity", "Pressure", "CO2", "Noise"),
                mainDashboardData,
                List.of(outdoorModule)
        );

        // Create mock body with the station
        NetatmoStationsDataResponse.Body body = new NetatmoStationsDataResponse.Body(
                List.of(station)
        );

        // Create and return the full response
        return new NetatmoStationsDataResponse(
                body,
                "ok",
                0.123,
                System.currentTimeMillis() / 1000
        );
    }

    @Override
    public NetatmoStationsDataResponse getStationsData() {
        return getStationsData(null);
    }

    @Override
    public NetatmoHistoricalDataResponse getHistoricalData(
            String deviceId, String moduleId, String scale, String type,
            Long dateBegin, Long dateEnd, Integer limit, Boolean optimize, Boolean realTime) {
        
        // Create a list of mock measurements
        List<List<Object>> measurements = new ArrayList<>();
        long timestamp = dateBegin;
        long stepTime = "1hour".equals(scale) ? 3600 : 86400; // 1 hour or 1 day in seconds
        
        int count = limit != null && limit > 0 ? limit : 24;
        for (int i = 0; i < count; i++) {
            if ("Temperature,Humidity,Pressure".equals(type)) {
                // Indoor measurements
                measurements.add(List.of(
                    20.0 + Math.random() * 5, // Temperature between 20-25
                    40 + (int)(Math.random() * 20), // Humidity between 40-60
                    1010.0 + Math.random() * 10 // Pressure between 1010-1020
                ));
            } else if ("Temperature,Humidity".equals(type)) {
                // Outdoor measurements
                measurements.add(List.of(
                    15.0 + Math.random() * 10, // Temperature between 15-25
                    50 + (int)(Math.random() * 30) // Humidity between 50-80
                ));
            }
            timestamp += stepTime;
        }
        
        // Create measurement data map
        Map<String, Object> measurementData = new HashMap<>();
        measurementData.put("beg_time", dateBegin);
        measurementData.put("step_time", stepTime);
        measurementData.put("value", measurements);
        
        // Create and return the response
        return new NetatmoHistoricalDataResponse(
            List.of(measurementData),
            "ok",
            0.123,
            System.currentTimeMillis() / 1000
        );
    }
}


