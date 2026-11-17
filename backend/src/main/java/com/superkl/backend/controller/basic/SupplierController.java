package com.superkl.backend.controller.basic;

import com.superkl.backend.annotation.OpLogs;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.common.extralparams.PageParam;
import com.superkl.backend.dto.basic.SupplierCreateDto;
import com.superkl.backend.dto.basic.SupplierUpdateDto;
import com.superkl.backend.info.basic.SupplierInfo;
import com.superkl.backend.service.basic.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    // 创建供应商
    @OpLogs(module = "供应商", description = "创建供应商")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody SupplierCreateDto dto) {
        supplierService.create(dto);
        return Result.successMessage("创建供应商成功！");
    }

    // 更新供应商信息
    @OpLogs(module = "供应商", description = "更新供应商信息")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDto dto) {
        supplierService.update(id, dto);
        return Result.successMessage("更新供应商成功！");
    }

    // 删除供应商
    @OpLogs(module = "供应商", description = "删除供应商")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.successMessage("删除供应商成功！");
    }

    // 获取供应商列表
    @GetMapping("/search/{supplierCode}")
    public Result<SupplierInfo> search(@PathVariable String supplierCode) {
        return Result.success(supplierService.search(supplierCode));
    }

    // 获取所有供应商（下拉/筛选用，需要全量）
    @GetMapping("/search/list")
    public Result<List<SupplierInfo>> searchList() {
        return Result.success(supplierService.searchList());
    }

    // 分页查询供应商
    @GetMapping("/page")
    public Result<PageResult<SupplierInfo>> searchPage(@RequestParam(required = false) String keyword, PageParam pageParam) {
        // 转换为Pageable
        Pageable pageable = pageParam.toPageable();
        return Result.success(supplierService.searchPage(keyword, pageable));
    }
}
