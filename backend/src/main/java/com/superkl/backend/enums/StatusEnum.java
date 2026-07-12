package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum StatusEnum {
    // 状态枚举
    ENABLE("ENABLE", "启用"),
    DISABLE("DISABLE", "禁用");

    // 属性
    private final String code;
    private final String description;
}
