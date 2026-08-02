package com.superkl.backend.dto.basic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierCreateDto {
    @NotBlank(message = "供应商名称不能为空")
    @Size(min = 2, max = 20, message = "供应商名称长度必须为2-20位")
    private String supplierName;
    @NotBlank(message = "供应商编码不能为空")
    @Size(min = 4, max = 20, message = "供应商编码长度必须为4-20位")
    private String supplierCode;


    private String contactPhone;
    @Size(max = 100,message = "备注长度不能超过100位")
    private String remark;
}
