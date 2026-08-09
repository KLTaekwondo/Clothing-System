package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberLevelEnum {
    COMMON("COMMON","普通用户"), // 0.88
    VIP("VIP", "普通会员"), //0.85 消费点数到10000（1000元）
    MVP("MVP","高级会员"),//0.80 消费点数到20000（2000元）
    CUSTOMER("CUSTOMER","自定义会员");//无法抵达，需要手动设置，折扣随机

    private final String code;
    private final String desc;
}
