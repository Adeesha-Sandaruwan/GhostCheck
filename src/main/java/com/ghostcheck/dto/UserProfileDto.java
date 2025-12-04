package com.ghostcheck.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String name;
    private String email;

    // Optionally add mapper helpers to/from entity if needed:
    // public static UserProfileDto fromEntity(UserProfile entity) { ...existing code... }
}

