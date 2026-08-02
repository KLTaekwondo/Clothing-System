package com.superkl.backend.dto.basic;

import com.superkl.backend.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierUpdateDto {
    @NotBlank(message = "供应商编码不能为空")
    @Size(min = 4, max = 20, message = "供应商编码长度必须在4到20之间")
    private String supplierCode;
    @NotBlank(message = "供应商名称不能为空")
    @Size(min = 2, max = 20, message = "供应商名称长度必须在2到20之间")
    private String supplierName;

    private String contactPhone;
    @Size(max = 100,message = "备注长度不能超过100位")
    private String remark;

    @NotNull(message = "供应商状态不能为空")
    private StatusEnum status;
}
