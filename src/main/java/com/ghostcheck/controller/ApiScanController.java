package com.ghostcheck.controller;

import com.ghostcheck.entity.BreachRecord;
import com.ghostcheck.entity.ScanRecord;
import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.service.ScanService;
import com.ghostcheck.service.UserProfileService;
import com.ghostcheck.service.dto.BreachDto;
import com.ghostcheck.service.dto.ScanDto;
import com.ghostcheck.service.dto.UserProfileDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiScanController {

    private final ScanService scanService;
    private final UserProfileService userProfileService;

    public ApiScanController(ScanService scanService, UserProfileService userProfileService) {
        this.scanService = scanService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanDto> triggerScan(@RequestBody UserProfileDto dto) {
        UserProfile profile = userProfileService.createProfile(dto);
        ScanRecord scan = scanService.performScan(profile.getId());
        return ResponseEntity.ok(toScanDto(scan));
    }

    @GetMapping("/scan/{id}")
    public ResponseEntity<ScanDto> getScan(@PathVariable UUID id) {
        ScanRecord scan = scanService.getScanRecord(id);
        return ResponseEntity.ok(toScanDto(scan));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable UUID id) {
        UserProfile profile = userProfileService.getProfile(id);
        return ResponseEntity.ok(new UserProfileDto(profile.getFullName(), profile.getEmail()));
    }

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> check(@RequestParam("email") String email) {
        int risk = scanService.getRiskScore(email);
        String severity = scanService.getSeverity(email);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("riskScore", risk);
        body.put("severity", severity);
        return ResponseEntity.ok(body);
    }

    private ScanDto toScanDto(ScanRecord scan) {
        List<BreachDto> breaches = scan.getBreachRecords().stream()
            .map(this::toBreachDto)
            .collect(Collectors.toList());
        return new ScanDto(
            scan.getId(),
            scan.getUserProfile().getId(),
            scan.getScanDate(),
            scan.getDataSourcesChecked(),
            scan.getRiskScore(),
            breaches
        );
    }

    private BreachDto toBreachDto(BreachRecord br) {
        return new BreachDto(
            br.getId(),
            br.getSourceName(),
            br.getBreachDate(),
            br.getDescription(),
            br.getExposedData()
        );
    }
}
