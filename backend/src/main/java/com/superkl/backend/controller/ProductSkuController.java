package com.superkl.backend.controller;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.ProductSkuCreateDto;
import com.superkl.backend.dto.ProductSkuUpdateDto;
import com.superkl.backend.info.ProductSkuInfo;
import com.superkl.backend.service.ProductSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productSku")
@RequiredArgsConstructor
public class ProductSkuController {
    private final ProductSkuService productSkuService;

    //1. 创建商品SKU
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody ProductSkuCreateDto dto) {
        productSkuService.create(dto);
        return Result.successMessage("创建成功");
    }

    //2. 更新商品SKU
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSkuUpdateDto dto) {
        productSkuService.update(id, dto);
        return Result.successMessage("更新成功");
    }

    //3. 删除商品SKU
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productSkuService.delete(id);
        return Result.successMessage("删除成功");
    }

    //4. 查询商品SKU
    @GetMapping("/search/{id}")
    public Result<ProductSkuInfo> search(@PathVariable Long id) {
        return Result.success(productSkuService.searchById(id));
    }

    // 5. 根据商品ID查询商品SKU列表
    @GetMapping("/search/byProductId/{productId}")
    public Result<List<ProductSkuInfo>> searchByProductId(@PathVariable Long productId) {
        return Result.success(productSkuService.searchByProductId(productId));
    }

    // 6.扫描编码获取商品SKU列表
    @GetMapping("/scan/{code}")
    public Result<List<ProductSkuInfo>> scanByCode(@PathVariable String code) {
        return Result.success(productSkuService.verify(code));
    }
}
