package com.superkl.backend.converter.product;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.dto.product.ProductCreateDto;
import com.superkl.backend.dto.product.ProductUpdateDto;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.info.product.ProductInfo;
import org.springframework.data.domain.Page;

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
                .type(product.getType())
                .category(product.getCategory())
                .unit(product.getUnit())
                .composition(product.getComposition())
                .year(product.getYear())
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

    // 分页实体转分页Info
    public static PageResult<ProductInfo> toInfoPage(Page<Product> products) {
        Page<ProductInfo> page = products.map(ProductConverter::toInfo);
        return new PageResult<>(page);
    }

    // dto转实体
    public static Product toEntity(ProductCreateDto productCreateDto) {
        // 这里不处理SKU，因为比较复杂，转换器无法一口气处理，外部将采用笛卡尔积的方式处理
        return Product.builder()
                .productCode(productCreateDto.getCode())
                .productName(productCreateDto.getName())
                .season(productCreateDto.getSeason())
                .type(productCreateDto.getType())
                .category(productCreateDto.getCategory())
                .unit(productCreateDto.getUnit())
                .composition(productCreateDto.getComposition())
                .year(productCreateDto.getYear())
                .importPrice(productCreateDto.getImportPrice())
                .salePrice(productCreateDto.getSalePrice())
                .special(productCreateDto.isSpecial())
                .build();
    }

    public static void updateEntity(Product product, ProductUpdateDto productUpdateDto) {
        product.setProductCode(productUpdateDto.getCode());
        product.setProductName(productUpdateDto.getName());
        product.setSeason(productUpdateDto.getSeason());
        product.setType(productUpdateDto.getType());
        product.setCategory(productUpdateDto.getCategory());
        product.setUnit(productUpdateDto.getUnit());
        product.setComposition(productUpdateDto.getComposition());
        product.setYear(productUpdateDto.getYear());
        product.setImportPrice(productUpdateDto.getImportPrice());
        product.setSalePrice(productUpdateDto.getSalePrice());
        product.setSpecial(productUpdateDto.isSpecial());
        product.setStatus(productUpdateDto.getStatus());
    }


}
