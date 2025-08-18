package com.kevindubois.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Configuration class for Netatmo Weather API
 */
@ApplicationScoped
public class WeatherApiConfig {

    @ConfigProperty(name = "netatmo.api.base.url")
    String baseUrl;

    @ConfigProperty(name = "netatmo.api.client-id")
    String clientId;

    @ConfigProperty(name = "netatmo.api.client-secret")
    String clientSecret;

    @ConfigProperty(name = "netatmo.api.refresh-token")
    String refreshToken;

    /**
     * Get the base URL for the Netatmo API
     * @return The base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get the client ID for the Netatmo API
     * @return The client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the client secret for the Netatmo API
     * @return The client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get the refresh token for the Netatmo API
     * @return The refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }
}
