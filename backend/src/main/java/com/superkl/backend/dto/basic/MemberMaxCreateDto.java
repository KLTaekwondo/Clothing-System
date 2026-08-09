package com.superkl.backend.dto.basic;

import com.superkl.backend.enums.MemberLevelEnum;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberMaxCreateDto{
    @NotBlank(message = "会员姓名不能为空")
    private String name;

    @NotBlank(message = "会员手机号不能为空")
    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "会员手机号格式错误")
    private String phone;

    @NotNull(message = "会员生日不能为空")
    private LocalDate birthday;

    @NotNull(message = "会员等级不能为空")
    private MemberLevelEnum level;

    @NotNull(message = "会员积分点不能为空")
    @Min(value = 0, message = "会员积分点不能小于0")
    private Long points;

    @NotNull(message = "会员等级点不能为空")
    @Min(value = 0, message = "会员等级点不能小于0")
    private Long levelPoints;

    @NotNull(message = "会员折扣不能为空")
    @DecimalMin(value = "0.00", message = "会员折扣不能小于0")
    @DecimalMax(value = "1.00", message = "会员折扣不能大于1")
    private BigDecimal discount;
}
