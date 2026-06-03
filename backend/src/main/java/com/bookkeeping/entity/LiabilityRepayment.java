package com.bookkeeping.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LiabilityRepayment {
    private Long id;
    private Long liabilityId;     // 关联的负债ID
    private Long userId;
    private BigDecimal amount;     // 还款金额
    private String description;    // 还款说明
    private LocalDate repaymentDate;  // 还款日期（可选择）
    private LocalDateTime createdAt;  // 实际创建时间
}
