package com.superkl.backend.converter.order;

import com.superkl.backend.dto.order.OrderItemCreateDto;
import com.superkl.backend.entity.order.OrderItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.info.order.OrderItemInfo;

import java.math.BigDecimal;
import java.util.List;

public class OrderItemConverter {
    private OrderItemConverter() {} // 私有构造函数，防止外部实例化

    // 实体转Info
    public static OrderItemInfo toInfo(OrderItem orderItem) {
        return OrderItemInfo.builder()
                .id(orderItem.getItemId())
                .productName(orderItem.getProductName())
                .productCode(orderItem.getProductCode())
                .skuName(orderItem.getSkuName())
                .skuCode(orderItem.getSkuCode())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .discount(orderItem.getDiscount())
                .totalPrice(orderItem.getTotalPrice())
                .actualPrice(orderItem.getActualPrice())
                .direction(orderItem.getDirection())
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
                .productCode(productSku.getProduct().getProductCode())
                .skuName(productSku.getSkuName())
                .skuCode(productSku.getSkuCode())
                .unitPrice(price)
                .discount(discount)
                .quantity(quantity.intValue())
                .totalPrice(totalPrice)
                .actualPrice(actualPrice)
                .build();
    }
}
