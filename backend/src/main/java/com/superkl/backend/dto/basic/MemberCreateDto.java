package com.superkl.backend.dto.basic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
// 收银界面临时创建会员的Dto
public class MemberCreateDto {
    @NotBlank(message = "会员姓名不能为空")
    private String name;
    @NotBlank(message = "会员手机号不能为空")
    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "会员手机号格式错误")
    private String phone;
    @NotNull(message = "会员生日不能为空")
    private LocalDate birthday;
}
