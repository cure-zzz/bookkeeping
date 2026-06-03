package com.bookkeeping.service.impl;

import com.bookkeeping.common.BusinessException;
import com.bookkeeping.dto.BillRequest;
import com.bookkeeping.dto.StatisticsVO;
import com.bookkeeping.entity.Bill;
import com.bookkeeping.entity.Category;
import com.bookkeeping.entity.Liability;
import com.bookkeeping.mapper.BillMapper;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.mapper.LiabilityMapper;
import com.bookkeeping.service.BillService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillService {

    @Resource
    private BillMapper billMapper;
    
    @Resource
    private CategoryMapper categoryMapper;
    
    @Resource
    private LiabilityMapper liabilityMapper;

    @Override
    public List<Bill> getBills(Long userId, Integer type, Long categoryId, LocalDate startDate, LocalDate endDate) {
        return billMapper.findPage(userId, type, categoryId, startDate, endDate, 0, Integer.MAX_VALUE);
    }
    
    @Override
    public PageResult<Bill> getBillsByPage(Long userId, Integer type, Long categoryId, LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Bill> bills = billMapper.findPage(userId, type, categoryId, startDate, endDate, offset, pageSize);
        long total = billMapper.count(userId, type, categoryId, startDate, endDate);
        return new PageResult<>(bills, total);
    }

    @Override
    public Bill getBillById(Long userId, Long id) {
        Bill bill = billMapper.findById(id, userId);
        if (bill == null) {
            throw new BusinessException(404, "账单不存在");
        }
        return bill;
    }

    @Override
    @Transactional
    public Bill createBill(Long userId, BillRequest request) {
        // 验证分类存在
        Category category = categoryMapper.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "分类不存在");
        }
        
        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setAmount(request.getAmount());
        bill.setType(request.getType());
        bill.setCategoryId(request.getCategoryId());
        bill.setPaymentAccountId(request.getPaymentAccountId());
        bill.setDescription(request.getDescription());
        bill.setBillDate(request.getBillDate());
        billMapper.insert(bill);
        
        // 更新资金账户余额
        updateAccountBalance(userId, request.getPaymentAccountId(), request.getType(), request.getAmount());
        
        return bill;
    }

    @Override
    @Transactional
    public Bill updateBill(Long userId, Long id, BillRequest request) {
        Bill exist = billMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "账单不存在");
        }
        
        // 验证分类存在
        Category category = categoryMapper.findById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "分类不存在");
        }
        
        // 先撤销原来的账户变动
        if (exist.getPaymentAccountId() != null) {
            reverseAccountBalance(userId, exist.getPaymentAccountId(), exist.getType(), exist.getAmount());
        }
        
        // 更新账单
        exist.setAmount(request.getAmount());
        exist.setType(request.getType());
        exist.setCategoryId(request.getCategoryId());
        exist.setPaymentAccountId(request.getPaymentAccountId());
        exist.setDescription(request.getDescription());
        exist.setBillDate(request.getBillDate());
        billMapper.update(exist);
        
        // 应用新的账户变动
        if (request.getPaymentAccountId() != null) {
            updateAccountBalance(userId, request.getPaymentAccountId(), request.getType(), request.getAmount());
        }
        
        return exist;
    }

    @Override
    @Transactional
    public void deleteBill(Long userId, Long id) {
        Bill exist = billMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "账单不存在");
        }
        
        // 撤销账户变动
        if (exist.getPaymentAccountId() != null) {
            reverseAccountBalance(userId, exist.getPaymentAccountId(), exist.getType(), exist.getAmount());
        }
        
        billMapper.delete(id, userId);
    }

    @Override
    public StatisticsVO getStatistics(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        StatisticsVO vo = new StatisticsVO();
        
        // 计算总收入和总支出
        BigDecimal totalIncome = billMapper.sumAmount(userId, 1, startDate, endDate);
        BigDecimal totalExpense = billMapper.sumAmount(userId, 2, startDate, endDate);
        
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(totalIncome.subtract(totalExpense));
        
        // 分类支出
        List<BillMapper.CategoryAmountVO> categoryAmounts = billMapper.sumByCategory(userId, startDate, endDate);
        List<StatisticsVO.CategoryAmount> categoryExpenseList = new ArrayList<>();
        
        for (BillMapper.CategoryAmountVO ca : categoryAmounts) {
            StatisticsVO.CategoryAmount item = new StatisticsVO.CategoryAmount();
            item.setCategoryId(ca.getCategoryId());
            Category cat = categoryMapper.findById(ca.getCategoryId());
            if (cat != null) {
                item.setCategoryName(cat.getName());
                item.setColor(cat.getColor());
            }
            item.setAmount(ca.getTotal());
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                item.setPercentage(ca.getTotal().divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
            } else {
                item.setPercentage(0.0);
            }
            categoryExpenseList.add(item);
        }
        vo.setCategoryExpenses(categoryExpenseList);
        
        // 每日收支
        List<BillMapper.DailyAmountVO> dailyAmounts = billMapper.sumByDate(userId, startDate, endDate);
        Map<String, BigDecimal> incomeMap = new HashMap<>();
        Map<String, BigDecimal> expenseMap = new HashMap<>();
        
        for (BillMapper.DailyAmountVO da : dailyAmounts) {
            String dateStr = da.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            if (da.getType() == 1) {
                incomeMap.merge(dateStr, da.getTotal(), BigDecimal::add);
            } else {
                expenseMap.merge(dateStr, da.getTotal(), BigDecimal::add);
            }
        }
        
        List<StatisticsVO.DailyAmount> dailyList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            StatisticsVO.DailyAmount item = new StatisticsVO.DailyAmount();
            String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
            item.setDate(dateStr);
            item.setIncome(incomeMap.getOrDefault(dateStr, BigDecimal.ZERO));
            item.setExpense(expenseMap.getOrDefault(dateStr, BigDecimal.ZERO));
            dailyList.add(item);
            current = current.plusDays(1);
        }
        vo.setDailyAmounts(dailyList);
        
        return vo;
    }

    @Override
    public List<Bill> getRecentBills(Long userId, Integer limit) {
        return billMapper.findRecent(userId, limit);
    }
    
    @Override
    public Map<Integer, Map<String, BigDecimal>> getYearlyStatistics(Long userId, Integer year) {
        List<BillMapper.MonthlyAmountVO> monthlyData = billMapper.sumByMonth(userId, year);
        
        // 初始化12个月的数据
        Map<Integer, Map<String, BigDecimal>> result = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, BigDecimal> monthData = new HashMap<>();
            monthData.put("income", BigDecimal.ZERO);
            monthData.put("expense", BigDecimal.ZERO);
            result.put(i, monthData);
        }
        
        // 填充数据
        for (BillMapper.MonthlyAmountVO vo : monthlyData) {
            Map<String, BigDecimal> monthData = result.get(vo.getMonth());
            if (monthData != null) {
                if (vo.getType() == 1) {
                    monthData.put("income", vo.getTotal());
                } else if (vo.getType() == 2) {
                    monthData.put("expense", vo.getTotal());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 更新资金账户余额
     * @param userId 用户ID
     * @param accountId 账户ID
     * @param type 账单类型 1:收入 2:支出
     * @param amount 金额
     */
    private void updateAccountBalance(Long userId, Long accountId, Integer type, BigDecimal amount) {
        if (accountId == null || amount == null) return;
        
        Liability account = liabilityMapper.findById(accountId, userId);
        if (account == null) return;
        
        BigDecimal currentRemaining = account.getRemainingAmount() != null ? account.getRemainingAmount() : BigDecimal.ZERO;
        
        if (type == 1) {
            // 收入：增加余额
            account.setRemainingAmount(currentRemaining.add(amount));
        } else {
            // 支出：减少余额
            account.setRemainingAmount(currentRemaining.subtract(amount));
        }
        // usedAmount 记录累计收入流水（便于统计）
        if (type == 1) {
            BigDecimal currentUsed = account.getUsedAmount() != null ? account.getUsedAmount() : BigDecimal.ZERO;
            account.setUsedAmount(currentUsed.add(amount));
        }
        
        liabilityMapper.update(account);
    }
    
    /**
     * 撤销资金账户变动（用于更新或删除账单时回滚）
     * @param userId 用户ID
     * @param accountId 账户ID
     * @param type 账单类型 1:收入 2:支出
     * @param amount 金额
     */
    private void reverseAccountBalance(Long userId, Long accountId, Integer type, BigDecimal amount) {
        if (accountId == null || amount == null) return;
        
        Liability account = liabilityMapper.findById(accountId, userId);
        if (account == null) return;
        
        BigDecimal currentRemaining = account.getRemainingAmount() != null ? account.getRemainingAmount() : BigDecimal.ZERO;
        
        if (type == 1) {
            // 收入撤销：减少余额
            account.setRemainingAmount(currentRemaining.subtract(amount));
            // 同时减少累计收入
            BigDecimal currentUsed = account.getUsedAmount() != null ? account.getUsedAmount() : BigDecimal.ZERO;
            account.setUsedAmount(currentUsed.subtract(amount));
        } else {
            // 支出撤销：增加余额（回退支出）
            account.setRemainingAmount(currentRemaining.add(amount));
        }
        
        liabilityMapper.update(account);
    }
}
