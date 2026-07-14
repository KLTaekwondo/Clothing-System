package com.superkl.backend.converter;

import com.superkl.backend.dto.ProductCreateDto;
import com.superkl.backend.dto.ProductUpdateDto;
import com.superkl.backend.entity.Product;
import com.superkl.backend.info.ProductInfo;

import java.util.List;

public class ProductConverter {
    private ProductConverter() {}// 私有构造方法，防止外部实例化

    // 实体转Info
    public static ProductInfo toInfo(Product product) {
        return ProductInfo.builder()
                .id(product.getProductId())
                .code(product.getProductCode())
                .name(product.getProductName())
                .season(product.getSeason())
                .importPrice(product.getImportPrice())
                .salePrice(product.getSalePrice())
                .special(product.isSpecial())
                .status(product.getStatus())
                .createTime(product.getCreateTime())
                .updateTime(product.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<ProductInfo> toInfoList(List<Product> products) {
        return products.stream()
                .map(ProductConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static Product toEntity(ProductCreateDto productCreateDto) {
        // 这里不处理SKU，因为比较复杂，转换器无法一口气处理，外部将采用笛卡尔积的方式处理
        return Product.builder()
                .productCode(productCreateDto.getCode())
                .productName(productCreateDto.getName())
                .season(productCreateDto.getSeason())
                .importPrice(productCreateDto.getImportPrice())
                .salePrice(productCreateDto.getSalePrice())
                .special(productCreateDto.isSpecial())
                .build();
    }

    public static void updateEntity(Product product, ProductUpdateDto productUpdateDto) {
        product.setProductCode(productUpdateDto.getCode());
        product.setProductName(productUpdateDto.getName());
        product.setSeason(productUpdateDto.getSeason());
        product.setImportPrice(productUpdateDto.getImportPrice());
        product.setSalePrice(productUpdateDto.getSalePrice());
        product.setSpecial(productUpdateDto.isSpecial());
        product.setStatus(productUpdateDto.getStatus());
    }


}
