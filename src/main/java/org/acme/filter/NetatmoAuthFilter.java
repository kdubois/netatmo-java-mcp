package org.acme.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

import org.acme.service.NetatmoAuthService;

import java.util.logging.Logger;

@Provider
public class NetatmoAuthFilter implements ClientRequestFilter {

    private static final Logger logger = Logger.getLogger(NetatmoAuthFilter.class.getName());

    @Inject
    NetatmoAuthService authService;

    @Override
    public void filter(ClientRequestContext requestContext) {
        try {
            String accessToken = authService.getAccessToken();
            requestContext.getHeaders().add("Authorization", "Bearer " + accessToken);
            logger.fine("Added authorization header to Netatmo API request");
        } catch (Exception e) {
            logger.severe("Failed to add authorization header: " + e.getMessage());
            throw new RuntimeException("Failed to authenticate with Netatmo API", e);
        }
    }
}