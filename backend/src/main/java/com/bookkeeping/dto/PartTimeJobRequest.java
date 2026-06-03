package com.bookkeeping.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PartTimeJobRequest {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate workDate;
}
