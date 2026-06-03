package com.bookkeeping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {
    private Long id;
    
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    
    @NotNull(message = "预算金额不能为空")
    @Positive(message = "预算金额必须为正数")
    private BigDecimal amount;
    
    @NotBlank(message = "月份不能为空")
    private String month;
}
