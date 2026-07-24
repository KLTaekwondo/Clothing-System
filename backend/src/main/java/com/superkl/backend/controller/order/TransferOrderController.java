package com.superkl.backend.controller.order;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.order.TransferOrderDraftDto;
import com.superkl.backend.info.order.TransferOrderInfo;
import com.superkl.backend.info.order.TransferOrderWithItemsInfo;
import com.superkl.backend.service.order.TransferOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-order")
@RequiredArgsConstructor
public class TransferOrderController {
    private final TransferOrderService transferOrderService;

    // 创建订单
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody TransferOrderDraftDto transferOrderDraftDto) {
        transferOrderService.create(transferOrderDraftDto);
        return Result.successMessage("库存调拨订单创建成功!");
    }

    // 更新订单
    @PutMapping("/update/{transferOrderId}")
    public Result<Void> update(@PathVariable Long transferOrderId,
                               @Valid @RequestBody TransferOrderDraftDto transferOrderDraftDto) {
        transferOrderService.update(transferOrderId, transferOrderDraftDto);
        return Result.successMessage("库存调拨订单更新成功!");
    }

    // 提交订单
    @PutMapping("/check/{transferOrderId}")
    public Result<Void> check(@PathVariable Long transferOrderId) {
        transferOrderService.check(transferOrderId);
        return Result.successMessage("库存调拨订单提交成功!");
    }

    // 审核(确认订单)
    @PutMapping("/approve/{transferOrderId}")
    public Result<Void> approve(@PathVariable Long transferOrderId) {
        transferOrderService.approve(transferOrderId);
        return Result.successMessage("库存调拨订单审核成功!");
    }

    // 拒绝订单
    @PutMapping("/reject/{transferOrderId}")
    public Result<Void> reject(@PathVariable Long transferOrderId) {
        transferOrderService.reject(transferOrderId);
        return Result.successMessage("库存调拨订单已拒绝!");
    }

    // 删除订单
    @DeleteMapping("/delete/{transferOrderId}")
    public Result<Void> delete(@PathVariable Long transferOrderId) {
        transferOrderService.delete(transferOrderId);
        return Result.successMessage("库存调拨订单删除成功!");
    }

    // 获取订单详情
    @GetMapping("/search/{transferOrderId}")
    public Result<TransferOrderWithItemsInfo> search(@PathVariable Long transferOrderId) {
        return Result.success(transferOrderService.search(transferOrderId));
    }

    // 获取订单列表
    @GetMapping("/search/list")
    public Result<List<TransferOrderInfo>> searchList() {
        return Result.success(transferOrderService.searchList());
    }
}
