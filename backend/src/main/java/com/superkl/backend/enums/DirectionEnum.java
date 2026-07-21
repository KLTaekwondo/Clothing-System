package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DirectionEnum {
    IN("IN", "正向"),
    OUT("OUT", "反向");

    private final String code;
    private final String desc;
}
