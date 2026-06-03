package com.bookkeeping.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PartTimeJob {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private LocalDate workDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
