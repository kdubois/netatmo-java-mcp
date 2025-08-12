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
import org.acme.util.HistoricalDataUtil;
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
            return ApiResponse.error(e.getMessage(), Response.Status.fromStatusCode(e.getErrorType().ordinal() + 400)).toResponse();
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving weather station data: " + e.getMessage(), 
                                    Response.Status.INTERNAL_SERVER_ERROR).toResponse();
        }
    }

    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentWeatherData() {
        try {
            var result = weatherService.getCurrentWeather();
            
            if (!result.success) {
                return ApiResponse.error(result.errorMessage, Response.Status.INTERNAL_SERVER_ERROR).toResponse();
            }
            
            return ApiResponse.success(result.data, "Successfully retrieved current weather data").toResponse();
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving current weather data: " + e.getMessage(), 
                                    Response.Status.INTERNAL_SERVER_ERROR).toResponse();
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
            @QueryParam("date_begin") Long dateBegin,
            @QueryParam("date_end") Long dateEnd,
            @QueryParam("limit") Integer limit
    ) {
        try {
            // Calculate days back if dateBegin and dateEnd are provided
            Integer daysBack = HistoricalDataUtil.calculateDaysBack(dateBegin, dateEnd);
            
            // If dateBegin and dateEnd are provided, use them directly instead of daysBack
            if (dateBegin != null && dateEnd != null) {
                // The WeatherService will handle the calculation of daysBack
                daysBack = null;
            }
            
            var result = weatherService.getHistoricalWeather(deviceId, moduleId, scale, sensorTypes, daysBack, limit);
            
            if (!result.success) {
                return ApiResponse.error(result.errorMessage, Response.Status.INTERNAL_SERVER_ERROR).toResponse();
            }
            
            return ApiResponse.success(result, "Successfully retrieved historical weather data").toResponse();
        } catch (WeatherApiException e) {
            return ApiResponse.error(e.getMessage(), Response.Status.fromStatusCode(e.getErrorType().ordinal() + 400)).toResponse();
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving historical weather data: " + e.getMessage(), 
                                    Response.Status.INTERNAL_SERVER_ERROR).toResponse();
        }
    }

    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableDevices() {
        try {
            var result = weatherService.getAvailableDevices();
            
            if (!result.success) {
                return ApiResponse.error(result.errorMessage, Response.Status.INTERNAL_SERVER_ERROR).toResponse();
            }
            
            return ApiResponse.success(result.devices, "Successfully retrieved available devices").toResponse();
        } catch (Exception e) {
            return ApiResponse.error("Error retrieving available devices: " + e.getMessage(), 
                                    Response.Status.INTERNAL_SERVER_ERROR).toResponse();
        }
    }
}
