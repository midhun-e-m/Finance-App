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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, UUID> {

    // Handles filtering and pagination at the database level
    @Query("SELECT f FROM FinanceRecord f WHERE " +
            "(:userId IS NULL OR f.user.id = :userId) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(f.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
            "(cast(:startDate as date) IS NULL OR f.date >= :startDate) AND " + // NEW
            "(cast(:endDate as date) IS NULL OR f.date <= :endDate)")           // NEW
    Page<FinanceRecord> findWithFilters(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate, // NEW
            @Param("endDate") LocalDate endDate,     // NEW
            Pageable pageable);

    // Database Math Optimization queries
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceRecord f WHERE f.type = 'INCOME' AND (:userId IS NULL OR f.user.id = :userId)")
    BigDecimal sumIncome(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceRecord f WHERE f.type = 'EXPENSE' AND (:userId IS NULL OR f.user.id = :userId)")
    BigDecimal sumExpense(@Param("userId") UUID userId);

    // Requirement 3: Category-wise totals
    @Query("SELECT f.category AS category, SUM(f.amount) AS total FROM FinanceRecord f WHERE (:userId IS NULL OR f.user.id = :userId) GROUP BY f.category")
    List<Map<String, Object>> getCategoryTotals(@Param("userId") UUID userId);

    // Requirement 3: Monthly Trends
    @Query("SELECT FUNCTION('TO_CHAR', f.date, 'YYYY-MM') AS month, f.type AS type, SUM(f.amount) AS total FROM FinanceRecord f WHERE (:userId IS NULL OR f.user.id = :userId) GROUP BY FUNCTION('TO_CHAR', f.date, 'YYYY-MM'), f.type ORDER BY month")
    List<Map<String, Object>> getMonthlyTrends(@Param("userId") UUID userId);
}