package com.Fin.FinApp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardSummaryDTO {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
}