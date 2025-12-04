package com.ghostcheck.repository;

import com.ghostcheck.entity.BreachRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BreachRecordRepository extends JpaRepository<BreachRecord, UUID> {
    // ...existing code...
}

