package com.superkl.backend.converter.order;

import com.superkl.backend.dto.order.StockCheckItemCreateDto;
import com.superkl.backend.entity.order.StockCheckItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.info.order.StockCheckItemInfo;

import java.util.List;

public class StockCheckItemConverter {
    private StockCheckItemConverter() {}// 私有构造方法，防止外部实例化

    // 实体转Info
    public static StockCheckItemInfo toInfo(StockCheckItem stockCheckItem) {
        return StockCheckItemInfo.builder()
                .skuName(stockCheckItem.getSkuName())
                .skuCode(stockCheckItem.getSkuCode())
                .productName(stockCheckItem.getProductName())
                .productCode(stockCheckItem.getProductCode())
                .actualQuantity(stockCheckItem.getActualQuantity())
                .systemQuantity(stockCheckItem.getSystemQuantity())
                .diffQuantity(stockCheckItem.getDiffQuantity())
                .id(stockCheckItem.getStockCheckItemId())
                .createTime(stockCheckItem.getCreateTime())
                .updateTime(stockCheckItem.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<StockCheckItemInfo> toInfoList(List<StockCheckItem> stockCheckItemList) {
        return stockCheckItemList.stream()
                .map(StockCheckItemConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static StockCheckItem toEntity(StockCheckItemCreateDto dto , ProductSku sku, WareHouseStock ws) {
        return StockCheckItem.builder()
                .stockId(ws.getStockId())
                .skuId(sku.getSkuId())
                .skuName(sku.getSkuName())
                .skuCode(sku.getSkuCode())
                .productName(sku.getProduct().getProductName())
                .productCode(sku.getProduct().getProductCode())
                .actualQuantity(dto.getActualQuantity())
                .systemQuantity(ws.getStock())
                .diffQuantity(dto.getActualQuantity() - ws.getStock())
                .build();
    }
}
