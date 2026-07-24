package com.superkl.backend.controller.product;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.product.OptionValueCreateDto;
import com.superkl.backend.dto.product.OptionValueUpdateDto;
import com.superkl.backend.enums.OptionTypeEnum;
import com.superkl.backend.info.poduct.OptionValueInfo;
import com.superkl.backend.service.product.OptionValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/option-value")
@RequiredArgsConstructor
public class OptionValueController {
    private final OptionValueService optionValueService;

    // 1. 创建选项值
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody OptionValueCreateDto dto) {
        optionValueService.create(dto);
        return Result.successMessage("创建成功");
    }

    // 2. 更新选项值
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OptionValueUpdateDto dto) {
        optionValueService.update(id, dto);
        return Result.successMessage("更新成功");
    }

    // 3. 删除选项值
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        optionValueService.delete(id);
        return Result.successMessage("删除成功");
    }

    // 4. 查询选项值
    @GetMapping("/search/{id}")
    public Result<OptionValueInfo> search(@PathVariable Long id) {
        return Result.success(optionValueService.search(id));
    }

    // 5. 查询选项值列表
    @GetMapping("/search/list")
    public Result<List<OptionValueInfo>> searchList() {
        return Result.success(optionValueService.searchList());
    }

    // 6. 查询指定类型的所有选项值
    @GetMapping("/search/list/{type}")
    public Result<List<OptionValueInfo>> searchListByType(@PathVariable OptionTypeEnum type) {
        return Result.success(optionValueService.searchListByType(type));
    }
}

