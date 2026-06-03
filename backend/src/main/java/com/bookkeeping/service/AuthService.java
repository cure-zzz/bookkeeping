package com.bookkeeping.service;

import com.bookkeeping.dto.*;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserInfoVO getUserInfo(Long userId);
}
