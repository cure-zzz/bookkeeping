package com.bookkeeping.service.impl;

import com.bookkeeping.common.BusinessException;
import com.bookkeeping.dto.LiabilityRequest;
import com.bookkeeping.dto.LiabilityRepaymentRequest;
import com.bookkeeping.entity.Liability;
import com.bookkeeping.entity.LiabilityRepayment;
import com.bookkeeping.mapper.LiabilityMapper;
import com.bookkeeping.mapper.LiabilityRepaymentMapper;
import com.bookkeeping.service.LiabilityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LiabilityServiceImpl implements LiabilityService {

    @Resource
    private LiabilityMapper liabilityMapper;
    
    @Resource
    private LiabilityRepaymentMapper repaymentMapper;

    @Override
    public List<Liability> getLiabilities(Long userId) {
        return liabilityMapper.findByUserId(userId);
    }

    @Override
    public Liability getLiabilityById(Long userId, Long id) {
        Liability liability = liabilityMapper.findById(id, userId);
        if (liability == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return liability;
    }

    @Override
    public Liability createLiability(Long userId, LiabilityRequest request) {
        Liability liability = new Liability();
        liability.setUserId(userId);
        liability.setName(request.getName());
        liability.setType(request.getType());
        liability.setAmount(request.getAmount());
        liability.setUsedAmount(request.getUsedAmount() != null ? request.getUsedAmount() : BigDecimal.ZERO);
        liability.setRemainingAmount(calculateRemaining(liability));
        liability.setDescription(request.getDescription());
        liability.setDueDate(request.getDueDate());
        liabilityMapper.insert(liability);
        return liability;
    }

    @Override
    public Liability updateLiability(Long userId, Long id, LiabilityRequest request) {
        Liability exist = liabilityMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "记录不存在");
        }
        exist.setName(request.getName());
        exist.setType(request.getType());
        exist.setAmount(request.getAmount());
        exist.setUsedAmount(request.getUsedAmount() != null ? request.getUsedAmount() : BigDecimal.ZERO);
        exist.setRemainingAmount(calculateRemaining(exist));
        exist.setDescription(request.getDescription());
        exist.setDueDate(request.getDueDate());
        liabilityMapper.update(exist);
        return exist;
    }

    @Override
    public void deleteLiability(Long userId, Long id) {
        Liability exist = liabilityMapper.findById(id, userId);
        if (exist == null) {
            throw new BusinessException(404, "记录不存在");
        }
        liabilityMapper.delete(id, userId);
    }
    
    @Override
    @Transactional
    public LiabilityRepayment createRepayment(Long userId, LiabilityRepaymentRequest request) {
        Liability liability = liabilityMapper.findById(request.getLiabilityId(), userId);
        if (liability == null) {
            throw new BusinessException(404, "负债记录不存在");
        }
        if (liability.getType() != 2) {
            throw new BusinessException(400, "只能对负债进行还款");
        }
        
        BigDecimal currentRemaining = liability.getRemainingAmount() != null ? liability.getRemainingAmount() : BigDecimal.ZERO;
        if (request.getAmount().compareTo(currentRemaining) > 0) {
            throw new BusinessException(400, "还款金额不能超过剩余负债金额");
        }
        
        LiabilityRepayment repayment = new LiabilityRepayment();
        repayment.setLiabilityId(request.getLiabilityId());
        repayment.setUserId(userId);
        repayment.setAmount(request.getAmount());
        repayment.setDescription(request.getDescription());
        // 使用用户选择的还款日期，默认为当天
        repayment.setRepaymentDate(request.getRepaymentDate() != null ? request.getRepaymentDate() : LocalDate.now());
        repaymentMapper.insert(repayment);
        
        // 更新负债剩余金额
        liability.setRemainingAmount(currentRemaining.subtract(request.getAmount()));
        liabilityMapper.update(liability);
        
        return repayment;
    }
    
    @Override
    public List<LiabilityRepayment> getRepaymentsByLiabilityId(Long userId, Long liabilityId) {
        Liability liability = liabilityMapper.findById(liabilityId, userId);
        if (liability == null) {
            throw new BusinessException(404, "负债记录不存在");
        }
        return repaymentMapper.findByLiabilityId(liabilityId);
    }
    
    @Override
    @Transactional
    public void deleteRepayment(Long userId, Long id) {
        // 获取还款记录
        List<LiabilityRepayment> repayments = repaymentMapper.findByUserId(userId);
        LiabilityRepayment exist = repayments.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (exist == null) {
            throw new BusinessException(404, "还款记录不存在");
        }
        
        // 恢复负债金额
        Liability liability = liabilityMapper.findById(exist.getLiabilityId(), userId);
        if (liability != null) {
            BigDecimal currentRemaining = liability.getRemainingAmount() != null ? liability.getRemainingAmount() : BigDecimal.ZERO;
            liability.setRemainingAmount(currentRemaining.add(exist.getAmount()));
            liabilityMapper.update(liability);
        }
        
        repaymentMapper.delete(id);
    }

    private BigDecimal calculateRemaining(Liability liability) {
        if (liability.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal used = liability.getUsedAmount() != null ? liability.getUsedAmount() : BigDecimal.ZERO;
        return liability.getAmount().subtract(used);
    }
}
