package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockChangeTypeEnum {
    SALE_OUT("SALE_OUT","销售出库"),
    SALE_RETURN("SALE_RETURN","销售退货"),
    IMPORT_IN("IMPORT_IN","采购入库"),
    IMPORT_RETURN("IMPORT_RETURN","采购退货"),
    TRANSFER_IN("TRANSFER_IN","调入"),
    TRANSFER_OUT("TRANSFER_OUT","调出"),
    MANUAL_ADJUST("MANUAL_ADJUST","手动操作"),
    STOCK_CHECK("STOCK_CHECK","盘点");

    private final String code;
    private final String desc;
}
