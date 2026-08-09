package com.superkl.backend.converter.order;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.info.order.OrderInfo;
import com.superkl.backend.info.order.OrderItemInfo;
import com.superkl.backend.info.order.OrderWithItemsInfo;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class OrderConverter {
    private OrderConverter() {
    } // 私有构造方法, 防止外部实例化

    // 实体转Info
    public static OrderInfo toInfo(Order order) {
        return OrderInfo.builder()
                .id(order.getOrderId())
                .orderNo(order.getOrderNo())
                .payMethod(order.getPayMethod())
                .orderStatus(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .actualPrice(order.getActualPrice())
                .remark(order.getRemark())
                .employeeName(order.getEmployee().getEmployeeName())
                .warehouseName(order.getWareHouse().getWareHouseName())
                .memberPhone(order.getMemberPhone())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<OrderInfo> toInfoList(List<Order> orders) {
        return orders.stream()
                .map(OrderConverter::toInfo)
                .toList();
    }

    // 实体转换为包含商品信息的Info
    public static OrderWithItemsInfo toInfoWithItems(Order order , List<OrderItemInfo> items) {
        return OrderWithItemsInfo.builder()
                .id(order.getOrderId())
                .orderNo(order.getOrderNo())
                .payMethod(order.getPayMethod())
                .orderStatus(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .actualPrice(order.getActualPrice())
                .remark(order.getRemark())
                .employeeName(order.getEmployee().getEmployeeName())
                .warehouseName(order.getWareHouse().getWareHouseName())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .items(items)
                .build();
    }

    // 实体分页转Info分页
    public static PageResult<OrderInfo> toInfoPage(Page<Order> orders) {
        Page<OrderInfo> page = orders.map(OrderConverter::toInfo);
        return new PageResult<>(page);
    }

    // dto转实体
    public static Order toEntity(){
        return Order.builder()
                .orderNo(generateOrderNo())
                .build();

    }


    private static String generateOrderNo(){
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "OR" + date + uuid;
    }
}
