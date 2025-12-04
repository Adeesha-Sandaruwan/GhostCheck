package com.ghostcheck.dto;

import com.ghostcheck.entity.ScanRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultDto {
    private int riskScore;
    private List<BreachDto> breaches;
    private Instant scanDate;

    public static ScanResultDto fromEntity(ScanRecord scan) {
        if (scan == null) return null;
        List<BreachDto> mappedBreaches = scan.getBreachRecords() == null
            ? List.of()
            : scan.getBreachRecords().stream()
                .map(BreachDto::fromEntity)
                .collect(Collectors.toList());
        return new ScanResultDto(
            scan.getRiskScore(),
            mappedBreaches,
            scan.getScanDate()
        );
    }
}

