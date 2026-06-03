package com.bookkeeping.service;

import com.bookkeeping.dto.BudgetRequest;
import com.bookkeeping.entity.Budget;

import java.util.List;

public interface BudgetService {
    List<Budget> getBudgets(Long userId, String month);
    Budget createBudget(Long userId, BudgetRequest request);
    Budget updateBudget(Long userId, Long id, BudgetRequest request);
    void deleteBudget(Long userId, Long id);
}
