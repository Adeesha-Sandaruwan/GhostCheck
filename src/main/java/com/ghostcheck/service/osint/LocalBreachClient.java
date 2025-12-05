package com.ghostcheck.service.osint;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

@Service
public class LocalBreachClient {

    private final Set<String> breachedEmails = new HashSet<>();

    public LocalBreachClient() {
        try (var reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/breach-database.txt")
                ))) {
            reader.lines().forEach(line -> {
                String email = line.split(":", 2)[0].trim().toLowerCase();
                breachedEmails.add(email);
            });
            System.out.println("Loaded " + breachedEmails.size() + " breached emails.");
        } catch (Exception e) {
            System.err.println("Failed to load breach-database.txt");
            e.printStackTrace();
        }
    }

    public boolean isBreached(String email) {
        if (email == null || email.isBlank()) return false;
        return breachedEmails.contains(email.toLowerCase().trim());
    }

    /**
     * Returns a small deterministic pseudo-random risk score between 5 and 20
     * for emails not present in the offline breach database.
     */
    public int randomExposureScore(String email) {
        if (email == null || email.isBlank()) return 5;
        int seed = email.toLowerCase().trim().hashCode();
        Random r = new Random(seed);
        return 5 + r.nextInt(16); // 5..20 inclusive
    }
}
