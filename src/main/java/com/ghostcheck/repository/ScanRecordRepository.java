package com.ghostcheck.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ghostcheck.entity.ScanRecord;

public interface ScanRecordRepository extends JpaRepository<ScanRecord, UUID> {
    // ...existing code...
}
