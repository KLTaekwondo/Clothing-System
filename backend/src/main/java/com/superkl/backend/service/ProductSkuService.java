package com.superkl.backend.service;

import com.superkl.backend.converter.ProductSkuConverter;
import com.superkl.backend.dto.ProductSkuCreateDto;
import com.superkl.backend.dto.ProductSkuUpdateDto;
import com.superkl.backend.entity.Product;
import com.superkl.backend.entity.ProductSku;
import com.superkl.backend.entity.WareHouseStock;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.ProductSkuInfo;
import com.superkl.backend.repository.ProductRepository;
import com.superkl.backend.repository.ProductSkuRepository;
import com.superkl.backend.repository.WareHouseRepository;
import com.superkl.backend.repository.WareHouseStockRepository;
import com.superkl.backend.utils.BarCodeUtil;
import com.superkl.backend.utils.SkuUtil;
import com.superkl.backend.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductSkuService {
    private final ProductSkuRepository productSkuRepository;
    private final ProductRepository productRepository;
    private final WareHouseRepository wareHouseRepository;
    private final WareHouseStockRepository wareHouseStockRepository;

    //1. 新增商品SKU
    @Transactional
    public void create(ProductSkuCreateDto dto) {
        // 校验商品是否存在
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 转化为Sku实体
        ProductSku productSku = ProductSkuConverter.toEntity(dto, product);
        productSkuRepository.save(productSku);
    }

    //2. 更新商品SKU
    @Transactional
    public void update(Long id , ProductSkuUpdateDto dto) {
        // 校验商品SKU是否存在
        ProductSku productSku = productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品SKU不存在"));

        // 更新商品SKU
        ProductSkuConverter.updateEntity(productSku, dto);
        productSkuRepository.save(productSku);
    }

    //3. 删除商品SKU
    @Transactional
    public void delete(Long id) {
        // 校验商品SKU是否存在
        ProductSku productSku = productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品SKU不存在"));

        // 删除商品SKU
        productSkuRepository.delete(productSku);
    }

    // 4. 根据商品ID查询商品SKU列表
    public List<ProductSkuInfo> searchByProductId(Long productId) {
        List<ProductSku> productSkus = productSkuRepository.findByProductId(productId);
        return ProductSkuConverter.toInfoList(productSkus);
    }

    // 5. 查询单个的商品属性
    public ProductSkuInfo searchById(Long id) {
        ProductSku productSku = productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品SKU不存在"));
        return ProductSkuConverter.toInfo(productSku);
    }

    // 6. 特殊创建方法
    @Transactional
    public void createFromProduct(Product product , Map<String , String>combo) {
        ProductSku sku = ProductSku.builder()
                .product(product)
                .skuName(SkuUtil.generateSkuName(combo))
                .skuCode(BarCodeUtil.generateEAN13())
                .specAttributes(JsonUtil.toJson(combo))
                .build();
        productSkuRepository.save(sku);

        // 初始化库存为0
        wareHouseRepository.findAll().forEach(wareHouse -> {
            wareHouseStockRepository.save(WareHouseStock.builder()
                    .wareHouse(wareHouse)
                    .productSku(sku)
                    .stock(0)
                    .build());
        });
    }
}
