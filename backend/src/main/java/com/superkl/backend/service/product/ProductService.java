package com.superkl.backend.service.product;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.product.ProductConverter;
import com.superkl.backend.dto.product.ProductCreateDto;
import com.superkl.backend.dto.product.ProductUpdateDto;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.product.ProductInfo;
import com.superkl.backend.repository.product.ProductRepository;
import com.superkl.backend.utils.SkuUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductSkuService productSkuService;

    // 创建商品（重中之重）
    @Transactional
    public void create(ProductCreateDto productCreateDto) {
        // 检查商品编码是否已经存在
        if(productRepository.existsByCode(productCreateDto.getCode())){
            throw new BusinessException(403, "商品编码已存在！");
        }
        // 1.开始处理规格信息（只取颜色和尺码）
        Map<String, List<String>> selectedOptions =
                productCreateDto.getSelectedOptions();
        Map<String, List<String>> skuOptions = new java.util.HashMap<>();
        // 只处理颜色和尺码规格
        if (selectedOptions.containsKey("COLOR")) {
            skuOptions.put("COLOR", selectedOptions.get("COLOR"));
        }
        if (selectedOptions.containsKey("SIZE")) {
            skuOptions.put("SIZE", selectedOptions.get("SIZE"));
        }
        SkuUtil.validateSelectedOptions(skuOptions);
        List<Map<String, String>> skuList =
                SkuUtil.generateSkuList(skuOptions);
        if (skuList.isEmpty()) {
            throw new BusinessException("商品规格不能为空");
        }

        // 2.创建商品本体的信息，然后入库
        Product product = ProductConverter.toEntity(productCreateDto);
        productRepository.save(product);
        log.info("新增商品：{}，编码：{}", product.getProductName(),
                product.getProductCode());
        RequestUser.log();

        // 3. 保存SKU信息
        for (Map<String, String> combo : skuList) {
            productSkuService.createFromProduct(product, combo);
        }
    }

    // 更新商品
    @Transactional
    public void update(Long id , ProductUpdateDto dto) {
        // 1.先查一下，看是否存在商品
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "商品不存在"));
        // 检查商品编码是否已经存在
        String code = dto.getCode();
        if(productRepository.existsByCode(code) && !code.equals(product.getProductCode())){
            throw new BusinessException(403, "商品编码已存在！");
        }
        // 2.更新商品信息
        ProductConverter.updateEntity(product, dto);
        productRepository.save(product);
        log.info("更新商品：{}", product.getProductName());
        RequestUser.log();
    }

    // 删除商品
    @Transactional
    public void delete(Long id) {
        // 现找是否存在商品
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "商品不存在"));
        // 2.禁用商品
        product.setStatus(StatusEnum.DISABLE);
        productRepository.save(product);
        log.info("禁用商品：{}", product.getProductName());
        RequestUser.log();
    }

    // 查询单个商品详情
    public ProductInfo search(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "商品不存在"));
        return ProductConverter.toInfo(product);
    }

    // 查询并导出商品列表
    public List<ProductInfo> getProductList(){
        return ProductConverter.toInfoList(productRepository.findAll());
    }

    // 查询所有商品
    public PageResult<ProductInfo> searchPage(Pageable pageable){
        return ProductConverter.toInfoPage(productRepository.findPage(pageable));
    }
}
