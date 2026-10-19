package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KindOfDirEnum {
    PRODUCT("product", "商品目录");

    private final String dir;
    private final String name;
}
