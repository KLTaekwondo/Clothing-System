package com.superkl.backend.converter;

import com.superkl.backend.entity.ImportOrder;
import com.superkl.backend.entity.ImportOrderItem;
import com.superkl.backend.info.ImportOrderInfo;

import java.util.List;

public class ImportOrderConverter {
    private ImportOrderConverter() {}// 私有构造方法,防止外部实例化

    // 实体转Info
    public static ImportOrderInfo toInfo(ImportOrder importOrder) {
        return ImportOrderInfo.builder()
                .id(importOrder.getImportOrderId())
                .importOrderNo(importOrder.getImportOrderNo())
                .supplierName(importOrder.getSupplierName())
                .remark(importOrder.getRemark())
                .totalAmount(importOrder.getTotalAmount())
                .importOrderEnum(importOrder.getImportOrderEnum())
                .build();
    }

    // 实体列表转Info列表
    public static List<ImportOrderInfo> toInfoList(List<ImportOrder> importOrders) {
        return importOrders.stream()
                .map(ImportOrderConverter::toInfo)
                .toList();
    }
}
