package com.superkl.backend.info.dash;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayInfo {
    private LocalDate day;
    private BigDecimal amount;
}
