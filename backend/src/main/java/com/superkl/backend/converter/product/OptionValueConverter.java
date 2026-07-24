package com.superkl.backend.converter.product;

import com.superkl.backend.dto.product.OptionValueCreateDto;
import com.superkl.backend.dto.product.OptionValueUpdateDto;
import com.superkl.backend.entity.product.OptionValue;
import com.superkl.backend.info.poduct.OptionValueInfo;

import java.util.List;

public class OptionValueConverter {
    private OptionValueConverter() {
    } // 私有构造函数，防止外部实例化

    //实体转info
    public static OptionValueInfo toInfo(OptionValue optionValue) {
        return OptionValueInfo.builder()
                .id(optionValue.getOptionValueId())
                .optionType(optionValue.getOptionType())
                .optionValue(optionValue.getOptionValue())
                .createTime(optionValue.getCreateTime())
                .updateTime(optionValue.getUpdateTime())
                .build();
    }

    // 实体列表转info列表
    public static List<OptionValueInfo> toInfoList(List<OptionValue> optionValues) {
        return optionValues.stream()
                .map(OptionValueConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static OptionValue toEntity(OptionValueCreateDto optionValueCreateDto) {
        return OptionValue.builder()
                .optionType(optionValueCreateDto.getOptionType())
                .optionValue(optionValueCreateDto.getOptionValue())
                .build();
    }

    // dto转更新实体
    public static void updateEntity(OptionValue optionValue, OptionValueUpdateDto optionValueUpdateDto) {
        if (optionValueUpdateDto.getOptionType() != null) {
            optionValue.setOptionType(optionValueUpdateDto.getOptionType());
        }
        if (optionValueUpdateDto.getOptionValue() != null) {
            optionValue.setOptionValue(optionValueUpdateDto.getOptionValue());
        }
    }
}
