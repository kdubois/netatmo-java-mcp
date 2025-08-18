package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.service.WeatherService;
import org.acme.dto.ApiResponse;
import org.acme.exception.WeatherApiException;

import java.util.logging.Logger;

@Path("/weather")
public class WeatherStationResource {

    private static final Logger logger = Logger.getLogger(WeatherStationResource.class.getName());

    @Inject
    WeatherService weatherService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Netatmo Weather Station API - Ready to serve weather data!";
    }

    @GET
    @Path("/stations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStationsData() {
        try {
            var data = weatherService.fetchAllStations();
            return ApiResponse.success(data, "Successfully retrieved all stations data").toResponse();
        } catch (WeatherApiException e) {
            return ApiResponse.error(e.getMessage(), e.getStatus()).toResponse();
        } catch (Exception e) {
            return ApiResponse.serverError("Error retrieving weather station data: " + e.getMessage()).toResponse();
        }
    }

    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentWeatherData() {
        try {
            var result = weatherService.getCurrentWeather();
            
            if (!result.isSuccess()) {
                return result.toResponse();
            }
            
            return ApiResponse.success(result.getData(), "Successfully retrieved current weather data").toResponse();
        } catch (Exception e) {
            return ApiResponse.serverError("Error retrieving current weather data: " + e.getMessage()).toResponse();
        }
    }

    @GET
    @Path("/historical")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistoricalWeatherData(
            @QueryParam("device_id") String deviceId,
            @QueryParam("module_id") String moduleId,
            @QueryParam("scale") String scale,
            @QueryParam("type") String sensorTypes,
            @QueryParam("date_begin") String dateBegin,
            @QueryParam("date_end") String dateEnd,
            @QueryParam("limit") Integer limit
    ) {
        try {
            var result = weatherService.getHistoricalWeather(deviceId, moduleId, scale, sensorTypes, dateBegin, dateEnd, limit);
            
            if (!result.isSuccess()) {
                return result.toResponse();
            }
            
            return ApiResponse.success(result.getData(), "Successfully retrieved historical weather data").toResponse();
        } catch (Exception e) {
            return ApiResponse.serverError("Error retrieving historical weather data: " + e.getMessage()).toResponse();
        }
    }

    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableDevices() {
        try {
            var result = weatherService.getAvailableDevices();
            
            if (!result.isSuccess()) {
                return result.toResponse();
            }
            
            return ApiResponse.success(result.getData(), "Successfully retrieved available devices").toResponse();
        } catch (Exception e) {
            return ApiResponse.serverError("Error retrieving available devices: " + e.getMessage()).toResponse();
        }
    }
}
