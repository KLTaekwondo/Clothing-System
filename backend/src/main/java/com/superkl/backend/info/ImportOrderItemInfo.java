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
public class ImportOrderItemInfo extends BaseInfo {
    private String productName;
    private String skuName;
    private BigDecimal importPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
