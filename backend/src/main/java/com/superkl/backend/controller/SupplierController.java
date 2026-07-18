package com.superkl.backend.controller;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.SupplierCreateDto;
import com.superkl.backend.dto.SupplierUpdateDto;
import com.superkl.backend.info.SupplierInfo;
import com.superkl.backend.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    // 创建供应商
    @PostMapping("/create")
    public Result<Void> createSupplier(@Valid @RequestBody SupplierCreateDto dto) {
        supplierService.create(dto);
        return Result.successMessage("创建供应商成功！");
    }

    // 更新供应商
    @PutMapping("/update/{id}")
    public Result<Void> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDto dto) {
        supplierService.update(id, dto);
        return Result.successMessage("更新供应商成功！");
    }

    // 删除供应商
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.successMessage("删除供应商成功！");
    }

    // 获取供应商列表
    @GetMapping("/search/{supplierCode}")
    public Result<SupplierInfo> searchSupplier(@PathVariable String supplierCode) {
        return Result.success(supplierService.search(supplierCode));
    }

    // 获取所有供应商
    @GetMapping("/search/list")
    public Result<List<SupplierInfo>> searchList() {
        return Result.success(supplierService.searchList());
    }
}
