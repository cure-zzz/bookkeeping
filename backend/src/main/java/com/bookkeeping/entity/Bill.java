package com.bookkeeping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Bill {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private Integer type;  // 1:收入 2:支出
    private Long categoryId;
    private Long paymentAccountId;  // 支付方式/资金账户ID
    private String description;
    private LocalDate billDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
