package com.ghostcheck.service.osint;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Offline breach client that loads a small breach database from the classpath (breach-database.txt).
 * Provides simple lookup and a deterministic small exposure score for emails not in the database.
 */
@Service
public class LocalBreachClient {

    private final Map<String, String> emailSeverityMap = new HashMap<>();

    public LocalBreachClient() {
        loadFromClasspath();
    }

    private void loadFromClasspath() {
        try {
            InputStream is = getClass().getResourceAsStream("/breach-database.txt");
            if (is == null) {
                System.err.println("[LocalBreachClient] breach-database.txt not found on classpath.");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> {
                    String trimmed = line == null ? "" : line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) return;
                    // Expected format: email,severity
                    String[] parts = trimmed.split(",");
                    if (parts.length >= 2) {
                        String email = parts[0].trim().toLowerCase();
                        String severity = parts[1].trim().toLowerCase();
                        if (!email.isEmpty()) {
                            emailSeverityMap.put(email, severity);
                        }
                    }
                });
            }
            System.out.println("[LocalBreachClient] Loaded severities for " + emailSeverityMap.size() + " emails from classpath.");
        } catch (Exception e) {
            System.err.println("[LocalBreachClient] Failed to load breach-database.txt");
            e.printStackTrace();
        }
    }

    public Map<String, String> getEmailSeverityMap() {
        return Collections.unmodifiableMap(emailSeverityMap);
    }

    public String getSeverity(String email) {
        if (email == null) return null;
        return emailSeverityMap.get(email.trim().toLowerCase());
    }

    public boolean isBreached(String email) {
        return getSeverity(email) != null;
    }

    public int getRiskScore(String email) {
        String severity = getSeverity(email);
        if (severity == null) return 0;
        return switch (severity) {
            case "low" -> 10;
            case "medium" -> 30;
            case "high" -> 70;
            case "critical" -> 100;
            default -> 0;
        };
    }
}
