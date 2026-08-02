package com.superkl.backend.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockCheckItemCreateDto {
    @NotBlank(message = "商品编码不能为空")
    private String skuCode;// 商品编码

    @NotNull(message = "商品数量不能为空")
    @Min(value = 0, message = "商品数量不能小于0")
    private Integer actualQuantity;// 商品数量
}
