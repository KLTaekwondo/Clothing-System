package com.superkl.backend.dto.product;

import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StatusEnum;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "类型不能为空")
    @Size(min = 1, max = 20, message = "类型长度必须为1-20位")
    private String type;// 产品类型：男大童/女大童/男/女

    @NotBlank(message = "种类不能为空")
    @Size(min = 1, max = 20, message = "种类长度必须为1-20位")
    private String category;// 产品种类：裤子，衣服，T恤

    @NotBlank(message = "单位不能为空")
    @Size(min = 1, max = 20, message = "单位长度必须为1-20位")
    private String unit;// 衣服计数单位

    @NotBlank(message = "面料组合不能为空")
    @Size(min = 1, max = 20, message = "面料组合长度必须为1-20位")
    private String composition;// 面料组合

    @NotBlank(message = "年份不得为空")
    @Size(min = 4 ,max = 4,message = "年份长度必须为4")
    @Pattern(regexp = "\\d{4}",message = "年份长度必须是四位数！")
    private String year;

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
