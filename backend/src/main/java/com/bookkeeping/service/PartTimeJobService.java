package com.bookkeeping.service;

import com.bookkeeping.entity.PartTimeJob;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PartTimeJobService {
    List<PartTimeJob> getPartTimeJobs(Long userId);
    PartTimeJob getPartTimeJobById(Long userId, Long id);
    List<PartTimeJob> getPartTimeJobsByMonth(Long userId, int year, int month);
    List<PartTimeJob> getPartTimeJobsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    List<PartTimeJob> getPartTimeJobsByDateRangeWithPage(Long userId, LocalDate startDate, LocalDate endDate, int page, int size);
    List<PartTimeJob> getPartTimeJobsWithPage(Long userId, int page, int size);
    int countByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    int countAll(Long userId);
    BigDecimal getTotalAmount(Long userId);
    BigDecimal getMonthlyAmount(Long userId, int year, int month);
    BigDecimal getAmountByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    PartTimeJob createPartTimeJob(Long userId, com.bookkeeping.dto.PartTimeJobRequest request);
    PartTimeJob updatePartTimeJob(Long userId, Long id, com.bookkeeping.dto.PartTimeJobRequest request);
    void deletePartTimeJob(Long userId, Long id);
}
