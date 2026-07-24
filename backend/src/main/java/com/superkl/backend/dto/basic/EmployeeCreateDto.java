package com.superkl.backend.dto.basic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeCreateDto {
    @NotBlank(message = "员工编码不能为空")
    @Size(min = 2, max = 10, message = "员工编码长度必须在2到10之间")
    private String code;

    @NotBlank(message = "员工姓名不能为空")
    @Size(min = 2, max = 10, message = "员工姓名长度必须在2到10之间")
    private String name;

    @NotNull(message = "仓库ID不能为空")
    private Long wareHouseId;
}
