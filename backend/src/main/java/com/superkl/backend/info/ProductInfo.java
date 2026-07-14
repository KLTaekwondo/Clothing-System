package com.superkl.backend.info;

import com.superkl.backend.enums.SeasonEnum;
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
public class ProductInfo extends BaseInfo {
    private String code; // 商品编码
    private String name; // 商品名称
    private SeasonEnum season; // 商品季节
    private BigDecimal importPrice; // 商品进口价格
    private BigDecimal salePrice; // 商品销售价格
    private boolean special; // 是否特价
    private StatusEnum status; // 商品状态
}
