package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    DRAFT("DRAFT","挂单"),
    COMPLETED("COMPLETED","已完成"),
    REFUND("REFUND","已退款");
    // 订单状态枚举
    private final String code;
    private final String desc;
}
