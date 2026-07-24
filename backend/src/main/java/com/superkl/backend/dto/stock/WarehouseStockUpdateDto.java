package com.superkl.backend.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseStockUpdateDto {
    @NotNull(message = "库存Id不能为空！")
    private Long stockId;

    @NotNull(message = "库存数量不能为空！")
    @Min(value = 0,message = "库存数量不得少于0！")
    private Integer stock;
}
