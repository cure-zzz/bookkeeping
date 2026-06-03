package com.bookkeeping.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillRequest {
    private Long id;
    
    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须为正数")
    private BigDecimal amount;
    
    @NotNull(message = "类型不能为空")
    private Integer type;
    
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    
    private Long paymentAccountId;  // 支付方式/资金账户ID
    
    private String description;
    
    @NotNull(message = "日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;
}
