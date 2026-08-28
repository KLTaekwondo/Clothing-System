package com.superkl.backend.info.dash;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutDashInfo {
    private BigDecimal sumAmount;// 选定区间的总额
    private BigDecimal AlipayAmount;// 选定区间的支付宝金额
    private BigDecimal CashAmount;// 选定区间的现金金额
    private BigDecimal CardAmount;// 选定区间的信用卡金额
    private BigDecimal WeChatAmount;// 选定区间的微信金额
    private BigDecimal TikTokWriteOffAmount;// 选定区间的抖音团购核销总额
}
