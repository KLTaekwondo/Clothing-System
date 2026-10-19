// ② 切面
package com.superkl.backend.aspect;

import com.superkl.backend.annotation.LimitRate;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
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
        // 1. 拿真实 IP
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }

        String ip = attrs.getRequest().getRemoteAddr();
        Long userId = RequestUser.notNull().getRequestId();
        String userName = RequestUser.notNull().getRequestName();
        String cacheKey = PREFIX + userId;

        // 2. 原子自增（INCR 是原子的，比 get + set 安全）
        Long count = redisTemplate.opsForValue().increment(cacheKey);
        if (count == null) {
            return joinPoint.proceed();
        }

        // 3. 第一次访问设过期（否则 key 永不过期，用户被永久限流）
        if (count == 1) {
            redisTemplate.expire(cacheKey, limitRate.limitTime(), TimeUnit.SECONDS);
        }

        // 4. 超限就拒绝（抛异常，交给全局异常处理器）
        if (count > limitRate.limitCount()) {
            log.warn("触发限流：userId={}，userName={}，ip={}，当前次数={}，上限={}",
                              userId, userName, ip, count, limitRate.limitCount());
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }
}