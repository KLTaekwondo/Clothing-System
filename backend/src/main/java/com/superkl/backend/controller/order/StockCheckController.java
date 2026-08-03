package com.superkl.backend.controller.order;

import com.superkl.backend.common.PageParam;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.dto.order.StockCheckDraftDto;
import com.superkl.backend.info.order.StockCheckInfo;
import com.superkl.backend.info.order.StockCheckWithItemsInfo;
import com.superkl.backend.service.order.StockCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stockCheck")
@RequiredArgsConstructor
public class StockCheckController {
    private final StockCheckService stockCheckService;

    // 1. 创建库存盘点单
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody StockCheckDraftDto dto) {
        stockCheckService.create(dto);
        return Result.successMessage("创建成功");
    }

    // 2.提交库存盘点单（更新盘点状态为审查中）
    @PutMapping("/check/{id}")
    public Result<Void> check(@PathVariable Long id) {
        stockCheckService.check(id);
        return Result.successMessage("提交成功");
    }

    // 3. 审核库存盘点单（更新盘点状态为已完成）
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        stockCheckService.approve(id);
        return Result.successMessage("审核成功");
    }

    // 4. 拒绝库存盘点单（更新盘点状态为已拒绝）
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id) {
        stockCheckService.reject(id);
        return Result.successMessage("拒绝成功");
    }

    // 5. 更新库存盘点单信息
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StockCheckDraftDto dto) {
        stockCheckService.update(id, dto);
        return Result.successMessage("更新成功");
    }

    // 6. 查询单个库存盘点单详情
    @GetMapping("/detail/{id}")
    public Result<StockCheckWithItemsInfo> detail(@PathVariable Long id) {
        return Result.success(stockCheckService.search(id));
    }

    // 7. 查询库存盘点单分页列表
    @GetMapping("/page")
    public Result<PageResult<StockCheckInfo>> page(@Valid PageParam pageParam) {
        Pageable pageable = pageParam.toPageable();
        return Result.success(stockCheckService.searchPage(pageable));
    }

    // 8. 删除草稿
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockCheckService.delete(id);
        return Result.successMessage("库存盘点单草稿已删除！");
    }
}
