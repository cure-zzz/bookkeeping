package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.PartTimeJobRequest;
import com.bookkeeping.entity.PartTimeJob;
import com.bookkeeping.service.PartTimeJobService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/part-time-job")
public class PartTimeJobController {

    @Resource
    private PartTimeJobService partTimeJobService;

    @GetMapping
    public Result<List<PartTimeJob>> getAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<PartTimeJob> list = partTimeJobService.getPartTimeJobs(userId);
        return Result.success(list);
    }

    @GetMapping("/monthly")
    public Result<Map<String, Object>> getMonthlyStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        List<PartTimeJob> jobs = partTimeJobService.getPartTimeJobsByMonth(userId, year, month);
        BigDecimal monthlyAmount = partTimeJobService.getMonthlyAmount(userId, year, month);
        
        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("monthlyAmount", monthlyAmount);
        result.put("year", year);
        result.put("month", month);
        
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        BigDecimal totalAmount = partTimeJobService.getTotalAmount(userId);
        BigDecimal monthlyAmount = partTimeJobService.getMonthlyAmount(userId, year, month);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("monthlyAmount", monthlyAmount);
        result.put("currentMonth", year + "-" + String.format("%02d", month));
        
        return Result.success(result);
    }

    @GetMapping("/query")
    public Result<Map<String, Object>> queryByDateRange(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") String startMonth,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") String endMonth,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        
        Long userId = (Long) request.getAttribute("userId");
        LocalDate now = LocalDate.now();
        
        List<PartTimeJob> jobs;
        int total;
        BigDecimal totalAmount;
        String rangeLabel = "";
        
        // 如果指定了全部查询
        if (all || (startMonth == null && endMonth == null && startDate == null && endDate == null)) {
            jobs = partTimeJobService.getPartTimeJobsWithPage(userId, page, size);
            total = partTimeJobService.countAll(userId);
            totalAmount = partTimeJobService.getTotalAmount(userId);
            rangeLabel = "全部记录";
        } else {
            LocalDate queryStartDate;
            LocalDate queryEndDate;
            
            // 如果指定了日期范围，直接使用
            if (startDate != null && endDate != null) {
                queryStartDate = startDate;
                queryEndDate = endDate;
            } else {
                // 根据月份范围计算
                String[] startParts = startMonth.split("-");
                String[] endParts = endMonth.split("-");
                queryStartDate = LocalDate.of(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]), 1);
                queryEndDate = LocalDate.of(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]), 1);
                queryEndDate = queryEndDate.withDayOfMonth(queryEndDate.lengthOfMonth());
            }
            
            jobs = partTimeJobService.getPartTimeJobsByDateRangeWithPage(userId, queryStartDate, queryEndDate, page, size);
            total = partTimeJobService.countByDateRange(userId, queryStartDate, queryEndDate);
            totalAmount = partTimeJobService.getAmountByDateRange(userId, queryStartDate, queryEndDate);
            rangeLabel = queryStartDate + " 至 " + queryEndDate;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("totalAmount", totalAmount);
        result.put("rangeLabel", rangeLabel);
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<PartTimeJob> getById(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        PartTimeJob job = partTimeJobService.getPartTimeJobById(userId, id);
        return Result.success(job);
    }

    @PostMapping
    public Result<PartTimeJob> create(HttpServletRequest request, @RequestBody PartTimeJobRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        PartTimeJob job = partTimeJobService.createPartTimeJob(userId, req);
        return Result.success("添加成功", job);
    }

    @PutMapping("/{id}")
    public Result<PartTimeJob> update(HttpServletRequest request, @PathVariable Long id, @RequestBody PartTimeJobRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        PartTimeJob job = partTimeJobService.updatePartTimeJob(userId, id, req);
        return Result.success("更新成功", job);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        partTimeJobService.deletePartTimeJob(userId, id);
        return Result.success("删除成功", null);
    }
}
