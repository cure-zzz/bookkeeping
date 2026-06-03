package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.LiabilityRequest;
import com.bookkeeping.dto.LiabilityRepaymentRequest;
import com.bookkeeping.entity.Liability;
import com.bookkeeping.entity.LiabilityRepayment;
import com.bookkeeping.service.LiabilityService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liabilities")
public class LiabilityController {

    @Resource
    private LiabilityService liabilityService;

    @GetMapping
    public Result<List<Liability>> getLiabilities(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(liabilityService.getLiabilities(userId));
    }

    @GetMapping("/{id}")
    public Result<Liability> getLiabilityById(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(liabilityService.getLiabilityById(userId, id));
    }

    @PostMapping
    public Result<Liability> createLiability(HttpServletRequest request, @Valid @RequestBody LiabilityRequest liabilityRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("添加成功", liabilityService.createLiability(userId, liabilityRequest));
    }

    @PutMapping("/{id}")
    public Result<Liability> updateLiability(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody LiabilityRequest liabilityRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("更新成功", liabilityService.updateLiability(userId, id, liabilityRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteLiability(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        liabilityService.deleteLiability(userId, id);
        return Result.success("删除成功", null);
    }
    
    // 还款相关接口
    @PostMapping("/repayments")
    public Result<LiabilityRepayment> createRepayment(HttpServletRequest request, @Valid @RequestBody LiabilityRepaymentRequest repaymentRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("还款成功", liabilityService.createRepayment(userId, repaymentRequest));
    }
    
    @GetMapping("/{id}/repayments")
    public Result<List<LiabilityRepayment>> getRepayments(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(liabilityService.getRepaymentsByLiabilityId(userId, id));
    }
    
    @DeleteMapping("/repayments/{id}")
    public Result<Void> deleteRepayment(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        liabilityService.deleteRepayment(userId, id);
        return Result.success("删除成功", null);
    }
}
