package com.superkl.backend.dto;

import com.superkl.backend.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WareHouseUpdateDto {
    @NotBlank(message = "仓库名称不能为空")
    @Size(min = 2, max = 10, message = "仓库名称长度必须在2到10之间")
    private String name;// 仓库名称

    @NotBlank(message = "仓库密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{6,12}$", message = "仓库密码长度必须在6到12之间")
    private String password;// 仓库密码

    @NotBlank(message = "仓库编码不能为空")
    @Size(min = 2, max = 10, message = "仓库编码长度必须在2到10之间")
    private String code;// 仓库编码

    @NotNull(message = "仓库状态不能为空")
    private StatusEnum status;// 仓库状态
}
