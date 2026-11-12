// ② 切面
package com.superkl.backend.aspect;

import com.superkl.backend.annotation.LimitRate;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LimitRateAspect {
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "clothing:limit_rate:";   // 项目前缀，避免和别的项目串

    @Around("@annotation(limitRate)")
    public Object aroundLimitRate(ProceedingJoinPoint joinPoint, LimitRate limitRate) throws Throwable {
        // 1. 没有请求上下文（定时任务、启动过程）直接放行
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attrs.getRequest();
        String ip = resolveIp(request);

        // 2. 有登录态按用户限流；没有（比如登录接口）按 IP 限流
        String subject;
        String label;
        RequestUser user = currentUserOrNull();
        if (user != null) {
            subject = "user:" + user.getRequestId();
            label = user.getRequestName() + "(ID=" + user.getRequestId() + ")";
        } else {
            subject = "ip:" + ip;
            label = "未登录";
        }

        // 3. key 里带上方法名，避免不同接口共用一个计数器
        String cacheKey = PREFIX + subject + ":" + joinPoint.getSignature().toShortString();

        // 4. 原子自增；Redis 挂了就放行，不能因为限流器故障把业务挡死
        Long count;
        try {
            count = redisTemplate.opsForValue().increment(cacheKey);
        } catch (Exception e) {
            log.warn("限流器不可用，放行本次请求：{}", e.getMessage());
            return joinPoint.proceed();
        }
        if (count == null) {
            return joinPoint.proceed();
        }

        // 5. 第一次访问设过期，否则 key 永不过期，用户被永久限流
        if (count == 1) {
            redisTemplate.expire(cacheKey, limitRate.limitTime(), TimeUnit.SECONDS);
        }

        // 6. 超限就拒绝（抛异常，交给全局异常处理器）
        if (count > limitRate.limitCount()) {
            log.warn("触发限流：{}，IP={}，方法={}，当前次数={}，上限={}",
                    label, ip, joinPoint.getSignature().toShortString(),
                    count, limitRate.limitCount());
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    // 没有登录态时返回 null，而不是抛异常
    private RequestUser currentUserOrNull() {
        try {
            return RequestUser.notNull();
        } catch (Exception e) {
            return null;
        }
    }

    // nginx 反代后 getRemoteAddr 永远是 127.0.0.1，优先取转发头
    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
