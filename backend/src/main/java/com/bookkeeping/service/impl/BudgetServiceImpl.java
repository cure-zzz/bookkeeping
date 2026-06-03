package com.bookkeeping.service.impl;

import com.bookkeeping.common.BusinessException;
import com.bookkeeping.dto.BudgetRequest;
import com.bookkeeping.entity.Budget;
import com.bookkeeping.entity.Category;
import com.bookkeeping.mapper.BudgetMapper;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.service.BudgetService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Resource
    private BudgetMapper budgetMapper;
    
    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<Budget> getBudgets(Long userId, String month) {
        return budgetMapper.findByMonth(userId, month);
    }

    @Override
    public Budget createBudget(Long userId, BudgetRequest request) {
        // 验证分类存在且为支出分类
        Category category = categoryMapper.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "分类不存在");
        }
        if (category.getType() != 2) {
            throw new BusinessException(400, "预算只能设置在支出分类上");
        }
        
        // 检查是否已存在
        Budget exist = budgetMapper.findByCategoryAndMonth(userId, request.getCategoryId(), request.getMonth());
        if (exist != null) {
            throw new BusinessException(400, "该分类本月预算已存在，请修改");
        }
        
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budgetMapper.insert(budget);
        return budget;
    }

    @Override
    public Budget updateBudget(Long userId, Long id, BudgetRequest request) {
        Budget exist = budgetMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "预算不存在");
        }
        
        // 验证分类存在且为支出分类
        Category category = categoryMapper.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "分类不存在");
        }
        if (category.getType() != 2) {
            throw new BusinessException(400, "预算只能设置在支出分类上");
        }
        
        exist.setCategoryId(request.getCategoryId());
        exist.setAmount(request.getAmount());
        exist.setMonth(request.getMonth());
        budgetMapper.update(exist);
        return exist;
    }

    @Override
    public void deleteBudget(Long userId, Long id) {
        Budget exist = budgetMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "预算不存在");
        }
        budgetMapper.delete(id, userId);
    }
}
