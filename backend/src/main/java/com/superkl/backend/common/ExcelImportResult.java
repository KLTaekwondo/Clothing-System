package com.superkl.backend.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.superkl.backend.dto.product.ExcelPreProductCreateDto;
import com.superkl.backend.enums.OptionTypeEnum;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class ExcelImportResult {
    private List<String> fatalErrors ; // 返回前端，严重错误
    private List<String> errors; // 返回前端，规格缺失错误
    @JsonIgnore
    private List<ExcelPreProductCreateDto> checkedDtos;

    @JsonIgnore
    private Map<OptionTypeEnum, Set<String>> notSeenOptions;

    public ExcelImportResult(){
        // 初始化+默认值
        this.fatalErrors = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.checkedDtos = new ArrayList<>();
        this.notSeenOptions = new EnumMap<>(OptionTypeEnum.class);

        this.notSeenOptions.put(OptionTypeEnum.COLOR, new HashSet<>());
        this.notSeenOptions.put(OptionTypeEnum.SIZE, new HashSet<>());
        this.notSeenOptions.put(OptionTypeEnum.TYPE, new HashSet<>());
        this.notSeenOptions.put(OptionTypeEnum.CATEGORY, new HashSet<>());
        this.notSeenOptions.put(OptionTypeEnum.UNIT, new HashSet<>());
        this.notSeenOptions.put(OptionTypeEnum.COMPOSITION, new HashSet<>());
        this.notSeenOptions.put(OptionTypeEnum.YEAR, new HashSet<>());
    }
}
