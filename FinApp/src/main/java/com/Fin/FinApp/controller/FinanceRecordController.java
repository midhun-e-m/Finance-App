package com.Fin.FinApp.controller;

import com.Fin.FinApp.dto.DashboardSummaryDTO;
import com.Fin.FinApp.entity.FinanceRecord;
import com.Fin.FinApp.entity.User;
import com.Fin.FinApp.repository.UserRepository;
import com.Fin.FinApp.service.FinanceRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController // Tells Spring this class handles HTTP web requests from React
@RequestMapping("/api/records") // All URLs in this file start with this path
public class FinanceRecordController {

    private final FinanceRecordService recordService;
    private final UserRepository userRepository;

    // Injecting the services
    public FinanceRecordController(FinanceRecordService recordService, UserRepository userRepository) {
        this.recordService = recordService;
        this.userRepository = userRepository;
    }

    // 1. CREATE A RECORD
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FinanceRecord> createRecord(@Valid @RequestBody FinanceRecord record) {
        FinanceRecord savedRecord = recordService.saveRecord(record);
        return new ResponseEntity<>(savedRecord, HttpStatus.CREATED);
    }

    // 2. GET PERSONAL RECORDS (With IDOR Protection & Date Filters!)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRecords(
            @PathVariable UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate startDate, // <-- NEW: Req 2 Date Filtering
            @RequestParam(required = false) LocalDate endDate,   // <-- NEW: Req 2 Date Filtering
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Authentication authentication) {

        // Check our private helper method to ensure no IDOR bypass
        if (!hasAccessToUserData(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view these records.");
        }

        // Make sure to pass startDate and endDate down to your Service!
        Page<FinanceRecord> records = recordService.getAllRecordsForUser(userId, type, category, startDate, endDate, page, size);
        return ResponseEntity.ok(records);
    }

    // 3. GET PERSONAL SUMMARY
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getDashboardSummary(@PathVariable UUID userId, Authentication authentication) {
        if (!hasAccessToUserData(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this summary.");
        }
        DashboardSummaryDTO summary = recordService.getSummaryForUser(userId);
        return ResponseEntity.ok(summary);
    }

    // 3.1. GET PERSONAL CATEGORY TOTALS (NEW: Req 3)
    @GetMapping("/user/{userId}/summary/by-category")
    public ResponseEntity<?> getCategoryTotals(@PathVariable UUID userId, Authentication authentication) {
        if (!hasAccessToUserData(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this data.");
        }
        return ResponseEntity.ok(recordService.getCategoryTotals(userId));
    }

    // 3.2. GET PERSONAL MONTHLY TRENDS (NEW: Req 3)
    @GetMapping("/user/{userId}/summary/by-month")
    public ResponseEntity<?> getMonthlyTrends(@PathVariable UUID userId, Authentication authentication) {
        if (!hasAccessToUserData(userId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this data.");
        }
        return ResponseEntity.ok(recordService.getMonthlyTrends(userId));
    }

    // 4. UPDATE A RECORD
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FinanceRecord> updateRecord(@PathVariable UUID id, @Valid @RequestBody FinanceRecord record) {
        return ResponseEntity.ok(recordService.updateRecord(id, record));
    }

    // 5. DELETE A RECORD
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    // 6. GET ALL COMPANY RECORDS (With Date Filters!)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @GetMapping("/all")
    public ResponseEntity<Page<FinanceRecord>> getAllCompanyRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate startDate, // <-- NEW: Req 2 Date Filtering
            @RequestParam(required = false) LocalDate endDate,   // <-- NEW: Req 2 Date Filtering
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<FinanceRecord> records = recordService.getAllCompanyRecords(type, category, startDate, endDate, page, size);
        return ResponseEntity.ok(records);
    }

    // 7. GET COMPANY SUMMARY
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST','VIEWER')")
    @GetMapping("/all/summary")
    public ResponseEntity<DashboardSummaryDTO> getCompanySummary() {
        return ResponseEntity.ok(recordService.getCompanySummary());
    }

    // 7.1. GET COMPANY CATEGORY TOTALS (NEW: Req 3)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST','VIEWER')")
    @GetMapping("/all/summary/by-category")
    public ResponseEntity<List<Map<String, Object>>> getCompanyCategoryTotals() {
        // Pass null to tell the service to get company-wide totals
        return ResponseEntity.ok(recordService.getCategoryTotals(null));
    }

    // 7.2. GET COMPANY MONTHLY TRENDS (NEW: Req 3)
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST','VIEWER')")
    @GetMapping("/all/summary/by-month")
    public ResponseEntity<List<Map<String, Object>>> getCompanyMonthlyTrends() {
        // Pass null to tell the service to get company-wide totals
        return ResponseEntity.ok(recordService.getMonthlyTrends(null));
    }

    // --- HELPER METHOD: Keeps code DRY and centralizes IDOR protection logic ---
    private boolean hasAccessToUserData(UUID requestedUserId, Authentication authentication) {
        String loggedInEmail = authentication.getName();
        boolean isAdminOrAnalyst = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANALYST"));

        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Returns true if the user is looking at their own ID, OR if they possess the Admin/Analyst VIP wristband
        return loggedInUser.getId().equals(requestedUserId) || isAdminOrAnalyst;
    }
}