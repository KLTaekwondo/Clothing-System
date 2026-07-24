package com.superkl.backend.info.order;

import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import com.superkl.backend.info.BaseInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrderInfo extends BaseInfo {
    private String orderNo; // 订单编号
    private PayMethodEnum payMethod; // 支付方式
    private OrderStatusEnum orderStatus; // 订单状态
    private BigDecimal totalPrice; // 订单总价
    private BigDecimal actualPrice; // 实际支付金额
    private String remark; // 订单备注
    private String employeeName; // 关联员工姓名
    private String warehouseName; // 关联仓库名称
    private DirectionEnum direction;// 业务方向(IN:售出商品/OUT:退货商品)
}
