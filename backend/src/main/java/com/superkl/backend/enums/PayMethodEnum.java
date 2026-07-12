package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayMethodEnum {
    CASH("CASH","现金"),
    CARD("CARD","信用卡"),
    ALIPAY("ALIPAY","支付宝"),
    WECHAT("WECHAT","微信");

    private final String code;
    private final String desc;
}
