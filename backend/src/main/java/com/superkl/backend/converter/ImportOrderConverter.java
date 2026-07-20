package com.superkl.backend.converter;

import com.superkl.backend.dto.ImportOrderDraftDto;
import com.superkl.backend.entity.ImportOrder;
import com.superkl.backend.info.ImportOrderInfo;
import com.superkl.backend.info.ImportOrderItemInfo;
import com.superkl.backend.info.ImportOrderWithItemsInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ImportOrderConverter {
    private ImportOrderConverter() {}// 私有构造方法,防止外部实例化

    // 实体转Info
    public static ImportOrderInfo toInfo(ImportOrder importOrder) {
        return ImportOrderInfo.builder()
                .id(importOrder.getImportOrderId())
                .importOrderNo(importOrder.getImportOrderNo())
                .supplierName(importOrder.getSupplierName())
                .remark(importOrder.getRemark())
                .totalAmount(importOrder.getTotalAmount())
                .status(importOrder.getStatus())
                .build();
    }

    // 实体列表转Info列表
    public static List<ImportOrderInfo> toInfoList(List<ImportOrder> importOrders) {
        return importOrders.stream()
                .map(ImportOrderConverter::toInfo)
                .toList();
    }

    // 实体转换为包含商品信息的Info
    public static ImportOrderWithItemsInfo toInfoWithItems(ImportOrder importOrder , List<ImportOrderItemInfo> items) {
        return ImportOrderWithItemsInfo.builder()
                .id(importOrder.getImportOrderId())
                .importOrderNo(importOrder.getImportOrderNo())
                .supplierName(importOrder.getSupplierName())
                .remark(importOrder.getRemark())
                .wareHouseName(importOrder.getWareHouse().getWareHouseName())
                .totalAmount(importOrder.getTotalAmount())
                .status(importOrder.getStatus())
                .createTime(importOrder.getCreateTime())
                .updateTime(importOrder.getUpdateTime())
                .items(items)
                .build();
    }

    // dto转实体
    public static ImportOrder toEntity(ImportOrderDraftDto dto){
        return ImportOrder.builder()
                .importOrderNo(generateImportOrderNo())
                .remark(dto.getRemark())
                .build();
    }

    // 生成导入订单号
    public static String generateImportOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "IOR" + date + uuid;
    }
}
