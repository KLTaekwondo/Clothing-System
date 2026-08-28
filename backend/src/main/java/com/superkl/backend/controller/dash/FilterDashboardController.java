package com.superkl.backend.controller.dash;

import com.superkl.backend.common.ManageQueryParams;
import com.superkl.backend.common.Result;
import com.superkl.backend.info.dash.OrderDashInfo;
import com.superkl.backend.service.dash.FilterDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dash/filter")
@RequiredArgsConstructor
public class FilterDashboardController {
    private final FilterDashboardService filterDashboardService;

    // 日订单
    @GetMapping("/daily")
    public Result<OrderDashInfo> getDailyFilterDashInfo(@Valid ManageQueryParams params) {
        return Result.success(filterDashboardService.getDailyFilterDashInfo(params));
    }

    // 周订单
    @GetMapping("/weekly")
    public Result<OrderDashInfo> getWeeklyFilterDashInfo(@Valid ManageQueryParams params) {
        return Result.success(filterDashboardService.getWeeklyFilterDashInfo(params));
    }

    // 月订单
    @GetMapping("/monthly")
    public Result<OrderDashInfo> getMonthlyFilterDashInfo(@Valid ManageQueryParams params) {
        return Result.success(filterDashboardService.getMonthlyFilterDashInfo(params));
    }

    // 年订单
    @GetMapping("/yearly")
    public Result<OrderDashInfo> getYearlyFilterDashInfo(@Valid ManageQueryParams params) {
        return Result.success(filterDashboardService.getYearlyFilterDashInfo(params));
    }

    // 自定义区间订单
    @GetMapping("/custom")
    public Result<OrderDashInfo> getFilterCustomInfo(@Valid ManageQueryParams params) {
        return Result.success(filterDashboardService.getFilterCustomInfo(params));
    }
}
