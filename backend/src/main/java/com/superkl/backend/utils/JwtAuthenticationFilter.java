package com.superkl.backend.utils;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            // 告知Spring Security当前用户认证信息，用于后续权限校验
            // 这里将认证信息设置到 SecurityContextHolder 中，后续的权限校验会使用这个信息。
            // 之后在 Controller 里就可以通过 SecurityContextHolder.getContext().getAuthentication().getPrincipal() 拿到 userId，或者通过 @AuthenticationPrincipal 注解直接获取。
            UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BusinessException e) {
            // 未登录或 token 无效，不做处理，证明当前请求是不正常的，直接拒绝存入上下文，Spring Security会认为是匿名用户。
            // 直接抛出BusinessException异常，让后续过滤器处理，至于这个异常怎么来的，后面讲喽。
            // 建议加一句日志，记录下异常信息，方便调试。
            log.debug("JWT authentication failed: {}", e.getMessage());
        }
        // 继续执行后续过滤器
        chain.doFilter(request, response);
    }

    // 从请求中获取认证信息
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        RequestUser requestUser = AuthContext.getRequestUserFromCookie(request, jwtUtil);
        Long requestId = requestUser.getRequestId();
        String requestRole = requestUser.getRequestRole();
        // 第一个参数 userId：存的是“谁”（principal）。
        // 第二个参数 null：凭证（credentials），这里没有密码之类的，所以填 null。
        // 第三个参数：权限集合（authorities）。这里用了 Collections.singleton(() -> role)，实际上是一个 lambda 表达式实现的 GrantedAuthority，意思是这个用户只有一个角色，就是 role 字符串。
        // 这个 lambda 写法虽然能跑，但一般用 new SimpleGrantedAuthority(role) 更直观。
        return new UsernamePasswordAuthenticationToken(requestId, null, Collections.singleton(() -> requestRole));
    }
}
