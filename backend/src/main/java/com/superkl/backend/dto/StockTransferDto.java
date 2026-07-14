package com.superkl.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockTransferDto {
    @NotNull(message = "商品SKU编码不能为空")
    private Long skuId;
    @NotNull(message = "目标仓库ID不能为空")
    private Long targetWarehouseId;
    @NotNull(message = "源仓库ID不能为空")
    private Long sourceWarehouseId;
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量不得小于1")
    private Integer stock;
}
