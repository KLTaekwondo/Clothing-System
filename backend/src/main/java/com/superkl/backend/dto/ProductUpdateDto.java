package com.superkl.backend.dto;

import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDto {
    @NotBlank(message = "商品编码不能为空")
    @Size(min = 1, max = 20, message = "商品编码长度必须为1-20位")
    private String code;// 商品编码
    @NotBlank(message = "商品名称不能为空")
    @Size(min = 1, max = 20, message = "商品名称长度必须为1-20位")
    private String name;// 商品名称
    @NotNull(message = "季节不能为空")
    private SeasonEnum season;// 季节
    @NotNull(message = "进货价格不能为空")
    @DecimalMin(value = "0.00", message = "传入的进货价格不得小于0")
    private BigDecimal importPrice;// 进货价格
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.00", message = "传入的销售价格不得小于0")
    private BigDecimal salePrice;// 销售价格
    @NotNull(message = "是否特价不能为空")
    private boolean special;// 是否特价
    @NotNull(message = "状态不能为空")
    private StatusEnum status;// 状态
}
