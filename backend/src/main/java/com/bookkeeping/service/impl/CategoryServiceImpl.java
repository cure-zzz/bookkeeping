package com.bookkeeping.service.impl;

import com.bookkeeping.common.BusinessException;
import com.bookkeeping.entity.Category;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.service.CategoryService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @PostConstruct
    public void init() {
        initDefaultCategories();
    }

    @Override
    public List<Category> getAllCategories(Long userId) {
        return categoryMapper.findAll(userId);
    }

    @Override
    public List<Category> getCategoriesByType(Long userId, Integer type) {
        return categoryMapper.findByType(type, userId);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.findById(id);
    }

    @Override
    public Category createCategory(Long userId, Category category) {
        category.setUserId(userId);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Long userId, Long id, Category category) {
        Category exist = categoryMapper.findById(id);
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (exist.getUserId() != null && !exist.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此分类");
        }
        category.setId(id);
        category.setUserId(userId);
        categoryMapper.update(category);
        return category;
    }

    @Override
    public void deleteCategory(Long userId, Long id) {
        Category exist = categoryMapper.findById(id);
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        if (exist.getUserId() == null) {
            throw new BusinessException(400, "系统预设分类不能删除");
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此分类");
        }
        categoryMapper.delete(id, userId);
    }

    @Override
    public void initDefaultCategories() {
        // 检查是否已初始化
        List<Category> existing = categoryMapper.findAll(null);
        if (!existing.isEmpty()) {
            return;
        }

        // 收入分类
        String[][] incomeCategories = {
            {"工资", "icon-wages", "#67C23A"},
            {"奖金", "icon-bonus", "#E6A23C"},
            {"投资收益", "icon-invest", "#409EFF"},
            {"副业收入", "icon-side", "#909399"},
            {"其他收入", "icon-other", "#F56C6C"}
        };

        // 支出分类
        String[][] expenseCategories = {
            {"餐饮", "icon-food", "#F56C6C"},
            {"交通", "icon-transport", "#409EFF"},
            {"购物", "icon-shopping", "#E6A23C"},
            {"居住", "icon-house", "#909399"},
            {"医疗", "icon-medical", "#67C23A"},
            {"娱乐", "icon-entertainment", "#9B59B6"},
            {"教育", "icon-education", "#3498DB"},
            {"通讯", "icon-communication", "#1ABC9C"},
            {"服装", "icon-clothes", "#E91E63"},
            {"其他支出", "icon-other-expense", "#607D8B"}
        };

        for (String[] cat : incomeCategories) {
            Category category = new Category();
            category.setName(cat[0]);
            category.setIcon(cat[1]);
            category.setType(1);
            category.setColor(cat[2]);
            category.setUserId(null);
            categoryMapper.insert(category);
        }

        for (String[] cat : expenseCategories) {
            Category category = new Category();
            category.setName(cat[0]);
            category.setIcon(cat[1]);
            category.setType(2);
            category.setColor(cat[2]);
            category.setUserId(null);
            categoryMapper.insert(category);
        }
    }
}
