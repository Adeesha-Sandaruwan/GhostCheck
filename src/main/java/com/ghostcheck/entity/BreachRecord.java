package com.ghostcheck.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
@ToString(exclude = "scanRecord")
public class BreachRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_record_id", nullable = false)
    private ScanRecord scanRecord;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Lob
    @Column(name = "exposed_data", nullable = false)
    private String exposedData;

    @Column(name = "breach_date", nullable = false)
    private Instant breachDate;

    @Column(name = "added_date", nullable = false)
    private Instant addedDate;

    @Column(name = "pwn_count")
    private Long pwnCount;

    @Column(name = "description")
    private String description;
}
