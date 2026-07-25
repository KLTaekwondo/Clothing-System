package com.superkl.backend.controller.product;

import com.superkl.backend.common.PageParam;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.dto.product.ProductCreateDto;
import com.superkl.backend.dto.product.ProductUpdateDto;
import com.superkl.backend.info.poduct.ProductInfo;
import com.superkl.backend.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 创建商品
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody ProductCreateDto dto) {
        productService.create(dto);
        return Result.successMessage("创建成功");
    }

    // 更新商品
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateDto dto) {
        productService.update(id, dto);
        return Result.successMessage("更新成功");
    }

    // 删除商品
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.successMessage("删除成功");
    }

    // 查询商品
    @GetMapping("/search/{id}")
    public Result<ProductInfo> search(@PathVariable Long id) {
        return Result.success(productService.search(id));
    }

    // 查询所有商品
    @GetMapping("/page")
    public Result<PageResult<ProductInfo>> searchPage(@Valid PageParam param) {
        return Result.success(productService.searchPage(param.toPageable()));
    }
}