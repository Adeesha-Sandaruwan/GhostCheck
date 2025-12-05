package com.ghostcheck.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ghostcheck.hibp")
public class HibpProperties {
    private String baseUrl = "https://haveibeenpwned.com/api/v3";
    private String apiKey; // optional, but required for real results
    private long rateLimitMs = 1600; // HIBP recommends ~1.6s between calls

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public long getRateLimitMs() { return rateLimitMs; }
    public void setRateLimitMs(long rateLimitMs) { this.rateLimitMs = rateLimitMs; }
}

