package com.superkl.backend.dto;

import com.superkl.backend.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeUpdateDto {
    @NotBlank(message = "员工编码不能为空")
    @Size(min = 2, max = 10, message = "员工编码长度必须在2到10之间")
    private String code;// 员工编码

    @NotBlank(message = "员工姓名不能为空")
    @Size(min = 2, max = 10, message = "员工姓名长度必须在2到10之间")
    private String name;// 员工姓名

    @NotNull(message = "仓库ID不能为空")
    private Long wareHouseId;// 仓库ID(这里留一个可以转换仓库的接口，方便后续扩展)

    @NotNull(message = "员工状态不能为空")
    private StatusEnum status;// 员工状态
}
