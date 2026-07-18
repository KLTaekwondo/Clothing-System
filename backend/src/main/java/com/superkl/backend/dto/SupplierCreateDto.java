package com.superkl.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierCreateDto {
    @NotBlank(message = "供应商名称不能为空")
    @Size(min = 1, max = 20, message = "供应商名称长度必须为1-20位")
    private String supplierName;
    @NotBlank(message = "供应商编码不能为空")
    @Size(min = 1, max = 20, message = "供应商编码长度必须为1-20位")
    private String supplierCode;


    private String contactPhone;
    private String remark;

    @NotNull(message = "管理员ID不能为空")
    @Size(min = 1, max = 20, message = "管理员ID长度必须为1-20位")
    private Long adminId;
}
