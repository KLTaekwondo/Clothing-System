package com.superkl.backend.utils;

import jakarta.servlet.http.Cookie;

public class TokenCookieManager {

    private TokenCookieManager() {}// 私有构造方法，防止外部实例化

    public static Cookie writeTokenCookie(String token ,boolean IsSecure) {
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(7 * 24 * 3600);
        cookie.setSecure(IsSecure);
        return cookie;
    }

    public static Cookie clearTokenCookie(boolean IsSecure) {
        Cookie cookie = new Cookie("token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setSecure(IsSecure);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
