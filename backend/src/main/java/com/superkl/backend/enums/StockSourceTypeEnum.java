package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockSourceTypeEnum {
    ORDER("ORDER","销售订单"),
    IMPORT_ORDER("IMPORT_ORDER","采购订单"),
    TRANSFER_ORDER("TRANSFER_ORDER","调拨订单"),
    MANUAL_ADJUST("MANUAL_ADJUST","手动操作");

    private final String code;
    private final String desc;
}
