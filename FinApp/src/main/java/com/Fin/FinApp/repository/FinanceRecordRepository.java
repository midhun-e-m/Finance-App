package com.Fin.FinApp.repository;

import com.Fin.FinApp.entity.FinanceRecord;
import com.Fin.FinApp.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, UUID> {

    // Handles filtering and pagination at the database level
    @Query("SELECT f FROM FinanceRecord f WHERE " +
            "(:userId IS NULL OR f.user.id = :userId) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(f.category) LIKE LOWER(CONCAT('%', :category, '%')))")
    Page<FinanceRecord> findWithFilters(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            Pageable pageable);

    // Database Math Optimization queries
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceRecord f WHERE f.type = 'INCOME' AND (:userId IS NULL OR f.user.id = :userId)")
    BigDecimal sumIncome(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceRecord f WHERE f.type = 'EXPENSE' AND (:userId IS NULL OR f.user.id = :userId)")
    BigDecimal sumExpense(@Param("userId") UUID userId);
}