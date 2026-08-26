package com.superkl.backend.controller.stock;

import com.superkl.backend.common.PageParam;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.dto.stock.WarehouseStockUpdateDto;
import com.superkl.backend.info.stock.WareHouseStockInfo;
import com.superkl.backend.service.stock.WareHouseStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class WareHouseStockController {
    private final WareHouseStockService wareHouseStockService;

    @GetMapping("/search/{warehouseId}/{productId}")
    public Result<List<WareHouseStockInfo>> search(
            @PathVariable Long warehouseId,
            @PathVariable Long productId) {
        return Result.success(wareHouseStockService.findByWareHouseIdAndProductId(warehouseId, productId));
    }

    @PutMapping("/batch-update")
    public Result<Void> batchUpdate(@RequestBody @Valid List<WarehouseStockUpdateDto> dtos) {
        wareHouseStockService.batchUpdate(dtos);
        return Result.successMessage("更新成功");
    }

    @GetMapping("/page")
    public Result<PageResult<WareHouseStockInfo>> page(
            @RequestParam Long warehouseId,
            @Valid PageParam pageParam) {
        Pageable pageable = pageParam.toPageable();
        return Result.success(wareHouseStockService.findByWareHouseId(warehouseId, pageable));
    }
}
