package com.superkl.backend.dto;

import lombok.Data;

@Data
public class StockTransferDto {
    private Long skuId;
    private Long targetWarehouseId;
    private Long sourceWarehouseId;
    private Integer stock;
}
