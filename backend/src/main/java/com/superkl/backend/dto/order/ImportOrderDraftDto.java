package com.superkl.backend.dto.order;

import com.superkl.backend.enums.DirectionEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ImportOrderDraftDto {

    @NotNull(message = "供应商Id不能为空")
    private Long supplierId;

    @NotNull(message = "仓库Id不能为空")
    private Long wareHouseId;

    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "0.00", message = "总金额不能小于0.00")
    private BigDecimal totalAmount;

    @Size(max = 100, message = "备注不能超过100个字符")
    private String remark;

    @NotNull(message = "方向不能为空")
    private DirectionEnum direction;

    @NotEmpty(message = "进货订单项不能为空")
    private List<@Valid ImportOrderItemCreateDto> importItems;
}
