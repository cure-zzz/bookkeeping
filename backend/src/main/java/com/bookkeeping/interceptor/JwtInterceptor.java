package com.bookkeeping.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bookkeeping.common.BusinessException;
import com.bookkeeping.config.JwtTokenProvider;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String token = request.getHeader("Authorization");
        
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(401, "未登录");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(401, "登录已过期");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        
        return true;
    }
}
