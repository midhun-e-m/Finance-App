package com.Fin.FinApp.service;

import com.Fin.FinApp.dto.DashboardSummaryDTO;
import com.Fin.FinApp.entity.FinanceRecord;
import com.Fin.FinApp.entity.TransactionType;
import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.repository.FinanceRecordRepository;
import com.Fin.FinApp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class FinanceRecordService {

    // Inject the repositories so the Brain can talk to the Database
    private final FinanceRecordRepository recordRepository;
    private final UserRepository userRepository;

    public FinanceRecordService(FinanceRecordRepository recordRepository, UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    // --- CREATE ---
    public FinanceRecord saveRecord(FinanceRecord record) {
        // SECURITY CHECK: We don't trust the frontend to send a whole user object.
        // We just take the ID they sent, and look up the REAL, active user in our database.
        UUID userId = record.getUser().getId();
        User realUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Attach the real user to the record and save it safely.
        record.setUser(realUser);
        return recordRepository.save(record);
    }

    // --- READ (Personal Data with Pagination) ---
    public Page<FinanceRecord> getAllRecordsForUser(UUID userId, String typeString, String category, int page, int size) {
        // Build the "Page" request, sorting newest dates to the top
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        // Convert the string "INCOME" into the actual Enum so the database understands it
        TransactionType type = (typeString != null && !typeString.isEmpty()) ? TransactionType.valueOf(typeString.toUpperCase()) : null;
        return recordRepository.findWithFilters(userId, type, category, pageable);
    }

    // --- READ (Company Wide Data with Pagination) ---
    public Page<FinanceRecord> getAllCompanyRecords(String typeString, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        TransactionType type = (typeString != null && !typeString.isEmpty()) ? TransactionType.valueOf(typeString.toUpperCase()) : null;

        // Notice we pass 'null' for the userId here, which tells the database to grab everyone's data!
        return recordRepository.findWithFilters(null, type, category, pageable);
    }

    // --- MATH: Personal Summary (OPTIMIZED) ---
    public DashboardSummaryDTO getSummaryForUser(UUID userId) {
        // OPTIMIZATION: Instead of loading every record into memory, we ask PostgreSQL to do the math instantly!
        BigDecimal totalIncome = recordRepository.sumIncome(userId);
        BigDecimal totalExpense = recordRepository.sumExpense(userId);

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetBalance(totalIncome.subtract(totalExpense));

        return summary;
    }

    // --- UPDATE ---
    public FinanceRecord updateRecord(UUID id, FinanceRecord updatedRecord) {
        // Find the old record first to make sure it exists
        FinanceRecord existing = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // Carefully overwrite only the specific fields (never change the ID or the User!)
        existing.setAmount(updatedRecord.getAmount());
        existing.setType(updatedRecord.getType());
        existing.setCategory(updatedRecord.getCategory());
        existing.setDate(updatedRecord.getDate());
        existing.setNotes(updatedRecord.getNotes());

        return recordRepository.save(existing);
    }

    // --- DELETE ---
    public void deleteRecord(UUID id) {
        recordRepository.deleteById(id);
    }

    // --- MATH: Company Summary (OPTIMIZED) ---
    public DashboardSummaryDTO getCompanySummary() {
        // OPTIMIZATION: Pass null to our new database math functions to sum up the entire company instantly!
        BigDecimal totalIncome = recordRepository.sumIncome(null);
        BigDecimal totalExpense = recordRepository.sumExpense(null);

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetBalance(totalIncome.subtract(totalExpense));

        return summary;
    }
}