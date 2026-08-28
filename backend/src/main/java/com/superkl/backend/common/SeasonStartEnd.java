package com.superkl.backend.common;

import com.superkl.backend.enums.SeasonEnum;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
// 中间类，用于构建SeasonStartEnd对象
public class SeasonStartEnd {
    private LocalDate start;
    private LocalDate end;
    private SeasonEnum season;
}
