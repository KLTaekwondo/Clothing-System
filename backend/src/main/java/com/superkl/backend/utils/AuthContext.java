package com.superkl.backend.utils;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthContext {
    private final JwtUtil jwtUtil;

    private static String extractTokenAndValidateFromCookie(HttpServletRequest request, JwtUtil jwtUtil) {
        String token = extractTokenFromCookie(request, jwtUtil);
        //自动检验token是否为空或过期
        // token为空，说明用户没有登录，抛出异常401，提示用户先登录
        if (token == null) {
            throw new BusinessException(401, "未登录，请先登录");
        }
        // token不为空，说明用户登录了，验证token是否过期
        // 如果token过期，抛出异常401，提示用户登录验证失败，重新重新登录
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "token过期，请重新登录");
        }

        // token验证通过，返回token值
        return token;
    }

    public static RequestUser getRequestUserFromCookie(HttpServletRequest request, JwtUtil jwtUtil) {
        String token = extractTokenAndValidateFromCookie(request, jwtUtil);
        return jwtUtil.parseRequestUser(token);
    }

    // 用来从cookie中提取token值，主要用于redis踢人时，需要根据token值来踢人
    public static String extractTokenFromCookie(HttpServletRequest request, JwtUtil jwtUtil) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return token;
    }
}
