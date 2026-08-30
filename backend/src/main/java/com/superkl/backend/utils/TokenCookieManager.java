package com.superkl.backend.utils;

import jakarta.servlet.http.Cookie;

public class TokenCookieManager {

    private TokenCookieManager() {}// 私有构造方法，防止外部实例化

    public static Cookie writeTokenCookie(String token ,boolean IsSecure) {
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7 * 24 * 3600);
        cookie.setSecure(IsSecure);
        // 线上 https：允许桌面端跨站携带登录态；本地 http 开发保持 Lax
        if (IsSecure) {
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setAttribute("SameSite", "Lax");
        }
        return cookie;
    }

    public static Cookie clearTokenCookie(boolean IsSecure) {
        Cookie cookie = new Cookie("token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setSecure(IsSecure);
        cookie.setHttpOnly(true);
        // 线上 https：允许桌面端跨站携带登录态；本地 http 开发保持 Lax
        if (IsSecure) {
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setAttribute("SameSite", "Lax");
        }
        return cookie;
    }
}
