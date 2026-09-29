package com.flowforge.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Settings under the "app." prefix in application.properties. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors) {

    /**
     * @param secret            HMAC signing key, at least 32 characters (from JWT_SECRET)
     * @param expirationMinutes token lifetime
     */
    public record Jwt(String secret, long expirationMinutes) {
    }

    /** Exact origins allowed to call the API from a browser. Never "*" in production. */
    public record Cors(List<String> allowedOrigins) {
    }
}
