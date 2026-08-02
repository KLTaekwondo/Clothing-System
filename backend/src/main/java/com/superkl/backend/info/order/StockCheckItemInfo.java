package com.superkl.backend.info.order;

import com.superkl.backend.info.BaseInfo;
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
public class StockCheckItemInfo extends BaseInfo {
    private String skuName;
    private String skuCode;
    private String productName;
    private String productCode;
    private Integer actualQuantity;
    private Integer systemQuantity;
    private Integer diffQuantity;
}
