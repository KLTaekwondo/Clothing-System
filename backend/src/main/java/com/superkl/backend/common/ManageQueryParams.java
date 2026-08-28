package com.superkl.backend.common;

import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ManageQueryParams {
    // 必填参数
    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate start;

    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

    // 选择性参数
    private OrderStatusEnum orderStatus = OrderStatusEnum.COMPLETED;// 指定查询的订单状态,默认查询已完成订单（这个参数不能为null）
    private Long wareHouseId = null; // 指定查询的仓库,null表示查询所有仓库,默认查询所有仓库
    private PayMethodEnum payMethod  = null; // 指定查询的支付方式,null表示查询所有支付方式,默认查询所有支付方式
    private Long employeeId = null; // 指定查询的员工,null表示查询所有员工,默认查询所有员工
}
