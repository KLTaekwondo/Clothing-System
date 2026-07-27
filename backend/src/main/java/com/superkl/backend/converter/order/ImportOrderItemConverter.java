package com.superkl.backend.converter.order;

import com.superkl.backend.dto.order.ImportOrderItemCreateDto;
import com.superkl.backend.entity.order.ImportOrderItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.info.order.ImportOrderItemInfo;

import java.math.BigDecimal;
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
                .createTime(importOrderItem.getCreateTime())
                .updateTime(importOrderItem.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<ImportOrderItemInfo> toInfoList(List<ImportOrderItem> importOrderItems) {
        return importOrderItems.stream()
                .map(ImportOrderItemConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static ImportOrderItem toEntity(ImportOrderItemCreateDto dto, ProductSku productSku) {
        BigDecimal importPrice = productSku.getProduct().getImportPrice();
        BigDecimal importQuantity = BigDecimal.valueOf(dto.getImportQuantity());
        BigDecimal totalPrice = importPrice.multiply(importQuantity);
        return ImportOrderItem.builder()
                .skuId(productSku.getSkuId())
                .productName(productSku.getProduct().getProductName())
                .skuName(productSku.getSkuName())
                .importPrice(importPrice)
                .quantity(importQuantity.intValue())
                .totalPrice(totalPrice)
                .build();
    }
}
