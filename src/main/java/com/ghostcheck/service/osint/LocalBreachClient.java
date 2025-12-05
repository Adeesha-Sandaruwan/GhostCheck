package com.ghostcheck.service.osint;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Offline breach client that loads a small breach database from the classpath (breach-database.txt).
 * Provides simple lookup and a deterministic small exposure score for emails not in the database.
 */
@Service
public class LocalBreachClient {

    private final Set<String> breachedEmails = new HashSet<>();

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
                    // Accept formats like: email:password OR email|... ; we only need the email (first token before ':' or '|')
                    String emailToken = trimmed.split("[:|]", 2)[0].trim().toLowerCase();
                    if (!emailToken.isEmpty()) {
                        breachedEmails.add(emailToken);
                    }
                });
            }
            System.out.println("[LocalBreachClient] Loaded " + breachedEmails.size() + " breached emails from classpath.");
        } catch (Exception e) {
            System.err.println("[LocalBreachClient] Failed to load breach-database.txt");
            e.printStackTrace();
        }
    }

    /**
     * Returns true if the email exists in the offline breach database.
     */
    public boolean isBreached(String email) {
        if (email == null) return false;
        String key = email.trim().toLowerCase();
        return !key.isEmpty() && breachedEmails.contains(key);
    }

    /**
     * Deterministic small exposure score between 5 and 20 for emails not found in the database.
     * Based on a hash of the email to keep results stable across runs.
     */
    public int randomExposureScore(String email) {
        if (email == null || email.isBlank()) return 5;
        int base = Math.abs(email.trim().toLowerCase().hashCode());
        // Bias slightly for common risky patterns
        int bias = 0;
        String e = email.toLowerCase();
        if (e.contains("test") || e.contains("admin") || e.matches(".*\\d{3,}.*")) {
            bias += 3;
        }
        // Deterministic pseudo-random between 5 and 20
        int val = 5 + (base % 16); // 5..20
        val = Math.min(20, Math.max(5, val + bias));
        return val;
    }
}
