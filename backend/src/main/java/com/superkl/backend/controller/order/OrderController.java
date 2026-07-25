package com.superkl.backend.controller.order;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.order.OrderCreateDto;
import com.superkl.backend.info.order.OrderInfo;
import com.superkl.backend.info.order.OrderWithItemsInfo;
import com.superkl.backend.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // 1.完成订单
    @PostMapping("/complete")
    public Result<Void> complete(@Valid @RequestBody OrderCreateDto dto) {
        orderService.complete(dto);
        return Result.successMessage("订单已完成！");
    }

    // 2.挂单订单
    @PostMapping("/draft")
    public Result<Void> draft(@Valid @RequestBody OrderCreateDto dto) {
        orderService.draft(dto);
        return Result.successMessage("订单已挂单！");
    }

    // 3.创建订单
    @PostMapping("/draft/update")
    public Result<Void> updateDraft(@Valid @RequestBody OrderCreateDto dto) {
        orderService.update(dto);
        return Result.successMessage("订单已更新！");
    }

    // 4.查询单个订单
    @GetMapping("/search/{orderId}")
    public Result<OrderWithItemsInfo> search(@PathVariable Long orderId) {
        return Result.success(orderService.search(orderId));
    }

    // 5.查询订单订单列表
    @GetMapping("/search/list")
    public Result<List<OrderInfo>> searchList() {
        return Result.success(orderService.searchList());
    }

    // 6.删除草稿订单
    @DeleteMapping("/delete/{orderId}")
    public Result<Void> deleteDraft(@PathVariable Long orderId) {
        orderService.deleteDraft(orderId);
        return Result.successMessage("订单已删除！");
    }
}
