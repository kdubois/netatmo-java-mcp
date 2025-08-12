package org.acme.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.acme.dto.NetatmoStationsDataResponse;
import org.acme.dto.NetatmoHistoricalDataResponse;
import org.acme.filter.NetatmoAuthFilter;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "netatmo-api")
@RegisterProvider(NetatmoAuthFilter.class)
public interface NetatmoApiClient {

    @GET
    @Path("/getstationsdata")
    @Produces(MediaType.APPLICATION_JSON)
    NetatmoStationsDataResponse getStationsData(@QueryParam("device_id") String deviceId);

    @GET
    @Path("/getstationsdata")
    @Produces(MediaType.APPLICATION_JSON)
    NetatmoStationsDataResponse getStationsData();

    @GET
    @Path("/getmeasure")
    @Produces(MediaType.APPLICATION_JSON)
    NetatmoHistoricalDataResponse getHistoricalData(
        @QueryParam("device_id") String deviceId,
        @QueryParam("module_id") String moduleId,
        @QueryParam("scale") String scale,
        @QueryParam("type") String type,
        @QueryParam("date_begin") Long dateBegin,
        @QueryParam("date_end") Long dateEnd,
        @QueryParam("limit") Integer limit,
        @QueryParam("optimize") Boolean optimize,
        @QueryParam("real_time") Boolean realTime
    );
}