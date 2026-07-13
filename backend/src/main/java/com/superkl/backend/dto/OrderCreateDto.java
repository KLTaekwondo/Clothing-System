package com.superkl.backend.dto;

import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDto {
    @NotNull(message = "支付方式不能为空")
    private PayMethodEnum payMethod;

    @NotNull(message = "关联员工不能为空")
    private Long employeeId;

    @NotNull(message = "关联仓库不能为空")
    private Long wareHouseId;

    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemCreateDto> orderItems;

    @NotNull(message = "传入的实际金额不能为空")
    private BigDecimal actualAmount;

    @NotNull(message  = "传入的总金额不能为空")
    private BigDecimal totalAmount;

    private String remark;
}
