package com.superkl.backend.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrderItemInfo extends BaseInfo{
    private Long itemId;
    private String productName;
    private String skuName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private BigDecimal actualPrice;
}
