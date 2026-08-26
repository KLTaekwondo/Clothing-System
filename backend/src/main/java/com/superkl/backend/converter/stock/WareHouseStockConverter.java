package com.superkl.backend.converter.stock;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.info.stock.WareHouseStockInfo;
import com.superkl.backend.utils.JsonUtil;
import org.springframework.data.domain.Page;

import java.util.List;

public class WareHouseStockConverter {
    private WareHouseStockConverter() {} // 私有化构造方法

    // 实体转化为Info
    public static WareHouseStockInfo toInfo(WareHouseStock wareHouseStock) {
        ProductSku productSku = wareHouseStock.getProductSku();
        Product product = productSku.getProduct();

        return WareHouseStockInfo.builder()
                .id(wareHouseStock.getStockId())
                .spec(JsonUtil.toMap(wareHouseStock.getProductSku().getSpecAttributes()))
                .stock(wareHouseStock.getStock())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .skuName(productSku.getSkuName())
                .skuCode(productSku.getSkuCode())
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

    // 实体列表转化为Info分页
    public static PageResult<WareHouseStockInfo> toInfoPage(Page<WareHouseStock> wareHouseStockPage) {
        Page<WareHouseStockInfo> page = wareHouseStockPage.map(WareHouseStockConverter::toInfo);
        return new PageResult<>(page);
       }
}
