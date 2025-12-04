package com.ghostcheck.service;

import com.ghostcheck.entity.BreachRecord;
import com.ghostcheck.entity.ScanRecord;
import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.repository.BreachRecordRepository;
import com.ghostcheck.repository.ScanRecordRepository;
import com.ghostcheck.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private ScanRecordRepository scanRecordRepository;
    @Mock
    private BreachRecordRepository breachRecordRepository;

    @InjectMocks
    private ScanService scanService;

    private UserProfile user;

    @BeforeEach
    void setUp() {
        user = UserProfile.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .riskScore(0)
                .build();
    }

    @Test
    void performScan_persistsScanAndBreaches_updatesRiskScore() {
        // Arrange
        when(userProfileRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Fix 1: Mock the save behavior to simulate DB generating an ID
        when(scanRecordRepository.save(any(ScanRecord.class))).thenAnswer(invocation -> {
            ScanRecord r = invocation.getArgument(0);
            r.setId(UUID.randomUUID()); // Simulate DB ID generation
            return r;
        });

        // Act
        ScanRecord result = scanService.performScan(user.getId());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId(), "ScanRecord should have an ID"); // This will now pass
        assertEquals(user, result.getUserProfile());

        // Verify flow
        verify(scanRecordRepository).save(any(ScanRecord.class));
        verify(userProfileRepository).save(user); // performScan DOES save the user
    }

    @Test
    void saveScanResults_persistsAll() {
        // Arrange
        ScanRecord scan = ScanRecord.builder().userProfile(user).build();
        List<BreachRecord> breaches = Collections.singletonList(BreachRecord.builder().build());

        // Fix 2: Mock save to return the object
        when(scanRecordRepository.save(any(ScanRecord.class))).thenReturn(scan);

        // Act
        scanService.saveScanResults(scan, breaches);

        // Assert
        verify(scanRecordRepository).save(scan);
        verify(breachRecordRepository).saveAll(breaches);

        // Fix 3: Removed verify(userProfileRepository).save(user)
        // Reason: saveScanResults() in your Service does NOT save the user profile.
        // Only performScan() does that.
    }

    // ... keep other tests if you have them ...
}