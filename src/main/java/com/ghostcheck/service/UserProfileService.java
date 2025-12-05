package com.ghostcheck.service;

import com.ghostcheck.entity.UserProfile;
import com.ghostcheck.repository.UserProfileRepository;
import com.ghostcheck.service.dto.UserProfileDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfile createProfile(UserProfileDto dto) {
        Objects.requireNonNull(dto, "UserProfileDto must not be null");
        if (dto.email() == null || dto.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalizedEmail = dto.email().trim().toLowerCase();
        return userProfileRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> persistNewProfile(dto.fullName(), normalizedEmail));
    }

    private UserProfile persistNewProfile(String fullName, String email) {
        UserProfile profile = UserProfile.builder()
            .fullName(fullName)
            .email(email)
            .createdAt(Instant.now())
            .riskScore(0)
            .build();
        try {
            return userProfileRepository.save(profile);
        } catch (DataIntegrityViolationException ex) {
            return userProfileRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public UserProfile getProfile(UUID id) {
        return userProfileRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("UserProfile not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserProfile> listProfiles() {
        return userProfileRepository.findAll();
    }

    @Transactional
    public UserProfile updateRiskScore(UUID id, int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }
        UserProfile profile = getProfile(id);
        profile.setRiskScore(score);
        return userProfileRepository.save(profile);
    }
}
