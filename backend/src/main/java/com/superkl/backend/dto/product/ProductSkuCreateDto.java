package com.superkl.backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSkuCreateDto {
    @NotBlank(message = "SKU名称不能为空")
    private String name;
    @NotBlank(message = "SKU规格不能为空")
    private String spec;
    @NotNull(message = "产品ID不能为空")
    private Long productId;
}
