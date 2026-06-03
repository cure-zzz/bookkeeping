package com.bookkeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsVO {
    // 总收入
    private BigDecimal totalIncome;
    // 总支出
    private BigDecimal totalExpense;
    // 结余
    private BigDecimal balance;
    // 分类支出
    private List<CategoryAmount> categoryExpenses;
    // 每日收支
    private List<DailyAmount> dailyAmounts;

    @Data
    public static class CategoryAmount {
        private Long categoryId;
        private String categoryName;
        private String color;
        private BigDecimal amount;
        private Double percentage;
    }

    @Data
    public static class DailyAmount {
        private String date;
        private BigDecimal income;
        private BigDecimal expense;
    }
}
