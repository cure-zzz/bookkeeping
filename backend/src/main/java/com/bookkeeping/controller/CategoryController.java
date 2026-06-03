package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.entity.Category;
import com.bookkeeping.service.CategoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> getCategories(
            HttpServletRequest request,
            @RequestParam(required = false) Integer type) {
        Long userId = (Long) request.getAttribute("userId");
        if (type != null) {
            return Result.success(categoryService.getCategoriesByType(userId, type));
        }
        return Result.success(categoryService.getAllCategories(userId));
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @PostMapping
    public Result<Category> createCategory(HttpServletRequest request, @RequestBody Category category) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("创建成功", categoryService.createCategory(userId, category));
    }

    @PutMapping("/{id}")
    public Result<Category> updateCategory(HttpServletRequest request, @PathVariable Long id, @RequestBody Category category) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("更新成功", categoryService.updateCategory(userId, id, category));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        categoryService.deleteCategory(userId, id);
        return Result.success("删除成功", null);
    }
}
