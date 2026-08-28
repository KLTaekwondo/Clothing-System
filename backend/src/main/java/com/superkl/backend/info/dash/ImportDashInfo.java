package com.superkl.backend.info.dash;

import com.superkl.backend.enums.SeasonEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportDashInfo {
    private SeasonEnum season;
    private BigDecimal amount;
    private Long quantity;
}
