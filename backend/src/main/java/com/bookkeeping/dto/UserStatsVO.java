package com.bookkeeping.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserStatsVO {
    private Long billCount;
    private Long categoryCount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
}
