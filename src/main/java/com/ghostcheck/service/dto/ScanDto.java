package com.ghostcheck.service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScanDto(
    UUID id,
    UUID userProfileId,
    Instant scanDate,
    int dataSourcesChecked,
    int riskScore,
    List<BreachDto> breaches
) {}

