package com.ghostcheck.service;

import com.ghostcheck.entity.BreachRecord;
import com.ghostcheck.entity.ScanRecord;
import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.repository.BreachRecordRepository;
import com.ghostcheck.repository.ScanRecordRepository;
import com.ghostcheck.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ScanService {

    private final UserProfileRepository userProfileRepository;
    private final ScanRecordRepository scanRecordRepository;
    private final BreachRecordRepository breachRecordRepository;

    public ScanService(UserProfileRepository userProfileRepository,
                       ScanRecordRepository scanRecordRepository,
                       BreachRecordRepository breachRecordRepository) {
        this.userProfileRepository = userProfileRepository;
        this.scanRecordRepository = scanRecordRepository;
        this.breachRecordRepository = breachRecordRepository;
    }

    @Transactional
    public ScanRecord performScan(UUID userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("UserProfile not found: " + userId));

        List<BreachRecord> breaches = generateMockBreaches(user.getEmail());
        int riskScore = calculateRiskScore(breaches);

        ScanRecord scan = ScanRecord.builder()
                .userProfile(user)
                .scanDate(Instant.now())
                .dataSourcesChecked(3) // simulated sources checked
                .rawData(buildRawDataSummary(breaches))
                .riskScore(riskScore)
                .build();

        // Updated: This now returns the saved record
        saveScanResults(scan, breaches);

        // update user risk score snapshot
        user.setRiskScore(riskScore);
        userProfileRepository.save(user);

        return scan;
    }

    public int calculateRiskScore(List<BreachRecord> breaches) {
        if (breaches == null || breaches.isEmpty()) return 0;
        int base = Math.min(breaches.size() * 15, 70);
        int severity = breaches.stream()
                .mapToInt(b -> severityFromExposedData(b.getExposedData()))
                .sum();
        int score = base + Math.min(severity, 30);
        return Math.max(0, Math.min(score, 100));
    }

    public List<BreachRecord> generateMockBreaches(String email) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int count = rnd.nextInt(0, 4); // 0-3 breaches
        List<String> sources = Arrays.asList("GhostLeaks", "DarkSearch", "BreachWatch", "DataPwned");
        List<String> fields = Arrays.asList("email", "password_hash", "ip", "name", "phone");

        List<BreachRecord> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String source = sources.get(rnd.nextInt(sources.size()));
            Instant breachDate = Instant.now().minusSeconds(rnd.nextLong(7L * 24 * 3600, 5L * 365 * 24 * 3600));
            String exposed = buildExposedDataJson(email, fields, rnd);

            BreachRecord br = BreachRecord.builder()
                    .sourceName(source)
                    .breachDate(breachDate)
                    .description("Suspected exposure from " + source)
                    .exposedData(exposed)
                    .build();
            result.add(br);
        }
        return result;
    }

    // --- CHANGED METHOD: Returns ScanRecord instead of void ---
    @Transactional
    public ScanRecord saveScanResults(ScanRecord scanRecord, List<BreachRecord> breaches) {
        ScanRecord persisted = scanRecordRepository.save(scanRecord);
        if (breaches != null && !breaches.isEmpty()) {
            // attach FK and persist in batch
            breaches.forEach(b -> b.setScanRecord(persisted));
            breachRecordRepository.saveAll(breaches);
            persisted.setBreachRecords(breaches);
        }
        return persisted; // Returns the saved object
    }
    // ----------------------------------------------------------

    @Transactional(readOnly = true)
    public ScanRecord getScanRecord(UUID id) {
        return scanRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ScanRecord not found: " + id));
    }

    private String buildRawDataSummary(List<BreachRecord> breaches) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("breachCount", breaches.size());
        summary.put("sources", breaches.stream().map(BreachRecord::getSourceName).collect(Collectors.toSet()));
        return toJson(summary);
    }

    private int severityFromExposedData(String json) {
        if (json == null || json.isEmpty()) return 0;
        int score = 0;
        if (json.contains("password_hash")) score += 20;
        if (json.contains("phone")) score += 10;
        if (json.contains("ip")) score += 5;
        return score;
    }

    private String buildExposedDataJson(String email, List<String> fields, ThreadLocalRandom rnd) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("email", email);
        if (rnd.nextBoolean()) data.put("password_hash", "sha256:" + UUID.randomUUID());
        if (rnd.nextBoolean()) data.put("ip", "192.0.2." + rnd.nextInt(1, 254));
        if (rnd.nextBoolean()) data.put("name", "Redacted");
        if (rnd.nextBoolean()) data.put("phone", "+1-555-" + rnd.nextInt(1000, 9999));
        return toJson(data);
    }

    private String toJson(Map<String, ?> map) {
        // Minimal JSON serialization to avoid extra deps
        return map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":" + stringify(e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private String stringify(Object v) {
        if (v == null) return "null";
        if (v instanceof Number || v instanceof Boolean) return String.valueOf(v);
        if (v instanceof Collection<?> c) {
            return c.stream().map(this::stringify).collect(Collectors.joining(",", "[", "]"));
        }
        return "\"" + String.valueOf(v).replace("\"", "\\\"") + "\"";
    }
}