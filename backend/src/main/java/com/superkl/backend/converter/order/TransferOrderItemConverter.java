package com.superkl.backend.converter.order;

import com.superkl.backend.dto.order.TransferOrderItemCreateDto;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.order.TransferOrderItem;
import com.superkl.backend.info.order.TransferOrderItemInfo;

import java.math.BigDecimal;
import java.util.List;

public class TransferOrderItemConverter {
    private TransferOrderItemConverter() {} // 私有构造函数，防止外部实例化

    // 实体转Info
    public static TransferOrderItemInfo toInfo(TransferOrderItem transferOrderItem) {
        return TransferOrderItemInfo.builder()
                .id(transferOrderItem.getTransferItemId())
                .skuId(transferOrderItem.getSkuId())
                .productName(transferOrderItem.getProductName())
                .productCode(transferOrderItem.getProductCode())
                .skuName(transferOrderItem.getSkuName())
                .skuCode(transferOrderItem.getSkuCode())
                .price(transferOrderItem.getPrice())
                .quantity(transferOrderItem.getQuantity())
                .totalPrice(transferOrderItem.getItemTotalPrice())
                .createTime(transferOrderItem.getCreateTime())
                .updateTime(transferOrderItem.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<TransferOrderItemInfo> toInfoList(List<TransferOrderItem> transferOrderItems) {
        return transferOrderItems.stream()
                .map(TransferOrderItemConverter::toInfo)
                .toList();
    }

    // dto 转换为实体
    public static TransferOrderItem toEntity(TransferOrderItemCreateDto dto, ProductSku productSku) {
        String productName = productSku.getProduct().getProductName();
        BigDecimal price = productSku.getProduct().getImportPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(dto.getQuantity()));
        return TransferOrderItem.builder()
                .skuId(productSku.getSkuId())
                .productName(productName)
                .productCode(productSku.getProduct().getProductCode())
                .skuCode(productSku.getSkuCode())
                .skuName(productSku.getSkuName())
                .price(price)
                .quantity(dto.getQuantity())
                .itemTotalPrice(totalPrice)
                .build();
    }
}
