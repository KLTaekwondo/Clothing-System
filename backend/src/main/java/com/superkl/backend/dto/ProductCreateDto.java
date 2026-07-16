package com.superkl.backend.dto;

import com.superkl.backend.enums.SeasonEnum;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductCreateDto {
    @NotBlank(message = "商品编码不能为空")
    @Size(min = 1, max = 20, message = "商品编码长度必须为1-20位")
    private String code;
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 1, max = 20, message = "商品名称长度必须为1-20位")
    private String name;
    @NotNull(message = "季节不能为空")
    private SeasonEnum season;
    @NotBlank(message = "类型不能为空")
    @Size(min = 1, max = 20, message = "类型长度必须为1-20位")
    private String type;
    @NotBlank(message = "种类不能为空")
    @Size(min = 1, max = 20, message = "种类长度必须为1-20位")
    private String category;
    @NotBlank(message = "单位不能为空")
    @Size(min = 1, max = 20, message = "单位长度必须为1-20位")
    private String unit;
    @NotBlank(message = "面料组合不能为空")
    @Size(min = 1, max = 20, message = "面料组合长度必须为1-20位")
    private String composition;
    @NotNull(message = "进货价格不能为空")
    @DecimalMin(value = "0.00", message = "进货价格不得小于0")
    private BigDecimal importPrice;
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.00", message = "销售价格不得小于0")
    private BigDecimal salePrice;
    @NotNull(message = "是否特价不能为空")
    private boolean special;

    @NotNull(message = "选中的选项不能为空")
    private Map<String , List<String>> selectedOptions;
}
