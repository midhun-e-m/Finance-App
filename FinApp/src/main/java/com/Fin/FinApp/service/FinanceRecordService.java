package com.Fin.FinApp.service;

import com.Fin.FinApp.dto.DashboardSummaryDTO;
import com.Fin.FinApp.entity.FinanceRecord;
import com.Fin.FinApp.entity.TransactionType;
import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.repository.FinanceRecordRepository;
import com.Fin.FinApp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service // Tells Spring this is a business logic class
public class FinanceRecordService {

    // Inject the repositories so we can talk to the database
    private final FinanceRecordRepository recordRepository;
    private final UserRepository userRepository; // <-- ADDED THE USER REPOSITORY

    // Updated Constructor to include both repositories
    public FinanceRecordService(FinanceRecordRepository recordRepository, UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    // 1. Method to save a new record (UPDATED TO PREVENT DETACHED ENTITY CRASH)
    public FinanceRecord saveRecord(FinanceRecord record) {
        // Step A: Grab the ID that React sent over
        UUID userId = record.getUser().getId();

        // Step B: Fetch the REAL, active user from the database
        User realUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step C: Attach the real, active user to the record
        record.setUser(realUser);

        // Step D: Save safely!
        return recordRepository.save(record);
    }

    // 2. Method to get all records for a user
    // 2. Method to get all records for a user (UPGRADED WITH FILTERING!)
    public List<FinanceRecord> getAllRecordsForUser(UUID userId, String type, String category) {
        // 1. Get all records from the database
        List<FinanceRecord> records = recordRepository.findByUserId(userId);

        // 2. If the user sent a 'type' filter (e.g., "INCOME"), filter the list
        if (type != null && !type.isEmpty()) {
            records = records.stream()
                    .filter(record -> record.getType().name().equalsIgnoreCase(type))
                    .toList();
        }

        // 3. If the user sent a 'category' filter, filter the list
        if (category != null && !category.isEmpty()) {
            records = records.stream()
                    .filter(record -> record.getCategory().toLowerCase().contains(category.toLowerCase()))
                    .toList();
        }

        return records;
    }

    // 3. Method to calculate the dashboard summary!
    public DashboardSummaryDTO getSummaryForUser(UUID userId) {
        // Fetch all records for this user from the database
        List<FinanceRecord> records = recordRepository.findByUserId(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        // Loop through the records and add up the amounts
        for (FinanceRecord record : records) {
            if (record.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(record.getAmount());
            } else if (record.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(record.getAmount());
            }
        }

        // Package the results into our DTO
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetBalance(totalIncome.subtract(totalExpense)); // Income - Expense

        return summary;
    }
    // 4. UPDATE an existing record
    public FinanceRecord updateRecord(UUID id, FinanceRecord updatedRecord) {
        // Find the old record first
        FinanceRecord existing = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // Overwrite it with the new data
        existing.setAmount(updatedRecord.getAmount());
        existing.setType(updatedRecord.getType());
        existing.setCategory(updatedRecord.getCategory());
        existing.setDate(updatedRecord.getDate());
        existing.setNotes(updatedRecord.getNotes());

        // Save the updated version
        return recordRepository.save(existing);
    }

    // 5. DELETE a record
    public void deleteRecord(UUID id) {
        recordRepository.deleteById(id);
    }
}