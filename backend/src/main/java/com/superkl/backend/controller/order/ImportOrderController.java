package com.superkl.backend.controller.order;

import com.superkl.backend.annotation.OpLogs;
import com.superkl.backend.common.extralparams.PageParam;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.dto.order.ImportOrderDraftDto;
import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.info.order.ImportOrderInfo;
import com.superkl.backend.info.order.ImportOrderWithItemsInfo;
import com.superkl.backend.service.order.ImportOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import-order")
@RequiredArgsConstructor
public class ImportOrderController {
    private final ImportOrderService importOrderService;

    //1. 新增进货订单
    @OpLogs(module = "订单", description = "新增进货订单")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody ImportOrderDraftDto importOrder){
        importOrderService.create(importOrder);
        return Result.successMessage("草稿保存成功！");
    }

    //2. 更新草稿
    @OpLogs(module = "订单", description = "更新进货订单草稿")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ImportOrderDraftDto importOrder){
        importOrderService.update(id, importOrder);
        return Result.successMessage("草稿更新成功！");
    }

    //3. 提交订单
    @OpLogs(module = "订单", description = "提交进货订单")
    @PutMapping("/check/{id}")
    public Result<Void> check(@PathVariable Long id){
        importOrderService.check(id);
        return Result.successMessage("订单已提交！");
    }

    //4. 通过订单
    @OpLogs(module = "订单", description = "通过进货订单")
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id){
        importOrderService.approve(id);
        return Result.successMessage("订单已通过！");
    }

    //5. 拒绝订单
    @OpLogs(module = "订单", description = "拒绝进货订单")
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id){
        importOrderService.reject(id);
        return Result.successMessage("订单已拒绝！");
    }

    //6. 获取订单详情
    @GetMapping("/search/{id}")
    public Result<ImportOrderWithItemsInfo> search(@PathVariable Long id){
        return Result.success(importOrderService.search(id));
    }

    //7. 获取订单列表
    @GetMapping("/page")
    public Result<PageResult<ImportOrderInfo>> searchPage(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) AuditStatusEnum status,
                                                          @Valid PageParam param) {
        Pageable pageable = param.toPageable();
        return Result.success(importOrderService.searchPage(keyword, status, pageable));
    }

    //8. 删除订单
    @OpLogs(module = "订单", description = "删除进货订单草稿")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id){
        importOrderService.delete(id);
        return Result.successMessage("草稿已删除！");
    }
}
