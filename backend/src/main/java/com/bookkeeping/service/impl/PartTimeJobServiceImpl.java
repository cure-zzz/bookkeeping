package com.bookkeeping.service.impl;

import com.bookkeeping.common.BusinessException;
import com.bookkeeping.dto.PartTimeJobRequest;
import com.bookkeeping.entity.PartTimeJob;
import com.bookkeeping.mapper.PartTimeJobMapper;
import com.bookkeeping.service.PartTimeJobService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PartTimeJobServiceImpl implements PartTimeJobService {

    @Resource
    private PartTimeJobMapper partTimeJobMapper;

    @Override
    public List<PartTimeJob> getPartTimeJobs(Long userId) {
        return partTimeJobMapper.findByUserId(userId);
    }

    @Override
    public PartTimeJob getPartTimeJobById(Long userId, Long id) {
        PartTimeJob job = partTimeJobMapper.findById(id, userId);
        if (job == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return job;
    }

    @Override
    public List<PartTimeJob> getPartTimeJobsByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return partTimeJobMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<PartTimeJob> getPartTimeJobsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return partTimeJobMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<PartTimeJob> getPartTimeJobsByDateRangeWithPage(Long userId, LocalDate startDate, LocalDate endDate, int page, int size) {
        int offset = (page - 1) * size;
        return partTimeJobMapper.findByUserIdAndDateRangeWithPage(userId, startDate, endDate, offset, size);
    }

    @Override
    public int countByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return partTimeJobMapper.countByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<PartTimeJob> getPartTimeJobsWithPage(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return partTimeJobMapper.findByUserIdWithPage(userId, offset, size);
    }

    @Override
    public int countAll(Long userId) {
        return partTimeJobMapper.countByUserId(userId);
    }

    @Override
    public BigDecimal getTotalAmount(Long userId) {
        return partTimeJobMapper.getTotalAmount(userId);
    }

    @Override
    public BigDecimal getMonthlyAmount(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return partTimeJobMapper.getAmountByDateRange(userId, startDate, endDate);
    }

    @Override
    public BigDecimal getAmountByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return partTimeJobMapper.getAmountByDateRange(userId, startDate, endDate);
    }

    @Override
    public PartTimeJob createPartTimeJob(Long userId, PartTimeJobRequest request) {
        PartTimeJob job = new PartTimeJob();
        job.setUserId(userId);
        job.setAmount(request.getAmount());
        job.setDescription(request.getDescription());
        job.setWorkDate(request.getWorkDate());
        partTimeJobMapper.insert(job);
        return job;
    }

    @Override
    public PartTimeJob updatePartTimeJob(Long userId, Long id, PartTimeJobRequest request) {
        PartTimeJob exist = partTimeJobMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "记录不存在");
        }
        exist.setAmount(request.getAmount());
        exist.setDescription(request.getDescription());
        exist.setWorkDate(request.getWorkDate());
        partTimeJobMapper.update(exist);
        return exist;
    }

    @Override
    public void deletePartTimeJob(Long userId, Long id) {
        PartTimeJob exist = partTimeJobMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "记录不存在");
        }
        partTimeJobMapper.delete(id, userId);
    }
}
