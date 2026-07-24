package com.superkl.backend.dto.basic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank(message = "账号不能为空")
    private String account;// 账号
    @NotBlank(message = "密码不能为空")
    private String password;// 密码
}
