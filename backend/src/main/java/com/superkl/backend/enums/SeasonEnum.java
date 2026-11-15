package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum SeasonEnum {
    SPRING("spring", "春款"),
    SUMMER("summer", "夏款"),
    AUTUMN("autumn", "秋款"),
    WINTER("winter", "冬款"),
    ALL_SEASONS("all-seasons","全季");

    private final String code;
    private final String desc;

    // 提取季节枚举值列表
    public static List<String> getSeasonList() {
        // 查询季节枚举值列表
        return Arrays.stream(SeasonEnum.values())
                .map(SeasonEnum::getDesc)
                .toList();
    }

    public static SeasonEnum seasonOf(String desc) {
        return Arrays.stream(SeasonEnum.values())
                .filter(s -> s.getDesc().equals(desc))
                .findFirst()
                .orElse(null);
    }
}
