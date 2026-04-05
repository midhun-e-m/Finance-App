package com.Fin.FinApp.controller;

import com.Fin.FinApp.dto.DashboardSummaryDTO;
import com.Fin.FinApp.entity.FinanceRecord;
import com.Fin.FinApp.service.FinanceRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
public class FinanceRecordController {

    private final FinanceRecordService recordService;

    // Injecting the service
    public FinanceRecordController(FinanceRecordService recordService) {
        this.recordService = recordService;
    }

    // 1. CREATE A RECORD
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FinanceRecord> createRecord(@RequestBody FinanceRecord record) {
        FinanceRecord savedRecord = recordService.saveRecord(record);
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    // 2. GET ALL RECORDS FOR A USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FinanceRecord>> getUserRecords(
            @PathVariable UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {

        List<FinanceRecord> records = recordService.getAllRecordsForUser(userId, type, category);
        return ResponseEntity.ok(records);
    }

    // 3. GET DASHBOARD SUMMARY FOR A USER
    // Example URL: GET http://localhost:8080/api/records/user/123e4567-e89b-12d3-a456-426614174000/summary
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(@PathVariable UUID userId) {
        DashboardSummaryDTO summary = recordService.getSummaryForUser(userId);
        return ResponseEntity.ok(summary);
    }

    // 4. UPDATE A RECORD (God Mode Only!)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FinanceRecord> updateRecord(@PathVariable UUID id, @RequestBody FinanceRecord record) {
        return ResponseEntity.ok(recordService.updateRecord(id, record));
    }

    // 5. DELETE A RECORD (God Mode Only!)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @GetMapping("/all")
    public ResponseEntity<List<FinanceRecord>> getAllCompanyRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {

        List<FinanceRecord> records = recordService.getAllCompanyRecords(type, category);
        return ResponseEntity.ok(records);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST','VIEWER')")
    @GetMapping("/all/summary")
    public ResponseEntity<DashboardSummaryDTO> getCompanySummary() {
        return ResponseEntity.ok(recordService.getCompanySummary());
    }
}