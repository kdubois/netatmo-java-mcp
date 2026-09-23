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

    private static long stepTimeFor(String scale) {
        if (scale == null) {
            return 3600;
        }
        return switch (scale) {
            case "30min" -> 1800;
            case "1hour" -> 3600;
            case "3hours" -> 10800;
            case "1day" -> 86400;
            case "1week" -> 604800;
            case "1month" -> 2592000;
            default -> 3600;
        };
    }

    private static double indoorTemperature(int i) {
        return 21 + 3 * Math.sin(i * 0.5);
    }

    private static int indoorHumidity(int i) {
        return 45 + (int) (10 * Math.sin(i * 0.3));
    }

    private static double pressure(int i) {
        return 1013 + 2 * Math.sin(i * 0.2);
    }

    private static int co2(int i) {
        return 800 + (int) (400 * Math.abs(Math.sin(i * 0.7)));
    }

    private static double outdoorTemperature(int i) {
        double t = 17 + 4 * Math.sin(i * 0.4);
        if (i % 12 == 6) {
            t += 7;
        }
        return t;
    }

    private static int outdoorHumidity(int i) {
        return 50 + (int) (25 * Math.sin(i * 0.25));
    }

    private static double dailyMin(int i) {
        return 14 + 3 * Math.sin(i * 0.4);
    }

    private static double dailyMax(int i) {
        return 22 + 4 * Math.sin(i * 0.4);
    }

    private static List<Object> indoorPoint(int i, String type) {
        if ("min_temp,max_temp".equals(type)) {
            return List.of(round(dailyMin(i)), round(dailyMax(i)));
        }
        if ("Temperature,CO2,Humidity".equals(type)) {
            return List.of(round(indoorTemperature(i)), co2(i), indoorHumidity(i));
        }
        return List.of(round(indoorTemperature(i)), indoorHumidity(i), round(pressure(i)));
    }

    private static List<Object> outdoorPoint(int i, String type) {
        if ("min_temp,max_temp".equals(type)) {
            return List.of(round(dailyMin(i)), round(dailyMax(i)));
        }
        if ("Temperature,CO2,Humidity".equals(type)) {
            return List.of(round(outdoorTemperature(i)), co2(i), outdoorHumidity(i));
        }
        return List.of(round(outdoorTemperature(i)), outdoorHumidity(i));
    }

    private static double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    @Override
    public NetatmoHistoricalDataResponse getHistoricalData(
            String deviceId, String moduleId, String scale, String type,
            Long dateBegin, Long dateEnd, Integer limit, Boolean optimize, Boolean realTime) {

        long now = System.currentTimeMillis() / 1000;
        long begin = dateBegin != null ? dateBegin : now - 7 * 86400;
        long end = dateEnd != null ? dateEnd : now;
        long step = stepTimeFor(scale);
        long count = (end - begin) / step;
        if (count < 1) {
            count = 1;
        }
        if (count > 1024) {
            count = 1024;
        }
        if (limit != null && limit > 0 && limit < count) {
            count = limit;
        }

        boolean outdoor = moduleId != null && !moduleId.isBlank();
        List<List<Object>> measurements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            measurements.add(outdoor ? outdoorPoint(i, type) : indoorPoint(i, type));
        }

        // Create measurement data map
        Map<String, Object> measurementData = new HashMap<>();
        measurementData.put("beg_time", begin);
        measurementData.put("step_time", step);
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
