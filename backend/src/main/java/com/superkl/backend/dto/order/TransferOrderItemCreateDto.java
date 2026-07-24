package com.superkl.backend.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferOrderItemCreateDto {
    @NotBlank(message = "商品SKU编码不能为空")
    private String skuCode;// 商品SKU编码
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    private Integer quantity;// 商品数量
}
