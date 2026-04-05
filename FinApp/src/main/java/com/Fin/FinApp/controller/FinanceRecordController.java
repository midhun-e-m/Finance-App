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

import java.util.UUID;

@RestController // Tells Spring this class handles HTTP web requests from React
@RequestMapping("/api/records") // All URLs in this file start with this path
public class FinanceRecordController {

    private final FinanceRecordService recordService;
    private final UserRepository userRepository; // <-- NEW: Needed to look up the logged-in user for IDOR protection

    // Injecting the services
    public FinanceRecordController(FinanceRecordService recordService, UserRepository userRepository) {
        this.recordService = recordService;
        this.userRepository = userRepository;
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

    // 2. GET PERSONAL RECORDS (Now with IDOR Protection!)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRecords( // <-- Changed to ? so we can return an error string OR the data
                                             @PathVariable UUID userId,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "5") int size,
                                             Authentication authentication) { // <-- Grabs the currently logged-in user's token details

        // --- IDOR PROTECTION CHECK ---
        String loggedInEmail = authentication.getName();
        boolean isAdminOrAnalyst = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANALYST"));

        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If the user is asking for someone else's ID, and they aren't an Admin/Analyst, block them!
        if (!loggedInUser.getId().equals(userId) && !isAdminOrAnalyst) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view these records.");
        }
        // -----------------------------

        Page<FinanceRecord> records = recordService.getAllRecordsForUser(userId, type, category, page, size);
        return ResponseEntity.ok(records);
    }

    // 3. GET PERSONAL SUMMARY (Now with IDOR Protection!)
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getDashboardSummary(@PathVariable UUID userId, Authentication authentication) {

        // --- IDOR PROTECTION CHECK ---
        String loggedInEmail = authentication.getName();
        boolean isAdminOrAnalyst = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ANALYST"));

        User loggedInUser = userRepository.findByEmail(loggedInEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!loggedInUser.getId().equals(userId) && !isAdminOrAnalyst) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to view this summary.");
        }
        // -----------------------------

        DashboardSummaryDTO summary = recordService.getSummaryForUser(userId);
        return ResponseEntity.ok(summary);
    }

    // 4. UPDATE A RECORD
    // Only an Admin can edit existing historical data
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FinanceRecord> updateRecord(@PathVariable UUID id, @Valid @RequestBody FinanceRecord record) {
        return ResponseEntity.ok(recordService.updateRecord(id, record));
    }

    // 5. DELETE A RECORD
    // Only an Admin can delete data from the database
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable UUID id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    // 6. GET ALL COMPANY RECORDS
    // Only Admins and Analysts  see the detailed company list! Viewers are blocked.
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST','VIEWER')")
    @GetMapping("/all/summary")
    public ResponseEntity<DashboardSummaryDTO> getCompanySummary() {
        return ResponseEntity.ok(recordService.getCompanySummary());
    }
}