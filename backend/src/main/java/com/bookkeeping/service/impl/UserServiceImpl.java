package com.bookkeeping.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.bookkeeping.common.BusinessException;
import com.bookkeeping.dto.*;
import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.BillMapper;
import com.bookkeeping.mapper.CategoryMapper;
import com.bookkeeping.mapper.UserMapper;
import com.bookkeeping.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private BillMapper billMapper;
    
    @Resource
    private CategoryMapper categoryMapper;

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

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        userMapper.update(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 验证当前密码
        String oldPasswordHash = SecureUtil.md5(request.getOldPassword());
        if (!oldPasswordHash.equals(user.getPassword())) {
            throw new BusinessException(400, "当前密码错误");
        }
        
        // 更新密码
        String newPasswordHash = SecureUtil.md5(request.getNewPassword());
        userMapper.updatePassword(userId, newPasswordHash);
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO vo = new UserStatsVO();
        vo.setBillCount(billMapper.countByUserId(userId));
        vo.setCategoryCount(categoryMapper.countByUserId(userId));
        vo.setTotalIncome(billMapper.sumAmountByType(userId, 1));
        vo.setTotalExpense(billMapper.sumAmountByType(userId, 2));
        return vo;
    }
}
