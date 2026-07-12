package com.superkl.backend.controller;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.StockTransferDto;
import com.superkl.backend.dto.WarehouseStockUpdateDto;
import com.superkl.backend.info.WareHouseStockInfo;
import com.superkl.backend.service.WareHouseStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PutMapping("/transfer")
    public Result<Void> transfer(@RequestBody @Valid StockTransferDto dto) {
        wareHouseStockService.transfer(dto);
        return Result.successMessage("调货成功");
    }
}
