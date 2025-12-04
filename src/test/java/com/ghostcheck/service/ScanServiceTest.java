package com.ghostcheck.service;

import com.ghostcheck.entity.BreachRecord;
import com.ghostcheck.entity.ScanRecord;
import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.repository.BreachRecordRepository;
import com.ghostcheck.repository.ScanRecordRepository;
import com.ghostcheck.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScanServiceTest {

    private UserProfileRepository userProfileRepository;
    private ScanRecordRepository scanRecordRepository;
    private BreachRecordRepository breachRecordRepository;

    private ScanService scanService;

    @BeforeEach
    void setUp() {
        userProfileRepository = mock(UserProfileRepository.class);
        scanRecordRepository = mock(ScanRecordRepository.class);
        breachRecordRepository = mock(BreachRecordRepository.class);

        // Construct ScanService with mocked repos
        scanService = new ScanService(userProfileRepository, scanRecordRepository, breachRecordRepository);
    }

    @Test
    void performScan_persistsScanAndBreaches_updatesRiskScore() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.builder()
            .id(userId)
            .fullName("Jane Doe")
            .email("jane@example.com")
            .riskScore(0)
            .createdAt(Instant.now())
            .build();

        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(scanRecordRepository.save(any(ScanRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(breachRecordRepository.saveAll(anyList())).thenAnswer(inv -> (List<BreachRecord>) inv.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanRecord savedScan = scanService.performScan(userId);

        assertNotNull(savedScan.getId(), "ScanRecord should have an ID");
        assertEquals(profile, savedScan.getUserProfile(), "Scan should link to the user profile");
        assertTrue(savedScan.getRiskScore() >= 0 && savedScan.getRiskScore() <= 100, "Risk score must be normalized");
        assertNotNull(savedScan.getScanDate(), "Scan date should be set");
        assertNotNull(savedScan.getBreachRecords(), "Breaches should be generated");

        // Verify persistence calls
        verify(scanRecordRepository, times(1)).save(any(ScanRecord.class));
        verify(breachRecordRepository, times(1)).saveAll(anyList());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void calculateRiskScore_boundsBetween0And100() {
        List<BreachRecord> breaches = List.of(
            BreachRecord.builder().sourceName("Example").breachDate(Instant.now()).exposedData("email,password").build(),
            BreachRecord.builder().sourceName("Another").breachDate(Instant.now().minusSeconds(86400)).exposedData("email").build()
        );

        int score = scanService.calculateRiskScore(breaches);
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    void getScanRecord_returnsPersistedScan() {
        UUID scanId = UUID.randomUUID();
        ScanRecord scan = ScanRecord.builder().id(scanId).scanDate(Instant.now()).riskScore(10).build();
        when(scanRecordRepository.findById(scanId)).thenReturn(Optional.of(scan));

        ScanRecord fetched = scanService.getScanRecord(scanId);
        assertEquals(scanId, fetched.getId());
    }

    @Test
    void saveScanResults_persistsAll_andUpdatesProfileRisk() {
        UserProfile profile = UserProfile.builder().id(UUID.randomUUID()).email("x@y.com").fullName("X").riskScore(0).createdAt(Instant.now()).build();
        ScanRecord scan = ScanRecord.builder().id(UUID.randomUUID()).userProfile(profile).scanDate(Instant.now()).riskScore(55).dataSourcesChecked(10).build();
        List<BreachRecord> breaches = List.of(
            BreachRecord.builder().scanRecord(scan).sourceName("Src").breachDate(Instant.now()).exposedData("email,password").build()
        );

        when(scanRecordRepository.save(any(ScanRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(breachRecordRepository.saveAll(anyList())).thenAnswer(inv -> (List<BreachRecord>) inv.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ScanRecord persisted = scanService.saveScanResults(scan, breaches);

        assertEquals(55, persisted.getRiskScore());
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        assertEquals(55, captor.getValue().getRiskScore(), "Profile risk score should be updated to latest scan");
    }
}

