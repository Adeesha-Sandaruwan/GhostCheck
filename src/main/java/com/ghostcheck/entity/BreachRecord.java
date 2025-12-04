package com.ghostcheck.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "breach_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"scanRecord"})
public class BreachRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_record_id", nullable = false)
    private ScanRecord scanRecord;

    @Column(nullable = false)
    private String sourceName;

    @Column(nullable = false)
    private Instant breachDate;

    @Column(length = 2048)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String exposedData;

    // ...existing code...
}

