package com.superkl.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCreateDto {
    @NotBlank(message = "商品SKU编码不能为空")
    private String skuCode;// 商品SKU

    @NotNull(message = "商品折扣不得为空！")
    @DecimalMin(value = "0.00", message = "商品折扣不得小于0.00")
    @DecimalMax(value = "1.00", message = "商品折扣不得大于1.00")
    private BigDecimal discount;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不得小于1")
    private Integer quantity;// 商品数量
}
