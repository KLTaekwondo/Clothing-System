package com.superkl.backend.controller;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.common.Result;
import com.superkl.backend.dto.LoginDto;
import com.superkl.backend.dto.ResetDto;
import com.superkl.backend.info.AdminInfo;
import com.superkl.backend.service.AdminService;
import com.superkl.backend.utils.AuthContext;
import com.superkl.backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // 1.管理员登录
    @PostMapping("/login")
    public Result<AdminInfo> login(@Valid @RequestBody LoginDto loginDto , HttpServletResponse response) {
        return Result.success(adminService.login(loginDto,response));
    }

    // 2. 管理员退出登录
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        adminService.logout(response);
        return Result.successMessage("退出登录成功！");
    }

    // 3.管理员修改密码
    @PutMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetDto resetDto) {
        adminService.resetPassword(resetDto);
        return Result.successMessage("重置密码成功！");
    }
}
