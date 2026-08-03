package com.superkl.backend.info.product;

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
public class ProductSkuCheckInfo extends ProductSkuInfo{
    private String productCode; // 商品编码
    private String productName; // 商品名称
    private BigDecimal salePrice;// 商品SKU价格
    private boolean special;// 是否特价
}
