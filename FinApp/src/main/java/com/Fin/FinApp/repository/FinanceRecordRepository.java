package com.Fin.FinApp.repository;

import com.Fin.FinApp.entity.FinanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, UUID> {

    // Automatically finds all financial records belonging to a specific user
    List<FinanceRecord> findByUserId(UUID userId);
}