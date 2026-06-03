package com.bookkeeping.service;

import com.bookkeeping.dto.UpdatePasswordRequest;
import com.bookkeeping.dto.UpdateProfileRequest;
import com.bookkeeping.dto.UserInfoVO;
import com.bookkeeping.dto.UserStatsVO;

public interface UserService {
    
    UserInfoVO getUserInfo(Long userId);
    
    void updateProfile(Long userId, UpdateProfileRequest request);
    
    void updatePassword(Long userId, UpdatePasswordRequest request);
    
    UserStatsVO getUserStats(Long userId);
}
