package com.superkl.backend.service.basic;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.basic.AdminConverter;
import com.superkl.backend.dto.basic.LoginDto;
import com.superkl.backend.dto.basic.ResetDto;
import com.superkl.backend.entity.basic.Admin;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.basic.AdminInfo;
import com.superkl.backend.repository.basic.AdminRepository;
import com.superkl.backend.utils.JwtUtil;
import com.superkl.backend.utils.TokenCookieManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    // 创建Repository实体，用来查询管理员
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.cookie.secure}")
    private boolean isSecure;

    //1.管理员登录
    public AdminInfo login(LoginDto dto, HttpServletResponse response) {
        // 先提取出来，更加方便操作
        String account = dto.getAccount();
        String password = dto.getPassword();

        // 先查找管理员是否存在
        Admin admin = adminRepository.findByAccount(account)
                .orElseThrow(() -> new BusinessException(403, "账号不存在"));

        // 检查两部分是否匹配
        // 1. 密码是否匹配
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 2. 账号状态是否启用
        if (!admin.isEnabled()) {
            throw new BusinessException(405, "账号已禁用");
        }

        // 提前解析出来，避免调用过长
        Long adminId = admin.getAdminId();
        String code = admin.getAdminCode();
        String username = admin.getUsername();

        // 副属性处理
        Map<String, Object> claims = new HashMap<>();
        claims.put("code", code);
        claims.put("username", username);
        claims.put("role", "ADMIN");

        // 3.登录成功，开始处理Cookie
        String token = jwtUtil.generateToken(adminId, claims);
        Cookie cookie = TokenCookieManager.writeTokenCookie(token, isSecure);
        response.addCookie(cookie);
        log.info("管理员登录成功：{}", account);
        return AdminConverter.toInfo(admin);
    }

    // 2.管理员退出登录
    public void logout(HttpServletResponse response) {
        Cookie cookie = TokenCookieManager.clearTokenCookie(isSecure);
        response.addCookie(cookie);
        log.info("管理员退出登录");
        RequestUser.log();
    }

    // 3.管理员重置密码
    @Transactional
    public void resetPassword(ResetDto resetDto) {
        // 先提取出来，更加方便操作
        Long account = RequestUser.notNull().getRequestId();
        String oldPassword = resetDto.getOldPassword();
        String newPassword = resetDto.getNewPassword();

        // 先查找管理员是否存在
        Admin admin = adminRepository.findById(account)
                .orElseThrow(() -> new BusinessException(403, "账号不存在"));

        // 先编码新密码，再比较是否匹配
        String adminPassword = admin.getPassword();

        if (!passwordEncoder.matches(oldPassword, adminPassword)) {
            throw new BusinessException("旧密码错误");
        }

        // 新旧密码比较，不能相同
        if (passwordEncoder.matches(newPassword, adminPassword)) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 保存新密码到数据库
        String newEncodedPassword = passwordEncoder.encode(newPassword);
        admin.setPassword(newEncodedPassword);
        adminRepository.save(admin);
        log.info("管理员密码已重置：{}", account);
        RequestUser.log();
    }
}