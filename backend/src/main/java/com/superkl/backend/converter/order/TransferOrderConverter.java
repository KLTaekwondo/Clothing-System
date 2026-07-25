package com.superkl.backend.converter.order;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.entity.order.TransferOrder;
import com.superkl.backend.info.order.TransferOrderInfo;
import com.superkl.backend.info.order.TransferOrderItemInfo;
import com.superkl.backend.info.order.TransferOrderWithItemsInfo;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class TransferOrderConverter {
    private TransferOrderConverter() {} // 私有构造函数，防止外部实例化

    // 实体转Info
    public static TransferOrderInfo toInfo(TransferOrder transferOrder) {
        return TransferOrderInfo.builder()
                .id(transferOrder.getTransferOrderId())
                .transferOrderNo(transferOrder.getTransferOrderNo())
                .sourceWareHouseName(transferOrder.getSourceWareHouseName())
                .targetWareHouseName(transferOrder.getTargetWareHouseName())
                .status(transferOrder.getStatus())
                .remark(transferOrder.getRemark())
                .totalPrice(transferOrder.getTotalPrice())
                .createTime(transferOrder.getCreateTime())
                .updateTime(transferOrder.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<TransferOrderInfo> toInfoList(List<TransferOrder> transferOrders) {
        return transferOrders.stream()
                .map(TransferOrderConverter::toInfo)
                .toList();
    }

    // 实体转化为带关联属性的Info列表
    public static TransferOrderWithItemsInfo toInfoWithItems(TransferOrder transferOrder ,
                                                             List<TransferOrderItemInfo> items) {
        return TransferOrderWithItemsInfo.builder()
                .id(transferOrder.getTransferOrderId())
                .transferOrderNo(transferOrder.getTransferOrderNo())
                .sourceWareHouseName(transferOrder.getSourceWareHouseName())
                .targetWareHouseName(transferOrder.getTargetWareHouseName())
                .status(transferOrder.getStatus())
                .items(items)
                .totalPrice(transferOrder.getTotalPrice())
                .remark(transferOrder.getRemark())
                .createTime(transferOrder.getCreateTime())
                .updateTime(transferOrder.getUpdateTime())
                .build();
    }

    // 实体分页转Info分页
    public static PageResult<TransferOrderInfo> toInfoPage(Page<TransferOrder> page) {
        Page<TransferOrderInfo> infoPage = page.map(TransferOrderConverter::toInfo);
        return new PageResult<>(infoPage);
    }

    // DTO转实体
    public static TransferOrder toEntity() {
        return TransferOrder.builder()
                .transferOrderNo(generateOrderNo())
                .build();
    }

    private static String generateOrderNo(){
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TOR" + date + uuid;
    }
}
