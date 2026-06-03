package com.bookkeeping.service;

import com.bookkeeping.dto.LiabilityRequest;
import com.bookkeeping.dto.LiabilityRepaymentRequest;
import com.bookkeeping.entity.Liability;
import com.bookkeeping.entity.LiabilityRepayment;

import java.util.List;

public interface LiabilityService {
    List<Liability> getLiabilities(Long userId);
    Liability getLiabilityById(Long userId, Long id);
    Liability createLiability(Long userId, LiabilityRequest request);
    Liability updateLiability(Long userId, Long id, LiabilityRequest request);
    void deleteLiability(Long userId, Long id);
    
    // 还款相关
    LiabilityRepayment createRepayment(Long userId, LiabilityRepaymentRequest request);
    List<LiabilityRepayment> getRepaymentsByLiabilityId(Long userId, Long liabilityId);
    void deleteRepayment(Long userId, Long id);
}
