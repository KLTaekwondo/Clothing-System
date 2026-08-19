package com.superkl.backend.info.dash;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDashInfo {
    private BigDecimal saleAmount;
    private BigDecimal importAmount;
    private BigDecimal marginRate;
    private BigDecimal profit;
}
