package com.ghostcheck.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scan_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"userProfile", "breachRecords"})
public class ScanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false)
    private Instant scanDate;

    @Column(nullable = false)
    private Integer dataSourcesChecked;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawData;

    @Column
    private Integer riskScore;

    @OneToMany(mappedBy = "scanRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreachRecord> breachRecords;
}
