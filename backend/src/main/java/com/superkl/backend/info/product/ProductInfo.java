package com.superkl.backend.info.product;

import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.info.BaseInfo;
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
    private String type; // 商品类型
    private String category; // 商品种类
    private String unit; // 商品单位
    private String composition; // 商品面料组合
    private String year; // 商品年份
    private BigDecimal importPrice; // 商品进口价格
    private BigDecimal salePrice; // 商品销售价格
    private boolean special; // 是否特价
    private StatusEnum status; // 商品状态
}
