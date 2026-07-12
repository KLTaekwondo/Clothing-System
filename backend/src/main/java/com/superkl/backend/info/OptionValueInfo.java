package com.superkl.backend.info;

import com.superkl.backend.enums.OptionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OptionValueInfo extends BaseInfo {
    private OptionTypeEnum optionType;
    private String optionValue;
}
