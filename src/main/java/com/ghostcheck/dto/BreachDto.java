package com.ghostcheck.dto;

import com.ghostcheck.entity.BreachRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreachDto {
    private UUID id;
    private String sourceName;
    private Instant breachDate;
    private String description;
    private String exposedData;

    public static BreachDto fromEntity(BreachRecord br) {
        if (br == null) return null;
        return new BreachDto(
            br.getId(),
            br.getSourceName(),
            br.getBreachDate(),
            br.getDescription(),
            br.getExposedData()
        );
    }
}

