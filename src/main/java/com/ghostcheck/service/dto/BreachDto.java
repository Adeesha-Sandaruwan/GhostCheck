package com.ghostcheck.service.dto;

import java.time.Instant;
import java.util.UUID;

public record BreachDto(
    UUID id,
    String sourceName,
    Instant breachDate,
    String description,
    String exposedData
) {}
