package com.superkl.backend.service.product;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.product.ProductSkuConverter;
import com.superkl.backend.dto.product.ProductSkuCreateDto;
import com.superkl.backend.dto.product.ProductSkuUpdateDto;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.product.ProductSkuCheckInfo;
import com.superkl.backend.info.product.ProductSkuInfo;
import com.superkl.backend.repository.product.ProductRepository;
import com.superkl.backend.repository.product.ProductSkuRepository;
import com.superkl.backend.repository.basic.WareHouseRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import com.superkl.backend.utils.BarCodeUtil;
import com.superkl.backend.utils.SkuUtil;
import com.superkl.backend.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
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
                .orElseThrow(() -> new BusinessException(403, "商品不存在"));

        // 转化为Sku实体
        ProductSku productSku = ProductSkuConverter.toEntity(dto, product);

        //保存商品SKU实体
        productSkuRepository.save(productSku);
        log.info("新增SKU：{}，所属商品ID：{}", productSku.getSkuName(), dto.getProductId());
        RequestUser.log();

        //添加库存
        wareHouseRepository.findAll().forEach(wareHouse -> {
            wareHouseStockRepository.save(WareHouseStock.builder()
                    .wareHouse(wareHouse)
                    .productSku(productSku)
                    .stock(0)
                    .build());
        });

    }

    //2. 更新商品SKU
    @Transactional
    public void update(Long id , ProductSkuUpdateDto dto) {
        // 校验商品SKU是否存在
        ProductSku productSku = productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "商品SKU不存在"));

        // 更新商品SKU
        ProductSkuConverter.updateEntity(productSku, dto);
        productSkuRepository.save(productSku);
        log.info("更新SKU：{}", productSku.getSkuName());
        RequestUser.log();
    }

    //3. 删除商品SKU
    @Transactional
    public void delete(Long id) {
        // 校验商品SKU是否存在
        ProductSku productSku = productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "商品SKU不存在"));

        // 禁用商品SKU状态
        productSku.setStatus(StatusEnum.DISABLE);
        productSkuRepository.save(productSku);
        log.info("禁用SKU：{}", productSku.getSkuName());
        RequestUser.log();
    }

    // 4. 特殊创建方法
    @Transactional
    public void createFromProduct(Product product , Map<String , String>combo) {
        ProductSku sku = ProductSku.builder()
                .product(product)
                .skuName(SkuUtil.generateSkuName(combo))
                .skuCode(BarCodeUtil.generateEAN13())
                .specAttributes(JsonUtil.toJson(combo))
                .build();
        productSkuRepository.save(sku);
        log.info("从商品创建SKU：{}", sku.getSkuName());
        RequestUser.log();

        // 初始化库存为0
        wareHouseRepository.findAll().forEach(wareHouse -> {
            wareHouseStockRepository.save(WareHouseStock.builder()
                    .wareHouse(wareHouse)
                    .productSku(sku)
                    .stock(0)
                    .build());
        });
    }

    // 5. 根据商品ID查询商品SKU列表
    public List<ProductSkuInfo> searchByProductId(Long productId) {
        List<ProductSku> productSkus = productSkuRepository.findByProductId(productId);
        return ProductSkuConverter.toInfoList(productSkus);
    }

    // 6. 查询单个的商品属性
    public ProductSkuInfo searchById(Long id) {
        ProductSku productSku = productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "商品SKU不存在"));
        return ProductSkuConverter.toInfo(productSku);
    }

    // 7. 验证商品SKU是否启用
    public List<ProductSkuCheckInfo> verify(String code) {
        // 1. 先当 SKU 条码查
        ProductSku productSku = productSkuRepository.findBySkuCode(code).orElse(null);
        if (productSku != null) {
            if (!productSku.getProduct().isEnabled()) {
                throw new BusinessException(405, "商品已禁用");
            }
            if (!productSku.isEnabled()) {
                throw new BusinessException(405, "商品SKU已禁用");
            }
            return List.of(ProductSkuConverter.toCheckInfo(productSku));
        }

        // 2. 没查到，当商品编码查
        Product product = productRepository.findByProductCode(code)
                .orElseThrow(() -> new BusinessException(403, "商品不存在"));

        if (!product.isEnabled()) {
            throw new BusinessException(405, "商品已禁用");
        }

        // 只返回启用状态的商品SKU列表
        List<ProductSku> productSkus = productSkuRepository.findByProductCodeAndStatus(code, StatusEnum.ENABLE);
        if (productSkus.isEmpty()) {
            throw new BusinessException("该商品下没有可用SKU");
        }

        return ProductSkuConverter.toCheckInfoList(productSkus);
    }
}
