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
import java.util.List;
import java.util.UUID;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, UUID> {

    // A simple query to grab every record for one specific person.
    // We use this mostly for the math to build the Summary Cards.
    List<FinanceRecord> findByUserId(UUID userId);

    // This custom query forces the Postgres database to do all the heavy lifting!
    // It checks if filters are provided. If they are NULL, it ignores them.
    // It also automatically slices the data into "Pages" so we don't crash the browser by sending 10,000 records at once.
    @Query("SELECT f FROM FinanceRecord f WHERE " +
            "(:userId IS NULL OR f.user.id = :userId) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(f.category) LIKE LOWER(CONCAT('%', :category, '%')))")
    Page<FinanceRecord> findWithFilters(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            Pageable pageable);

    // Let the database do the math instantly!
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceRecord f WHERE f.type = 'INCOME' AND (:userId IS NULL OR f.user.id = :userId)")
    BigDecimal sumIncome(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceRecord f WHERE f.type = 'EXPENSE' AND (:userId IS NULL OR f.user.id = :userId)")
    BigDecimal sumExpense(@Param("userId") UUID userId);
}