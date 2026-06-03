package com.bookkeeping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Budget {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
    private String month;  // 格式: 2024-01
    private LocalDateTime createdAt;
}
