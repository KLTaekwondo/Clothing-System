package com.superkl.backend.converter;

import com.superkl.backend.dto.OrderCreateDto;
import com.superkl.backend.entity.Order;
import com.superkl.backend.entity.OrderItem;
import com.superkl.backend.info.OrderInfo;
import com.superkl.backend.info.OrderItemInfo;
import com.superkl.backend.info.OrderWithItemsInfo;

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
