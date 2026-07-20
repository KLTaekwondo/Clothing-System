package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImportOrderEnum {
    DRAFT("DRAFT", "草稿"),// 写好了保存，未提交
    CHECKING("CHECKING", "审核中"),// 提交了，未审核
    APPROVED("APPROVED", "已审批"),// 审核了
    REJECTED("REJECTED", "已拒绝");// 拒绝了

    private final String code;
    private final String desc;
}
