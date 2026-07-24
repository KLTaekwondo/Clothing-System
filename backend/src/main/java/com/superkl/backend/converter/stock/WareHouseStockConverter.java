package com.superkl.backend.converter.stock;

import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.info.stock.WareHouseStockInfo;
import com.superkl.backend.utils.JsonUtil;

import java.util.List;

public class WareHouseStockConverter {
    private WareHouseStockConverter() {} // 私有化构造方法

    // 实体转化为Info
    public static WareHouseStockInfo toInfo(WareHouseStock wareHouseStock) {
        return WareHouseStockInfo.builder()
                .id(wareHouseStock.getStockId())
                .spec(JsonUtil.toMap(wareHouseStock.getProductSku().getSpecAttributes()))
                .stock(wareHouseStock.getStock())
                .createTime(wareHouseStock.getCreateTime())
                .updateTime(wareHouseStock.getUpdateTime())
                .build();

    }

    // 实体列表转化为Info列表
    public static List<WareHouseStockInfo> toInfoList(List<WareHouseStock> wareHouseStockList) {
        return wareHouseStockList.stream()
                .map(WareHouseStockConverter::toInfo)
                .toList();
    }
}
