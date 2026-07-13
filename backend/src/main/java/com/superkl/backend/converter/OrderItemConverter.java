package com.superkl.backend.converter;

import com.superkl.backend.dto.OrderItemCreateDto;
import com.superkl.backend.entity.OrderItem;
import com.superkl.backend.entity.ProductSku;
import com.superkl.backend.info.OrderItemInfo;

import java.math.BigDecimal;
import java.util.List;

public class OrderItemConverter {
    private OrderItemConverter() {} // 私有构造函数，防止外部实例化

    // 实体转Info
    public static OrderItemInfo toInfo(OrderItem orderItem) {
        return OrderItemInfo.builder()
                .itemId(orderItem.getItemId())
                .productName(orderItem.getProductName())
                .skuName(orderItem.getSkuName())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .discount(orderItem.getDiscount())
                .totalPrice(orderItem.getTotalPrice())
                .actualPrice(orderItem.getActualPrice())
                .createTime(orderItem.getCreateTime())
                .updateTime(orderItem.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<OrderItemInfo> toInfoList(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemConverter::toInfo)
                .toList();
    }


    // dto转实体
    public static OrderItem toEntity(OrderItemCreateDto dto, ProductSku productSku){
        BigDecimal price = productSku.getProduct().getSalePrice();
        BigDecimal quantity = BigDecimal.valueOf(dto.getQuantity());
        BigDecimal discount = dto.getDiscount();
        BigDecimal totalPrice = price.multiply(quantity);
        BigDecimal actualPrice = totalPrice.multiply(discount);

        return OrderItem.builder()
                .skuId(productSku.getSkuId())
                .productName(productSku.getProduct().getProductName())
                .skuName(productSku.getSkuName())
                .unitPrice(price)
                .discount(discount)
                .quantity(quantity.intValue())
                .totalPrice(totalPrice)
                .actualPrice(actualPrice)
                .build();
    }
}
