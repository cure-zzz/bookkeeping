package com.bookkeeping.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.bookkeeping.common.BusinessException;
import com.bookkeeping.config.JwtTokenProvider;
import com.bookkeeping.dto.*;
import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否存在
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        
        // 检查邮箱是否已被注册
        User existEmail = userMapper.findByEmail(request.getEmail());
        if (existEmail != null) {
            throw new BusinessException(400, "该邮箱已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(SecureUtil.md5(request.getPassword())); // MD5加密
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setAvatar("/avatar/default.jpg");
        userMapper.insert(user);
        
        // 生成Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        
        // 验证密码
        String passwordHash = SecureUtil.md5(request.getPassword());
        if (!passwordHash.equals(user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        
        // 生成Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
