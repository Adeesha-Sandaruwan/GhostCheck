package com.ghostcheck.util;

import com.ghostcheck.entity.BreachRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class RiskUtils {

    private RiskUtils() { }

    /**
     * Normalize risk score to 0–100 using simple heuristics:
     * - Base on breach count
     * - Weight recent breaches higher
     * - Weight by exposed data severity
     */
    public static int normalizeRiskScore(List<BreachRecord> breaches) {
        if (breaches == null || breaches.isEmpty()) return 0;

        double score = 0.0;
        Instant now = Instant.now();

        for (BreachRecord b : breaches) {
            if (b == null) continue;
            double base = 15.0; // base per breach
            double recencyWeight = 1.0;
            if (b.getBreachDate() != null) {
                long days = Math.max(1, Duration.between(b.getBreachDate(), now).toDays());
                // Recent breaches weigh more: <=30 days ~ 2.0x, <=180 days ~1.5x, otherwise ~1.0x
                recencyWeight = days <= 30 ? 2.0 : (days <= 180 ? 1.5 : 1.0);
            }
            double severityWeight = severityFromExposedData(b.getExposedData());
            score += base * recencyWeight * severityWeight;
        }

        // Normalize and cap between 0–100
        int normalized = (int) Math.round(Math.min(100.0, score));
        return Math.max(0, normalized);
    }

    public static String riskLabel(int score) {
        if (score <= 33) return "Low";
        if (score <= 66) return "Moderate";
        return "High";
    }

    // Returns a plain color name suitable for mapping to UI classes
    public static String riskColor(int score) {
        if (score <= 33) return "green";
        if (score <= 66) return "yellow";
        return "red";
    }

    private static double severityFromExposedData(String exposedData) {
        if (exposedData == null || exposedData.isBlank()) return 1.0;
        String data = exposedData.toLowerCase();

        // Assign weights based on sensitive terms present
        double weight = 1.0;
        if (containsAny(data, "password", "pwd", "hash")) weight += 0.8;
        if (containsAny(data, "email", "username", "handle")) weight += 0.4;
        if (containsAny(data, "phone", "address")) weight += 0.3;
        if (containsAny(data, "ssn", "credit card", "cc", "token", "api key")) weight += 1.0;

        return Math.min(3.0, weight); // cap severity contribution
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }
}

