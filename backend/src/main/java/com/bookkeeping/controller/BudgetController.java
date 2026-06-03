package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.BudgetRequest;
import com.bookkeeping.entity.Budget;
import com.bookkeeping.service.BudgetService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Resource
    private BudgetService budgetService;

    @GetMapping
    public Result<List<Budget>> getBudgets(HttpServletRequest request, @RequestParam String month) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(budgetService.getBudgets(userId, month));
    }

    @PostMapping
    public Result<Budget> createBudget(HttpServletRequest request, @Valid @RequestBody BudgetRequest budgetRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("创建成功", budgetService.createBudget(userId, budgetRequest));
    }

    @PutMapping("/{id}")
    public Result<Budget> updateBudget(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody BudgetRequest budgetRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("更新成功", budgetService.updateBudget(userId, id, budgetRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBudget(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        budgetService.deleteBudget(userId, id);
        return Result.success("删除成功", null);
    }
}
