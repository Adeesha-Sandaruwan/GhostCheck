package com.ghostcheck.service;

import com.ghostcheck.entity.BreachRecord;
import com.ghostcheck.entity.ScanRecord;
import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.repository.BreachRecordRepository;
import com.ghostcheck.repository.ScanRecordRepository;
import com.ghostcheck.repository.UserProfileRepository;
import com.ghostcheck.service.osint.HIBPClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScanService {

    private final UserProfileRepository userProfileRepository;
    private final ScanRecordRepository scanRecordRepository;
    private final BreachRecordRepository breachRecordRepository;
    private final HIBPClient hibpClient;

    public ScanService(UserProfileRepository userProfileRepository,
                       ScanRecordRepository scanRecordRepository,
                       BreachRecordRepository breachRecordRepository,
                       HIBPClient hibpClient) {
        this.userProfileRepository = userProfileRepository;
        this.scanRecordRepository = scanRecordRepository;
        this.breachRecordRepository = breachRecordRepository;
        this.hibpClient = hibpClient;
    }

    @Transactional
    public ScanRecord performScan(UUID userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("UserProfile not found: " + userId));

        List<BreachRecord> breaches = hibpClient.breachesForEmail(user.getEmail());
        int riskScore = calculateRiskScore(breaches);

        ScanRecord scan = ScanRecord.builder()
                .userProfile(user)
                .scanDate(Instant.now())
                .dataSourcesChecked(1)
                .rawData(buildRawDataSummary(breaches))
                .riskScore(riskScore)
                .build();

        ScanRecord persisted = scanRecordRepository.save(scan);

        if (!breaches.isEmpty()) {
            breaches.forEach(b -> b.setScanRecord(persisted));
            breachRecordRepository.saveAll(breaches);
            persisted.setBreachRecords(breaches);
        }

        user.setRiskScore(riskScore);
        userProfileRepository.save(user);

        return persisted;
    }

    @Transactional(readOnly = true)
    public ScanRecord getScanRecord(UUID scanId) {
        return scanRecordRepository.findById(scanId)
                .orElseThrow(() -> new NoSuchElementException("ScanRecord not found: " + scanId));
    }

    /**
     * Fallback scan that does not persist anything. Useful when the database is unavailable.
     */
    public ScanRecord performScanWithoutPersistence(String fullName, String email) {
        UserProfile user = UserProfile.builder()
                .id(null)
                .fullName(fullName)
                .email(email)
                .createdAt(Instant.now())
                .riskScore(0)
                .build();

        List<BreachRecord> breaches = hibpClient.breachesForEmail(email);
        int riskScore = calculateRiskScore(breaches);

        ScanRecord scan = ScanRecord.builder()
                .id(null)
                .userProfile(user)
                .scanDate(Instant.now())
                .dataSourcesChecked(1)
                .rawData(buildRawDataSummary(breaches))
                .riskScore(riskScore)
                .build();

        if (!breaches.isEmpty()) {
            breaches.forEach(b -> b.setScanRecord(scan));
            scan.setBreachRecords(breaches);
        } else {
            scan.setBreachRecords(java.util.Collections.emptyList());
        }
        user.setRiskScore(riskScore);
        return scan;
    }

    public int calculateRiskScore(List<BreachRecord> breaches) {
        if (breaches == null || breaches.isEmpty()) return 0;

        int count = breaches.size();
        int countComponent = Math.min(15 * count, 60);

        int severityComponent = breaches.stream()
                .mapToInt(b -> severityFromExposedData(b.getExposedData()) + pwnSeverity(b.getPwnCount()))
                .sum();
        severityComponent = Math.min(severityComponent, 40);

        int score = countComponent + severityComponent;
        return Math.max(0, Math.min(score, 100));
    }

    private int severityFromExposedData(String json) {
        if (json == null || json.isEmpty()) return 0;
        int score = 0;
        String lower = json.toLowerCase();
        // Heuristics for common breach data classes
        if (lower.contains("password") || lower.contains("password_hash")) score += 25;
        if (lower.contains("email")) score += 5;
        if (lower.contains("ip")) score += 5;
        if (lower.contains("phone")) score += 10;
        if (lower.contains("address")) score += 5;
        if (lower.contains("credit card") || lower.contains("card")) score += 25;
        // Cap to avoid runaway
        return Math.min(score, 40);
    }

    private int pwnSeverity(Long pwnCount) {
        if (pwnCount == null || pwnCount <= 0) return 0;
        if (pwnCount >= 50_000_000L) return 15;
        if (pwnCount >= 1_000_000L) return 10;
        if (pwnCount >= 10_000L) return 5;
        return 2;
    }

    private String buildRawDataSummary(List<BreachRecord> breaches) {
        List<BreachRecord> safeList = breaches == null ? Collections.emptyList() : breaches;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("breachCount", safeList.size());
        payload.put("breaches", safeList.stream()
                .map(b -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("source", b.getSourceName());
                    entry.put("breachDate", b.getBreachDate());
                    entry.put("addedDate", b.getAddedDate());
                    entry.put("pwnCount", b.getPwnCount());
                    entry.put("severity", severityFromExposedData(b.getExposedData()));
                    entry.put("description", b.getDescription());
                    return entry;
                })
                .collect(Collectors.toList()));
        return toJson(payload);
    }

    private String toJson(Map<String, ?> map) {
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