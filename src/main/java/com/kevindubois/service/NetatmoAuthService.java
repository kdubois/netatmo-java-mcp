package com.kevindubois.service;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

import java.time.Instant;
import java.util.logging.Logger;

@ApplicationScoped
public class NetatmoAuthService {

    private static final Logger logger = Logger.getLogger(NetatmoAuthService.class.getName());

    @ConfigProperty(name = "netatmo.api.base.url")
    String baseUrl;

    @ConfigProperty(name = "netatmo.api.client-id")
    String clientId;

    @ConfigProperty(name = "netatmo.api.client-secret")
    String clientSecret;

    @ConfigProperty(name = "netatmo.api.refresh-token")
    String refreshToken;

    private volatile String accessToken;
    private volatile Instant tokenExpiry;

    public String getAccessToken() {
        if (accessToken == null || isTokenExpired()) {
            logger.info("Access token is null or expired, refreshing...");
            refreshAccessToken();
        } else {
            logger.info("Using existing access token");
        }
        return accessToken;
    }

    private boolean isTokenExpired() {
        return tokenExpiry == null || Instant.now().isAfter(tokenExpiry.minusSeconds(60)); // Refresh 1 minute before expiry
    }

    private synchronized void refreshAccessToken() {
        Client client = null;
        try {
            logger.info("Refreshing Netatmo access token");
            
            client = ClientBuilder.newClient();
            
            Form form = new Form()
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken)
                .param("client_id", clientId)
                .param("client_secret", clientSecret);
            
            Response response = client.target(baseUrl + "/oauth2/token")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
            
            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                TokenResponse tokenResponse = mapper.readValue(jsonResponse, TokenResponse.class);
                
                this.accessToken = tokenResponse.getAccessToken();
                this.tokenExpiry = Instant.now().plusSeconds(tokenResponse.getExpiresIn() - 60); // Subtract 60 seconds for safety
                
                logger.info("Successfully refreshed Netatmo access token, expires in " + tokenResponse.getExpiresIn() + " seconds");
            } else {
                String errorBody = response.readEntity(String.class);
                logger.severe("Failed to refresh token, HTTP status: " + response.getStatus() + ", body: " + errorBody);
                throw new RuntimeException("Token refresh failed with status: " + response.getStatus());
            }
            
        } catch (Exception e) {
            logger.severe("Failed to refresh Netatmo access token: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to refresh access token", e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @RegisterForReflection
    public static class TokenResponse {
        @JsonProperty("access_token")
        private final String accessToken;
        
        @JsonProperty("expires_in")
        private final int expiresIn;
        
        @JsonProperty("refresh_token")
        private final String refreshToken;
        
        @JsonProperty("scope")
        private final List<String> scope;
        
        /**
         * Default constructor for Jackson
         */
        public TokenResponse() {
            this(null, 0, null, null);
        }
        
        /**
         * Constructor for Jackson deserialization
         */
        @JsonCreator
        public TokenResponse(
                @JsonProperty("access_token") String accessToken,
                @JsonProperty("expires_in") int expiresIn,
                @JsonProperty("refresh_token") String refreshToken,
                @JsonProperty("scope") List<String> scope) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.refreshToken = refreshToken;
            this.scope = scope;
        }
        
        // Getters
        @JsonProperty("access_token")
        public String getAccessToken() {
            return accessToken;
        }
        
        @JsonProperty("expires_in")
        public int getExpiresIn() {
            return expiresIn;
        }
        
        @JsonProperty("refresh_token")
        public String getRefreshToken() {
            return refreshToken;
        }
        
        @JsonProperty("scope")
        public List<String> getScope() {
            return scope;
        }
    }

}