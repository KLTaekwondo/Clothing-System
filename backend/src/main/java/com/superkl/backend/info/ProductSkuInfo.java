package com.superkl.backend.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProductSkuInfo extends BaseInfo {
    private String code; // 商品SKU编码
    private String name; // 商品SKU名称
    private String spec; // 商品SKU规格
}
