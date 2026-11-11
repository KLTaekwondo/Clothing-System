package com.superkl.backend.aspect;

import com.superkl.backend.annotation.OpLogs;
import com.superkl.backend.common.RequestUser;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OpLogsAspect {
    @Around("@annotation(opLogs)")
    public Object around(ProceedingJoinPoint joinPoint, OpLogs opLogs) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        String error = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            success = false;
            error = e.getMessage();
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            String operator;
            try {
                operator = RequestUser.notNull().getRequestName();
            } catch (Exception ignored) {
                operator = "未登录";
            }
            log.info("OPLOG | 模块：{} | 描述：{} | 方法：{} | 操作人：{} | 消耗时间：{}ms | 结果：{}",
                    opLogs.module(),
                    opLogs.description(),
                    joinPoint.getSignature().toShortString(),
                    operator,
                    cost,
                    success ? "成功" : "失败：" + error);
        }
    }
}