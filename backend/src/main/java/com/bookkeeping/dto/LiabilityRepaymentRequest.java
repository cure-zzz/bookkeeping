package com.bookkeeping.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LiabilityRepaymentRequest {
    
    @NotNull(message = "负债ID不能为空")
    private Long liabilityId;
    
    @NotNull(message = "还款金额不能为空")
    @Positive(message = "还款金额必须为正数")
    private BigDecimal amount;
    
    private String description;
    
    private LocalDate repaymentDate;  // 还款日期，可选择
}
