package com.superkl.backend.service;

import com.superkl.backend.converter.ProductConverter;
import com.superkl.backend.dto.ProductCreateDto;
import com.superkl.backend.dto.ProductUpdateDto;
import com.superkl.backend.entity.Product;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.ProductInfo;
import com.superkl.backend.repository.ProductRepository;
import com.superkl.backend.utils.SkuUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductSkuService productSkuService;

    // 创建商品（重中之重）
    @Transactional
    public void create(ProductCreateDto productCreateDto) {
        // 分成两部分处理
        // 1.创建商品本体的信息，然后入库
        Product product = ProductConverter.toEntity(productCreateDto);
        productRepository.save(product);

        // 2.开始处理规格信息
        Map<String , List<String>> selectedOptions = productCreateDto.getSelectedOptions();
        // 2.1 组合选中的选项，生成SKU名称
        List<Map<String , String>> skuList = SkuUtil.generateSkuList(selectedOptions);
        // 2.2 保存SKU信息
        for (Map<String, String> combo : skuList) {
            productSkuService.createFromProduct(product, combo);
        }
    }

    // 更新商品
    @Transactional
    public void update(Long id , ProductUpdateDto dto) {
        // 1.先查一下，看是否存在商品
        Product product = productRepository.findById(id).orElseThrow(() -> new BusinessException("商品不存在"));
        // 2.更新商品信息
        ProductConverter.updateEntity(product, dto);
        productRepository.save(product);
    }

    // 删除商品
    @Transactional
    public void delete(Long id) {
        // 现找是否存在商品
        Product product = productRepository.findById(id).orElseThrow(() -> new BusinessException("商品不存在"));
        // 2.删除商品
        productRepository.deleteById(id);
    }

    // 查询单个商品详情
    public ProductInfo search(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new BusinessException("商品不存在"));
        return ProductConverter.toInfo(product);
    }

    // 查询所有商品
    public List<ProductInfo> searchList(){
        List<Product> products = productRepository.findAll();
        return ProductConverter.toInfoList(products);
    }
}
