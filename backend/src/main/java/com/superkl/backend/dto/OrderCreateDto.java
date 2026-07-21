package com.superkl.backend.dto;

import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDto {
    private Long orderId;// 订单ID，用于判断是否是以及保存过的订单，还是新订单创建

    @NotNull(message = "支付方式不能为空")
    private PayMethodEnum payMethod;

    @NotNull(message = "关联员工不能为空")
    private Long employeeId;

    @NotNull(message = "关联仓库不能为空")
    private Long wareHouseId;

    @NotNull(message = "订单项不能为空")
    private List<@Valid OrderItemCreateDto> saleItems;

    @NotNull(message = "订单项不能为空")
    private List<@Valid OrderItemCreateDto> refundItems;

    @NotNull(message = "传入的实际金额不能为空")
    private BigDecimal actualAmount;

    @NotNull(message  = "传入的总金额不能为空")
    private BigDecimal totalAmount;

    @Size(max = 100, message = "备注长度不能超过100")
    private String remark;
}
