package com.superkl.backend.controller.dash;

import com.superkl.backend.common.Result;
import com.superkl.backend.info.dash.CheckoutDashInfo;
import com.superkl.backend.info.dash.DayInfo;
import com.superkl.backend.info.dash.OrderDashInfo;
import com.superkl.backend.service.dash.OrderDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dash/order")
@RequiredArgsConstructor
public class OrderDashboardController {
    private final OrderDashboardService orderDashboardService;

    // 查询当日的全部数据
    @GetMapping("/daily")
    public Result<OrderDashInfo> getDailyInfo(){
        return Result.success(orderDashboardService.getDailyInfo());
    }

    // 查询本周的全部数据
    @GetMapping("/weekly")
    public Result<OrderDashInfo> getWeeklyInfo(){
        return Result.success(orderDashboardService.getWeeklyInfo());
    }

    // 查询本月的全部数据
    @GetMapping("/monthly")
    public Result<OrderDashInfo> getMonthlyInfo(){
        return Result.success(orderDashboardService.getMonthlyInfo());
    }

    // 查询本年的全部数据
    @GetMapping("/yearly")
    public Result<OrderDashInfo> getYearlyInfo(){
        return Result.success(orderDashboardService.getYearlyInfo());
    }

    // 查询所有数据
    @GetMapping("/custom")
    public Result<OrderDashInfo> getCustomInfo(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        return Result.success(orderDashboardService.getCustomInfo(startDate, endDate));
    }

    // 查询近七天的销售趋势
    @GetMapping("/sevenDays")
    public Result<List<DayInfo>> getSevenDaysInfo(){
        return Result.success(orderDashboardService.getSevenDaysInfo());
    }

    // 查询区间内的所有每日数据
    @GetMapping("/everyDay")
    public Result<List<DayInfo>> getEveryDayInfo(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        return Result.success(orderDashboardService.getEveryDayInfo(startDate, endDate));
    }

    // 收银页面专属
    @GetMapping("/checkout/custom")
    public Result<CheckoutDashInfo> getCheckoutCustomInfo(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        return Result.success(orderDashboardService.getCheckoutInfo(startDate, endDate));
    }
}
