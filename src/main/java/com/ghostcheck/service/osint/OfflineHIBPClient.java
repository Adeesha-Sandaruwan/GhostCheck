package com.ghostcheck.service.osint;

import com.ghostcheck.entity.BreachRecord;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Offline HIBP-like client using an internal dataset. No network calls or API keys.
 */
@Service
@Primary
public class OfflineHIBPClient extends HIBPClient {

    public OfflineHIBPClient() {
        super(null, null);
    }

    private final Map<String, List<Map<String, Object>>> dataset = buildDataset();

    @Override
    public List<BreachRecord> breachesForEmail(String email) {
        if (email == null) return Collections.emptyList();
        List<Map<String, Object>> rows = dataset.getOrDefault(email.toLowerCase(Locale.ROOT), Collections.emptyList());
        if (rows.isEmpty()) return Collections.emptyList();

        List<BreachRecord> result = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            BreachRecord br = new BreachRecord();
            br.setSourceName((String) r.get("sourceName"));
            br.setExposedData((String) r.get("exposedData"));
            br.setBreachDate((Instant) r.get("breachDate"));
            br.setAddedDate((Instant) r.get("addedDate"));
            br.setPwnCount((Long) r.get("pwnCount"));
            result.add(br);
        }
        return result;
    }

    private Map<String, List<Map<String, Object>>> buildDataset() {
        Instant now = Instant.now();
        Map<String, List<Map<String, Object>>> map = new HashMap<>();

        map.put("test@example.com", List.of(
                entry("ExampleDump", "{\"password_hash\":\"$2b$10$abc...\",\"ip\":\"203.0.113.42\"}", now.minus(120, ChronoUnit.DAYS), now.minus(110, ChronoUnit.DAYS), 5_000L),
                entry("ExampleForum", "{\"email\":\"test@example.com\",\"phone\":\"+1-555-0100\"}", now.minus(400, ChronoUnit.DAYS), now.minus(395, ChronoUnit.DAYS), 120_000L)
        ));
        map.put("johndoe@gmail.com", List.of(
                entry("MegaShop", "{\"password_hash\":\"$argon2id$...\",\"ip\":\"198.51.100.77\"}", now.minus(800, ChronoUnit.DAYS), now.minus(790, ChronoUnit.DAYS), 2_500_000L),
                entry("SocialNet", "{\"email\":\"johndoe@gmail.com\",\"phone\":\"+1-555-0142\"}", now.minus(300, ChronoUnit.DAYS), now.minus(295, ChronoUnit.DAYS), 75_000_000L)
        ));
        map.put("alice@gmail.com", List.of(
                entry("PhotoShare", "{\"password_hash\":\"$pbkdf2-sha256$...\",\"ip\":\"192.0.2.55\"}", now.minus(600, ChronoUnit.DAYS), now.minus(590, ChronoUnit.DAYS), 50_000_000L)
        ));
        map.put("user@provider.com", List.of(
                entry("ServiceDB", "{\"email\":\"user@provider.com\",\"password\":\"password123\"}", now.minus(50, ChronoUnit.DAYS), now.minus(45, ChronoUnit.DAYS), 150_000L),
                entry("AnotherService", "{\"email\":\"user@provider.com\",\"phone\":\"+1-555-0199\"}", now.minus(200, ChronoUnit.DAYS), now.minus(190, ChronoUnit.DAYS), 2_000_000L)
        ));
        map.put("victim@company.com", List.of(
                entry("BadBank", "{\"ssn\":\"...\",\"credit card\":\"...\"}", now.minus(20, ChronoUnit.DAYS), now.minus(15, ChronoUnit.DAYS), 1000L),
                entry("LeakyCorp", "{\"api key\":\"...\"}", now.minus(90, ChronoUnit.DAYS), now.minus(85, ChronoUnit.DAYS), 500L),
                entry("OldBreach", "{\"username\":\"victim\"}", now.minus(1000, ChronoUnit.DAYS), now.minus(990, ChronoUnit.DAYS), 10000000L)
        ));

        return map;
    }

    private Map<String, Object> entry(String sourceName, String exposedData, Instant breachDate, Instant addedDate, Long pwnCount) {
        Map<String, Object> e = new HashMap<>();
        e.put("sourceName", sourceName);
        e.put("exposedData", exposedData);
        e.put("breachDate", breachDate);
        e.put("addedDate", addedDate);
        e.put("pwnCount", pwnCount);
        return e;
    }
}
