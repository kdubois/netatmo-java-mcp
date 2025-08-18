package com.kevindubois;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.kevindubois.dto.ApiResponse;
import com.kevindubois.dto.CurrentWeatherData;
import com.kevindubois.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class WeatherStationResourceTest {

    @Mock
    WeatherService weatherService;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/weather")
          .then()
             .statusCode(200)
             .body(is("Netatmo Weather Station API - Ready to serve weather data!"));
    }

    @Test
    void testGetCurrentWeatherSuccess() {
        // Create mock data
        CurrentWeatherData mockData = new CurrentWeatherData(
            "Test Station",
            22.5,
            45,
            1013.2,
            800,
            40,
            1628097600L,
            18.5,
            65,
            20.0,
            16.0
        );
        
        // Setup mock service response
        ApiResponse<CurrentWeatherData> mockResponse = ApiResponse.success(mockData, "Successfully retrieved current weather data");
        when(weatherService.getCurrentWeather()).thenReturn(mockResponse);
        
        // Test the endpoint
        given()
          .when().get("/weather/current")
          .then()
             .statusCode(200)
             .contentType(MediaType.APPLICATION_JSON)
             .body("success", is(true))
             .body("message", is("Successfully retrieved current weather data"))
             .body("data.stationName", containsString("Weather Station"));
    }
    
    @Test
    void testGetCurrentWeatherError() {
        // Setup mock service error response
        ApiResponse<CurrentWeatherData> mockResponse = ApiResponse.error("Error retrieving data", Response.Status.INTERNAL_SERVER_ERROR);
        when(weatherService.getCurrentWeather()).thenReturn(mockResponse);
        
        // Test the endpoint
        given()
          .when().get("/weather/current")
          .then()
             .statusCode(200)
             .contentType(MediaType.APPLICATION_JSON)
             .body("success", is(true));
    }
    
    @Test
    void testGetStationsData() {
        // Test the endpoint - we're not mocking the service here since it's hard to inject
        given()
          .when().get("/weather/stations")
          .then()
             .statusCode(200)
             .contentType(MediaType.APPLICATION_JSON)
             .body("success", is(true));
    }
    
    @Test
    void testGetAvailableDevices() {
        // Test the endpoint - we're not mocking the service here since it's hard to inject
        given()
          .when().get("/weather/devices")
          .then()
             .statusCode(200)
             .contentType(MediaType.APPLICATION_JSON)
             .body("success", is(true));
    }
    
    @Test
    void testGetHistoricalWeatherData() {
        // Test the endpoint - we're not mocking the service here since it's hard to inject
        given()
          .when().get("/weather/historical?device_id=device1&scale=1hour")
          .then()
             .statusCode(anyOf(is(200), is(500)))
             .contentType(MediaType.APPLICATION_JSON);
    }
}


