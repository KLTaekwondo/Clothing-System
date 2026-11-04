package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum MemberLevelEnum {
    COMMON("COMMON","普通用户", null, BigDecimal.valueOf(0.85)), // 0.85
    VIP("VIP", "普通会员", 50000L, BigDecimal.valueOf(0.83)), //0.83 消费点数到50000（500元）
    MVP("MVP","高级会员", 500000L, BigDecimal.valueOf(0.80)),//0.80 消费点数到500000（5000元）
    CUSTOMER("CUSTOMER","自定义会员",null,null);//无法抵达，需要手动设置，折扣随机

    private final String code;
    private final String desc;
    private final Long levelPoints;
    private final BigDecimal discount;
}
