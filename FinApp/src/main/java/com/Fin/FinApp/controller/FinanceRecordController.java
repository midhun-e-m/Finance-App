package com.Fin.FinApp.controller;

import com.Fin.FinApp.dto.DashboardSummaryDTO;
import com.Fin.FinApp.entity.FinanceRecord;
import com.Fin.FinApp.service.FinanceRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController // Tells Spring this class handles HTTP web requests from React
@RequestMapping("/api/records") // All URLs in this file start with this path
public class FinanceRecordController {

    private final FinanceRecordService recordService;

    public FinanceRecordController(FinanceRecordService recordService) {
        this.recordService = recordService;
    }

    // 1. CREATE A RECORD
    // @PreAuthorize is our Bouncer.
    // If you don't have the ADMIN role , you are blocked!
    // @Valid forces Spring to check our Entity rules (like amount > 0) before accepting the data.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FinanceRecord> createRecord(@Valid @RequestBody FinanceRecord record) {
        FinanceRecord savedRecord = recordService.saveRecord(record);
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    // 2. GET PERSONAL RECORDS
    // Default route for Viewers to get their own paginated data.
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<FinanceRecord>> getUserRecords(
            @PathVariable UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page, // Tells React we start on page 0
            @RequestParam(defaultValue = "5") int size) { // Tells React to send 5 items at a time

        Page<FinanceRecord> records = recordService.getAllRecordsForUser(userId, type, category, page, size);
        return ResponseEntity.ok(records);
    }

    // 3. GET PERSONAL SUMMARY
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(@PathVariable UUID userId) {
        DashboardSummaryDTO summary = recordService.getSummaryForUser(userId);
        return ResponseEntity.ok(summary);
    }

    // 4. UPDATE A RECORD
    // Only an Admin can edit existing historical data! (God Mode)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FinanceRecord> updateRecord(@PathVariable UUID id, @Valid @RequestBody FinanceRecord record) {
        return ResponseEntity.ok(recordService.updateRecord(id, record));
    }

    // 5. DELETE A RECORD
    // Only an Admin can delete data from the database! (God Mode)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    // 6. GET ALL COMPANY RECORDS
    // Only Admins and Analysts have the VIP Wristband required to see the detailed company list! Viewers are blocked.
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @GetMapping("/all")
    public ResponseEntity<Page<FinanceRecord>> getAllCompanyRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<FinanceRecord> records = recordService.getAllCompanyRecords(type, category, page, size);
        return ResponseEntity.ok(records);
    }

    // 7. GET COMPANY SUMMARY
    // The Bouncer checks the VIP wristband, but allows everyone (including Viewers) to see the high-level math.
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST','VIEWER')")
    @GetMapping("/all/summary")
    public ResponseEntity<DashboardSummaryDTO> getCompanySummary() {
        return ResponseEntity.ok(recordService.getCompanySummary());
    }
}