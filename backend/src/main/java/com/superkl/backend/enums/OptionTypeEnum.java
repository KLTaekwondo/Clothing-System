package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OptionTypeEnum {
    COLOR("color", "颜色"),// 颜色
    SIZE("size", "尺码"),// 110-150这种尺码
    TYPE("type", "类型"),// 男大童/女大童/男/女
    CATEGORY("category", "种类"),// 裤子，衣服，T恤
    UNIT("unit", "单位"),// 衣服计数单位
    COMPOSITION("composition", "面料组合"),// 组合
    YEAR("year","年份");

    private final String code;
    private final String desc;
}
