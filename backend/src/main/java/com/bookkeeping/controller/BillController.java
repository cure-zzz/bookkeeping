package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.BillRequest;
import com.bookkeeping.dto.StatisticsVO;
import com.bookkeeping.entity.Bill;
import com.bookkeeping.service.BillService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Resource
    private BillService billService;

    @GetMapping
    public Result<BillService.PageResult<Bill>> getBills(
            HttpServletRequest request,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(billService.getBillsByPage(userId, type, categoryId, startDate, endDate, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Bill> getBillById(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(billService.getBillById(userId, id));
    }

    @PostMapping
    public Result<Bill> createBill(HttpServletRequest request, @Valid @RequestBody BillRequest billRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("添加成功", billService.createBill(userId, billRequest));
    }

    @PutMapping("/{id}")
    public Result<Bill> updateBill(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody BillRequest billRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("更新成功", billService.updateBill(userId, id, billRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBill(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        billService.deleteBill(userId, id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/statistics")
    public Result<StatisticsVO> getStatistics(HttpServletRequest request, @RequestParam String month) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(billService.getStatistics(userId, month));
    }

    @GetMapping("/recent")
    public Result<List<Bill>> getRecentBills(HttpServletRequest request, @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(billService.getRecentBills(userId, limit));
    }

    @GetMapping("/yearly")
    public Result<Map<Integer, Map<String, BigDecimal>>> getYearlyStatistics(
            HttpServletRequest request, 
            @RequestParam Integer year) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(billService.getYearlyStatistics(userId, year));
    }
}
