package com.superkl.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransferOrderDraftDto {
    @NotNull(message = "源仓库ID不能为空")
    private Long sourceWareHouseId;// 源仓库ID
    @NotNull(message = "目标仓库ID不能为空")
    private Long targetWareHouseId;// 目标仓库ID

    @NotEmpty(message = "转移订单项列表不能为空")
    private List<@Valid TransferOrderItemCreateDto> transferOrderItems;// 转移订单项列表

    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.00", message = "总金额不能小于0.00")
    private BigDecimal totalPrice;// 总金额

    @Size(max = 100, message = "备注不能超过100个字符")
    private String remark;// 备注
}
