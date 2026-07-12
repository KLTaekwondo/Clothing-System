package com.superkl.backend.dto;

import com.superkl.backend.enums.OptionTypeEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OptionValueUpdateDto {
    @NotNull(message = "选项类型不能为空")
    private OptionTypeEnum optionType;
    @NotNull(message = "选项值不能为空")
    @Size(min = 1, max = 20, message = "选项值长度必须在1-20之间")
    private String optionValue;
}
