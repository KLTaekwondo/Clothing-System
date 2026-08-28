package com.superkl.backend.service.dash;

import com.superkl.backend.common.ManageQueryParams;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import com.superkl.backend.info.dash.CheckoutDashInfo;
import com.superkl.backend.info.dash.DayInfo;
import com.superkl.backend.info.dash.OrderDashInfo;
import com.superkl.backend.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDashboardService {
    private final OrderRepository orderRepository;
    private final CacheDashService cacheDashService;

    // =====每日数据======
    // 查询当日的全部数据
    public OrderDashInfo getDailyInfo(){
        return getCustomInfo(LocalDate.now(), LocalDate.now().plusDays(1));
    }

    // =====每周数据======
    // 查询本周的全部数据
    public OrderDashInfo getWeeklyInfo(){
        LocalDate today = LocalDate.now();
        // 找出本周的周一日期
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusWeeks(1);
        return getCustomInfo(weekStart, weekEnd);
    }

    // =====每月数据=====
    // 获取本月的全部数据
    public OrderDashInfo getMonthlyInfo(){
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).withDayOfMonth(1);
        return getCustomInfo(monthStart, monthEnd);
    }

    // =====每年数据======
    // 获取本年的全部数据
    public OrderDashInfo getYearlyInfo(){
        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate yearEnd = yearStart.plusYears(1);
        return getCustomInfo(yearStart, yearEnd);
    }

    // =====自定义日期数据=====
    // 查询自定义日期的全部数据
    public OrderDashInfo getCustomInfo(LocalDate start, LocalDate end){
        BigDecimal salesAmount =  sumSaleAmountByTimeAndStatus(start, end, OrderStatusEnum.COMPLETED);
        BigDecimal importAmount = sumImportAmountByTimeAndStatus(start, end, OrderStatusEnum.COMPLETED);
        BigDecimal profit = salesAmount.subtract(importAmount);
        // 计算自定义日期毛利率
        BigDecimal marginRate = BigDecimal.ZERO;
        // 如果自定义日期订单金额为0，则毛利率为0.0
        if(salesAmount.compareTo(BigDecimal.ZERO) != 0){
            marginRate = profit.divide(salesAmount, 4,  RoundingMode.HALF_UP);
        }
        return OrderDashInfo.builder()
                .saleAmount(salesAmount)
                .importAmount(importAmount)
                .marginRate(marginRate)
                .profit(profit)
                .build();
    }

    // 查询近七天的销售趋势
    public List<DayInfo> getSevenDaysInfo(){
        LocalDate today = LocalDate.now();
        List<DayInfo> dayInfoList = new ArrayList<>();
        for(int i = 6; i >= 0; i--){
            LocalDate day = today.minusDays(i);
            BigDecimal amount = cacheDashService.sumSomeDaySaleAmount(day);
            dayInfoList.add(DayInfo.builder()
                    .day(day)
                    .amount(amount)
                    .build());
        }
        return dayInfoList;
    }

    // 查询区间内的所有每日数据
    public List<DayInfo> getEveryDayInfo(LocalDate start, LocalDate end){
        List<DayInfo> dayInfoList = new ArrayList<>();
        for(LocalDate day = start; day.isBefore(end); day = day.plusDays(1)){
            dayInfoList.add(DayInfo.builder()
                    .day(day)
                    .amount(cacheDashService.sumSomeDaySaleAmount(day))
                    .build());
        }
        return dayInfoList;
    }

    // =========收银端专属=========
    public CheckoutDashInfo getCheckoutInfo(LocalDate start , LocalDate end){
        BigDecimal sumAmount = sumAmountByTimeAndWIdAndPayMethod(start, end, null);
        BigDecimal AlipayAmount = sumAmountByTimeAndWIdAndPayMethod(start, end, PayMethodEnum.ALIPAY);
        BigDecimal CashAmount = sumAmountByTimeAndWIdAndPayMethod(start, end, PayMethodEnum.CASH);
        BigDecimal CardAmount = sumAmountByTimeAndWIdAndPayMethod(start, end, PayMethodEnum.CARD);
        BigDecimal WeChatAmount = sumAmountByTimeAndWIdAndPayMethod(start, end, PayMethodEnum.WECHAT);
        BigDecimal TikTokWriteOffAmount = sumAmountByTimeAndWIdAndPayMethod(start, end, PayMethodEnum.TIKTOK_WRITE_OFF);

        return CheckoutDashInfo.builder()
                .sumAmount(sumAmount)
                .AlipayAmount(AlipayAmount)
                .CashAmount(CashAmount)
                .CardAmount(CardAmount)
                .WeChatAmount(WeChatAmount)
                .TikTokWriteOffAmount(TikTokWriteOffAmount)
                .build();
    }


    // 辅助函数
    // 管理端查看总汇总（不可选参数）
    private BigDecimal sumSaleAmountByTimeAndStatus(LocalDate start,
                                                    LocalDate end ,
                                                    OrderStatusEnum status){
        return orderRepository.sumTotalAmountByTime(start.atStartOfDay(),
                end.atStartOfDay(),
                DirectionEnum.IN,
                DirectionEnum.OUT,
                status,
                null,
                null,
                null);
    }

    // 管理端查看总进货汇总（不可选参数）
    private BigDecimal sumImportAmountByTimeAndStatus(LocalDate start,
                                                      LocalDate end ,
                                                      OrderStatusEnum status){
        return orderRepository.sumImportAmountByTime(start.atStartOfDay(),
                end.atStartOfDay(),
                DirectionEnum.IN,
                DirectionEnum.OUT,
                status,
                null,
                null,
                null);
    }

    // 收银端查看汇总
    private BigDecimal sumAmountByTimeAndWIdAndPayMethod(LocalDate start, LocalDate end, PayMethodEnum payMethod){
        Long wareHouseId = RequestUser.notNull().getRequestId();

        return orderRepository.sumAmountByTimeAndWId(start.atStartOfDay(),
                end.atStartOfDay(),
                DirectionEnum.IN,
                DirectionEnum.OUT,
                OrderStatusEnum.COMPLETED,
                wareHouseId,
                payMethod);
    }
}
