package com.superkl.backend.controller.stock;

import com.superkl.backend.common.PageParam;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.info.stock.StockRecordInfo;
import com.superkl.backend.service.stock.StockRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-record")
@RequiredArgsConstructor
public class StockRecordController {
    private final StockRecordService stockRecordService;

    //1. 获取库存记录列表
    @GetMapping("/page")
    public Result<PageResult<StockRecordInfo>> searchPage(@Valid PageParam param) {
        return Result.success(stockRecordService.searchPage(param.toPageable()));
    }
}
