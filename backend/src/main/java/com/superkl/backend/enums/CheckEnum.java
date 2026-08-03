package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CheckEnum {
    UNDER_CHECK("UNDER_CHECK","正在盘点"),
    NO_CHECK("NO_CHECK","未在盘点");

    private final String code;
    private final String desc;
}
