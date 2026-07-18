package com.superkl.backend.converter;

import com.superkl.backend.entity.ImportOrder;
import com.superkl.backend.entity.ImportOrderItem;
import com.superkl.backend.info.ImportOrderInfo;
import com.superkl.backend.info.ImportOrderItemInfo;

import java.util.List;

public class ImportOrderItemConverter {
    private ImportOrderItemConverter() {}// 私有构造方法,防止外部实例化

    // 实体转Info
    public static ImportOrderItemInfo toInfo(ImportOrderItem importOrderItem) {
        return ImportOrderItemInfo.builder()
                .id(importOrderItem.getImportItemId())
                .productName(importOrderItem.getProductName())
                .skuName(importOrderItem.getSkuName())
                .importPrice(importOrderItem.getImportPrice())
                .quantity(importOrderItem.getQuantity())
                .totalPrice(importOrderItem.getTotalPrice())
                .build();
    }

    // 实体列表转Info列表
    public static List<ImportOrderItemInfo> toInfoList(List<ImportOrderItem> importOrderItems) {
        return importOrderItems.stream()
                .map(ImportOrderItemConverter::toInfo)
                .toList();
    }
}
