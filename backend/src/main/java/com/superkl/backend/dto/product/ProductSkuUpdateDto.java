package com.superkl.backend.dto.product;

import com.superkl.backend.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSkuUpdateDto {
    @NotBlank(message = "SKU名称不能为空")
    private String name; // 商品SKU名称
    @NotBlank(message = "SKU规格不能为空")
    private String spec; // 商品SKU规格
    @NotNull(message = "状态不能为空")
    private StatusEnum status;// 状态
}
