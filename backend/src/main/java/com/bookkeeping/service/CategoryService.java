package com.bookkeeping.service;

import com.bookkeeping.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories(Long userId);
    List<Category> getCategoriesByType(Long userId, Integer type);
    Category getCategoryById(Long id);
    Category createCategory(Long userId, Category category);
    Category updateCategory(Long userId, Long id, Category category);
    void deleteCategory(Long userId, Long id);
    void initDefaultCategories();
}
