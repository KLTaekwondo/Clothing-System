package com.superkl.backend.converter.stock;

import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.entity.stock.StockRecord;
import com.superkl.backend.info.stock.StockRecordInfo;

import java.util.List;

public class StockRecordConverter {
    private StockRecordConverter() {
    } // 私有化构造函数，防止外部实例化对象

    // 实体转Info
    public static StockRecordInfo toInfo(StockRecord stockRecord) {
        return StockRecordInfo.builder()
                .id(stockRecord.getRecordId())
                .sourceNo(stockRecord.getSourceNo())
                .sourceType(stockRecord.getSourceType())
                .changeType(stockRecord.getChangeType())
                .productCode(stockRecord.getProductCode())
                .productName(stockRecord.getProductName())
                .skuCode(stockRecord.getSkuCode())
                .skuName(stockRecord.getSkuName())
                .wareHouseCode(stockRecord.getWareHouseCode())
                .wareHouseName(stockRecord.getWareHouseName())
                .changeQuantity(stockRecord.getChangeQuantity())
                .beforeQuantity(stockRecord.getBeforeQuantity())
                .afterQuantity(stockRecord.getAfterQuantity())
                .operationCode(stockRecord.getOperationCode())
                .operationName(stockRecord.getOperationName())
                .operationRole(stockRecord.getOperationRole())
                .createTime(stockRecord.getCreateTime())
                .updateTime(stockRecord.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<StockRecordInfo> toInfoList(List<StockRecord> stockRecords) {
        return stockRecords.stream()
                .map(StockRecordConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static StockRecord toEntity(StockContext stockContext) {
        return StockRecord.builder()
                .sourceNo(stockContext.getSourceNo())
                .changeType(stockContext.getChangeType())
                .sourceType(stockContext.getSourceType())
                .build();
    }

}