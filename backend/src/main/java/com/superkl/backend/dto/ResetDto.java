package com.superkl.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetDto {
    @NotBlank(message = "账号不能为空")
    private String account;
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
