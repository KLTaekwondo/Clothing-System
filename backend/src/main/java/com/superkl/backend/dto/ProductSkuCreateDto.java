package com.superkl.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProductSkuCreateDto {
    @NotBlank(message = "SKU名称不能为空")
    private String name;
    @NotBlank(message = "SKU规格不能为空")
    private String spec;
    @NotNull(message = "产品ID不能为空")
    private Long productId;
}
