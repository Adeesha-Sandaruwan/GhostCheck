package com.ghostcheck.service.osint;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

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
}

