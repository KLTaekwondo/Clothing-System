package com.superkl.backend.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthRedisService {
    private final StringRedisTemplate redis;

    @Value("${auth.max-sessions}")
    private int maxSessions;

    // 将已经登陆的用户的token存入redis
    public void recordToken(String role, Long userId, String token,
                            Duration ttl){
        try{
            // 1. token 入集合，score = 当前毫秒时间戳（登录时间）
            //    分数越小 = 登录越早 = 越该被踢
            String key = sessionKey(role, userId);
            redis.opsForZSet().add(key, token, System.currentTimeMillis());

            // 2. 将整个的集合过期时间设置为ttl
            redis.expire(key, ttl);

            // 3. 数一下集合里有多少个会话
            Long count = redis.opsForZSet().zCard(key);
            if (count != null && count > maxSessions) {
                // 4. 超了：取出最旧的几个（按分数从小到大取前 N 个）
                Set<String> oldest = redis.opsForZSet().range(key, 0, count - maxSessions - 1);
                if (oldest != null) {
                    oldest.forEach(t -> {
                        redis.opsForZSet().remove(key, t);                  // 逐个删
                        redis.opsForValue().set("auth:blacklist:" + t, "1", ttl);  // 黑名单兜底
                    });
                }
            }
        }catch(Exception e){
            log.warn("REDIS 记录失败，异常信息：{}", e.getMessage());
        }

    }

    // 辅助方法，直接返回session key
    public String sessionKey(String role, Long userId) {
        return "auth:session:" + role + ":" + userId;
    }

    // 踢掉指定用户的指定token
    public void kick(String role, Long userId, String token) {
        try{
            redis.opsForZSet().remove(sessionKey(role, userId), token);
        }catch(Exception e){
            log.warn("REDIS 踢掉失败，异常信息：{}", e.getMessage());
        }
    }

    // 黑名单兜底：查 token 是否被拉黑（recordToken 里写过黑名单，这里要能查）
    public boolean isBlacklisted(String token) {
        try{
            return Boolean.TRUE.equals(redis.hasKey("auth:blacklist:" + token));
        }catch(Exception e){
            log.warn("REDIS 检查是否被拉黑失败，异常信息：{}", e.getMessage());
            return false;
        }
    }

    // 检查token是否是当前用户的token
    public boolean isCurrentToken(String role, Long userId, String token) {
        try{
            return redis.opsForZSet().score(sessionKey(role, userId), token) != null;
        }catch(Exception e){
            log.warn("REDIS 检查是否是当前用户的token失败，异常信息：{}", e.getMessage());
            return false;
        }
    }
}
