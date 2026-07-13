package com.superkl.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCreateDto {
    @NotNull(message = "商品SKU编码不能为空")
    private String skuCode;// 商品SKU

    @NotNull(message = "商品折扣不得为空！")
    private BigDecimal discount;

    @NotNull(message = "商品数量不能为空")
    private Integer quantity;// 商品数量
}
