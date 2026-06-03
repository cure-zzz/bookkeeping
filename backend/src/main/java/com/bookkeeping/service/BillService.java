package com.bookkeeping.service;

import com.bookkeeping.dto.BillRequest;
import com.bookkeeping.dto.StatisticsVO;
import com.bookkeeping.entity.Bill;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BillService {
    List<Bill> getBills(Long userId, Integer type, Long categoryId, LocalDate startDate, LocalDate endDate);
    PageResult<Bill> getBillsByPage(Long userId, Integer type, Long categoryId, LocalDate startDate, LocalDate endDate, int page, int pageSize);
    Bill getBillById(Long userId, Long id);
    Bill createBill(Long userId, BillRequest request);
    Bill updateBill(Long userId, Long id, BillRequest request);
    void deleteBill(Long userId, Long id);
    StatisticsVO getStatistics(Long userId, String month);
    List<Bill> getRecentBills(Long userId, Integer limit);
    Map<Integer, Map<String, BigDecimal>> getYearlyStatistics(Long userId, Integer year);
    
    class PageResult<T> {
        private List<T> data;
        private long total;
        
        public PageResult() {}
        
        public PageResult(List<T> data, long total) {
            this.data = data;
            this.total = total;
        }
        
        public List<T> getData() { return data; }
        public void setData(List<T> data) { this.data = data; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }
}
