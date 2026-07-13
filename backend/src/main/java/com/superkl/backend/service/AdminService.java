package com.superkl.backend.service;

import com.superkl.backend.converter.AdminConverter;
import com.superkl.backend.dto.LoginDto;
import com.superkl.backend.dto.ResetDto;
import com.superkl.backend.entity.Admin;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.AdminInfo;
import com.superkl.backend.repository.AdminRepository;
import com.superkl.backend.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    // 创建Repository实体，用来查询管理员
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    //1.管理员登录
    public AdminInfo login(LoginDto dto, HttpServletResponse response){
        // 先提取出来，更加方便操作
        String account = dto.getAccount();
        String password = dto.getPassword();

        // 先查找管理员是否存在
        Admin admin = adminRepository.findByAccount(account).orElseThrow(() -> new BusinessException("账号不存在"));

        // 检查两部分是否匹配
        // 1. 密码是否匹配
        if(!passwordEncoder.matches(password,admin.getPassword())){
            throw new BusinessException("密码错误");
        }

        // 2. 账号状态是否启用
        if(!admin.getStatus().equals(StatusEnum.ENABLE)){
            throw new BusinessException("账号已禁用");
        }

        // 提前解析出来，避免调用过长
        Long adminId = admin.getAdminId();
        String code = admin.getAdminCode();
        String username = admin.getUsername();

        // 副属性处理
        Map<String,Object> claims = new HashMap<>();
        claims.put("code",code);
        claims.put("username",username);
        claims.put("role","ADMIN");

        // 3.登录成功，开始处理Cookie
        String token = jwtUtil.generateToken(adminId, claims);
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7*24*3600);
        // cookie.setSecure(true); //暂时不启用，因为本地开发环境不支持HTTPS
        response.addCookie(cookie);
        // 登录成功，返回管理员信息
        return AdminConverter.toInfo(admin);
    }

    // 2.管理员退出登录
    public void logout(HttpServletResponse response){
        Cookie cookie = new Cookie("token", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // 3.管理员重置密码
    @Transactional
    public void resetPassword(ResetDto resetDto){
        // 先提取出来，更加方便操作
        String account = resetDto.getAccount();
        String oldPassword = resetDto.getOldPassword();
        String newPassword = resetDto.getNewPassword();

        // 先查找管理员是否存在
        Admin admin = adminRepository.findByAccount(account).orElseThrow(() -> new BusinessException("账号不存在"));

        // 先编码新密码，再比较是否匹配
        String encodedOldPassword = passwordEncoder.encode(oldPassword);

        if(!passwordEncoder.matches(encodedOldPassword,admin.getPassword())){
            throw new BusinessException("旧密码错误");
        }

        // 新旧密码比较，不能相同
        if(passwordEncoder.matches(newPassword,oldPassword)){
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 保存新密码到数据库
        String newEncodedPassword = passwordEncoder.encode(newPassword);
        admin.setPassword(newEncodedPassword);
        adminRepository.save(admin);
    }
}
