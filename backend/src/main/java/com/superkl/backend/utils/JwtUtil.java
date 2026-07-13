package com.superkl.backend.utils;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // 先创建密钥
    private SecretKey getSecretKey() {
        // 先转化成UTF-8编码
        byte[] keyBytes = jwtKey.getBytes(StandardCharsets.UTF_8);

        // 检查密钥长度是否大于32位
        if(keyBytes.length <= 32) {
            throw new BusinessException("JWT密钥长度必须大于32位");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    };

    // 创建token , 包含管理员ID , 状态 , 用户名
    public String generateToken(Long adminId ,Map<String,Object> claims) {
        // 以管理员ID作为Token的主体
        String subject = adminId.toString();
        // 处理过期时间
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);

        // 创建Token
        // 1.设置主体
        // 2. 设置副属性
        // 3. 设置过期时间
        // 4. 签名
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();

    }

    // 验证token
    public boolean validateToken(String token) {
        try{
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
        }catch(Exception e){
            throw new BusinessException("token无效");
        }

        return true;
    }

    // 解析管理员Id,其他的不解析了，不是说不重要，只是没有那个必要，因为管理员ID是唯一的，所以只需要解析管理员ID即可。
    public RequestUser parseRequestUser(String token) {
        try{
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long requestId = Long.parseLong(claims.getSubject());
            String requestRole = "ROLE_" + claims.get("role").toString();

            return RequestUser.builder()
                    .requestId(requestId)
                    .requestRole(requestRole)
                    .build();
        }catch(Exception e){
            throw new BusinessException("token无效");
        }
    }
}
