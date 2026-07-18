package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImportOrderEnum {
    DRAFT("DRAFT", "草稿"),
    CHECKING("CHECKING", "审核中"),
    APPROVED("APPROVED", "已审批"),
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String desc;
}
