package com.bookkeeping.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Liability {
    private Long id;
    private Long userId;
    private String name;           // 负债名称，如"花呗"、"信用卡"、"房贷"
    private Integer type;          // 1:资产 2:负债
    private BigDecimal amount;     // 负债金额
    private BigDecimal usedAmount; // 已使用金额
    private BigDecimal remainingAmount; // 剩余额度
    private String description;     // 描述/备注
    private LocalDate dueDate;     // 到期日（可选）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
