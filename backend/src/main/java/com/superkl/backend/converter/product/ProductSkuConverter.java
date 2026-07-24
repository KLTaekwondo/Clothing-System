package com.superkl.backend.converter.product;

import com.superkl.backend.dto.product.ProductSkuCreateDto;
import com.superkl.backend.dto.product.ProductSkuUpdateDto;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.info.poduct.ProductSkuInfo;
import com.superkl.backend.utils.BarCodeUtil;

import java.util.List;

public class ProductSkuConverter {
    private ProductSkuConverter() {}// 私有构造方法，防止外部实例化

    // 实体转Info
    public static ProductSkuInfo toInfo(ProductSku productSku) {
        return ProductSkuInfo.builder()
                .id(productSku.getSkuId())
                .code(productSku.getSkuCode())
                .name(productSku.getSkuName())
                .spec(productSku.getSpecAttributes())
                .status(productSku.getStatus())
                .createTime(productSku.getCreateTime())
                .updateTime(productSku.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<ProductSkuInfo> toInfoList(List<ProductSku> productSkus) {
        return productSkus.stream()
                .map(ProductSkuConverter::toInfo)
                .toList();
    }

    // dto转换为实体
    public static ProductSku toEntity(ProductSkuCreateDto dto, Product product) {
        return ProductSku.builder()
                .skuCode(BarCodeUtil.generateEAN13())
                .skuName(dto.getName())
                .specAttributes(dto.getSpec())
                .product(product)
                .build();
    }

    // dto更新实体
    public static void updateEntity(ProductSku productSku, ProductSkuUpdateDto dto) {
        productSku.setSkuName(dto.getName());
        productSku.setSpecAttributes(dto.getSpec());
        productSku.setStatus(dto.getStatus());
    }
}
