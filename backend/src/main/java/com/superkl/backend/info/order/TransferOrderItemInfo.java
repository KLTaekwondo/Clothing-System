package com.superkl.backend.info.order;

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
public class TransferOrderItemInfo extends BaseInfo {
    private Long skuId;
    private String productName;
    private String productCode;
    private String skuName;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
