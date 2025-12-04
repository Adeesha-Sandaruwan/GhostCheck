package com.ghostcheck.service;

import com.ghostcheck.service.dto.UserProfileDto;
import com.ghostcheck.entity.UserProfile;
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

class UserProfileServiceTest {

    private UserProfileRepository repo;
    private UserProfileService service;

    @BeforeEach
    void setup() {
        repo = mock(UserProfileRepository.class);
        service = new UserProfileService(repo);
    }

    @Test
    void createProfile_savesAndReturnsProfile() {
        UserProfileDto dto = new UserProfileDto("Alice Doe", "alice@example.com");
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);

        when(repo.save(any(UserProfile.class))).thenAnswer(inv -> {
            UserProfile p = inv.getArgument(0);
            // simulate DB assigned ID and timestamps if needed
            if (p.getId() == null) p.setId(UUID.randomUUID());
            if (p.getCreatedAt() == null) p.setCreatedAt(Instant.now());
            return p;
        });

        UserProfile created = service.createProfile(dto);

        verify(repo).save(captor.capture());
        UserProfile saved = captor.getValue();
        assertEquals("Alice Doe", saved.getFullName());
        assertEquals("alice@example.com", saved.getEmail());

        assertNotNull(created.getId());
        assertEquals("Alice Doe", created.getFullName());
        assertEquals("alice@example.com", created.getEmail());
    }

    @Test
    void getProfile_returnsProfileWhenFound() {
        UUID id = UUID.randomUUID();
        UserProfile profile = UserProfile.builder()
                .id(id)
                .fullName("Bob Smith")
                .email("bob@example.com")
                .riskScore(10)
                .createdAt(Instant.now())
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(profile));

        UserProfile found = service.getProfile(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("Bob Smith", found.getFullName());
    }

    @Test
    void getProfile_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getProfile(id));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void listProfiles_returnsAll() {
        UserProfile p1 = UserProfile.builder().id(UUID.randomUUID()).fullName("A").email("a@example.com").createdAt(Instant.now()).build();
        UserProfile p2 = UserProfile.builder().id(UUID.randomUUID()).fullName("B").email("b@example.com").createdAt(Instant.now()).build();
        when(repo.findAll()).thenReturn(List.of(p1, p2));

        List<UserProfile> all = service.listProfiles();

        assertEquals(2, all.size());
        assertEquals("A", all.get(0).getFullName());
        assertEquals("B", all.get(1).getFullName());
    }

    @Test
    void updateRiskScore_updatesAndSaves() {
        UUID id = UUID.randomUUID();
        UserProfile profile = UserProfile.builder()
                .id(id)
                .fullName("Carol")
                .email("carol@example.com")
                .riskScore(5)
                .createdAt(Instant.now())
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(profile));
        when(repo.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateRiskScore(id, 42);

        assertEquals(42, profile.getRiskScore());
        verify(repo).save(profile);
    }
}
