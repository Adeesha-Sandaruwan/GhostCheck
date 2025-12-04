package com.ghostcheck.entity;
}
    // ...existing code...

    private List<BreachRecord> breachRecords;
    @OneToMany(mappedBy = "scanRecord", cascade = CascadeType.ALL, orphanRemoval = true)

    private Integer riskScore;
    @Column

    private String rawData;
    @Column(columnDefinition = "TEXT")
    @Lob

    private Integer dataSourcesChecked;
    @Column(nullable = false)

    private Instant scanDate;
    @Column(nullable = false)

    private UserProfile userProfile;
    @JoinColumn(name = "user_profile_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    private UUID id;
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id

public class ScanRecord {
@ToString(exclude = {"userProfile", "breachRecords"})
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "scan_records")
@Entity

import java.util.UUID;
import java.util.List;
import java.time.Instant;
import lombok.*;
import jakarta.persistence.*;


