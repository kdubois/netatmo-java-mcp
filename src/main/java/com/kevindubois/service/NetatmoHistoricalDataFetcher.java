package com.kevindubois.service;

import com.kevindubois.client.NetatmoApiClient;
import com.kevindubois.dto.NetatmoHistoricalDataResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.time.temporal.ChronoUnit;

/**
 * Thin resiliency wrapper around the Netatmo historical-data REST call. Netatmo is flaky and
 * returns transient HTTP errors (most often 503, sometimes 403/500 during token refresh); a
 * small bounded retry usually clears them, so callers get data instead of a degraded result.
 * The retry is declared with SmallRye Fault Tolerance rather than a hand-rolled loop: it is
 * applied through the CDI proxy, and {@link ClientWebApplicationException} is the exception the
 * MP REST client throws for any non-2xx response, so the annotation retries the whole HTTP
 * call cleanly.
 */
@ApplicationScoped
public class NetatmoHistoricalDataFetcher {

    @Inject
    @RestClient
    NetatmoApiClient netatmoApiClient;

    @Retry(
        retryOn = ClientWebApplicationException.class,
        maxRetries = 2,
        delay = 500,
        delayUnit = ChronoUnit.MILLIS
    )
    public NetatmoHistoricalDataResponse getHistoricalData(
            String deviceId, String moduleId, String scale, String type,
            Long dateBegin, Long dateEnd, Integer limit, Boolean optimize, Boolean realTime) {
        return netatmoApiClient.getHistoricalData(
            deviceId, moduleId, scale, type, dateBegin, dateEnd, limit, optimize, realTime
        );
    }
}
