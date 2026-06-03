package com.bookkeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LiabilityRequest {
    private String name;
    private Integer type;          // 1:资产 2:负债
    private BigDecimal amount;
    private BigDecimal usedAmount;
    private String description;
    private LocalDate dueDate;
}
