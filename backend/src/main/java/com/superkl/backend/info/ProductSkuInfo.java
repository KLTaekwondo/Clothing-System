package com.superkl.backend.info;

import com.superkl.backend.enums.StatusEnum;
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
public class ProductSkuInfo extends BaseInfo {
    private String code; // 商品SKU编码
    private String name; // 商品SKU名称
    private String spec; // 商品SKU规格
    private StatusEnum status; // 商品SKU状态
}
