package com.Fin.FinApp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "finance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be strictly greater than zero")
    @Column(nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @NotBlank(message = "Category cannot be blank")
    @Column(nullable = false)
    private String category;

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;

    private String notes; // No @Column needed if we just want default settings

    // --- THE RELATIONSHIP ---
    @ManyToOne(fetch = FetchType.LAZY) // Many records can belong to One user
    @JoinColumn(name = "user_id", nullable = false) // Creates a Foreign Key column
    private User user;
}