package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.UpdatePasswordRequest;
import com.bookkeeping.dto.UpdateProfileRequest;
import com.bookkeeping.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(HttpServletRequest request, @Valid @RequestBody UpdateProfileRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, req);
        return Result.success("修改成功", null);
    }

    @PutMapping("/password")
    public Result<?> updatePassword(HttpServletRequest request, @Valid @RequestBody UpdatePasswordRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updatePassword(userId, req);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/stats")
    public Result<?> getUserStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserStats(userId));
    }
}
