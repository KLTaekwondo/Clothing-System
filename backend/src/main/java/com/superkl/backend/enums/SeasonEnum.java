package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeasonEnum {
    SPRING("spring", "春款"),
    SUMMER("summer", "夏款"),
    AUTUMN("autumn", "秋款"),
    WINTER("winter", "冬款");

    private final String code;
    private final String desc;
}
