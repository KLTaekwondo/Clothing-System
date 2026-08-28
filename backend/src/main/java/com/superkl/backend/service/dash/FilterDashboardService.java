package com.superkl.backend.service.dash;

import com.superkl.backend.common.ManageQueryParams;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.dash.OrderDashInfo;
import com.superkl.backend.repository.basic.EmployeeRepository;
import com.superkl.backend.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
public class FilterDashboardService {
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;

    // 日订单
    public OrderDashInfo getDailyFilterDashInfo(ManageQueryParams params){
        // 手动调整，查询当前天的订单，避免使用前端的默认时间范围，减少前端的复杂度和错误风险
        params.setStart(LocalDate.now());
        params.setEnd(LocalDate.now().plusDays(1));
        return getFilterCustomInfo(params);
    }

    // 周订单
    public OrderDashInfo getWeeklyFilterDashInfo(ManageQueryParams params){
        // 手动调整，查询当前周的订单，避免使用前端的默认时间范围，减少前端的复杂度和错误风险
        LocalDate today = LocalDate.now();
        // 找出本周的周一日期
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusWeeks(1);


        params.setStart(weekStart);
        params.setEnd(weekEnd);
        return getFilterCustomInfo(params);
    }

    // 月订单
    public OrderDashInfo getMonthlyFilterDashInfo(ManageQueryParams params){
        // 手动调整，查询当前月的订单，避免使用前端的默认时间范围，减少前端的复杂度和错误风险
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).withDayOfMonth(1);

        params.setStart(monthStart);
        params.setEnd(monthEnd);
        return getFilterCustomInfo(params);
    }

    // 年订单
    public OrderDashInfo getYearlyFilterDashInfo(ManageQueryParams params){
        // 手动调整，查询当前年的订单，避免使用前端的默认时间范围，减少前端的复杂度和错误风险
        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate yearEnd = yearStart.plusYears(1);

        params.setStart(yearStart);
        params.setEnd(yearEnd);
        return getFilterCustomInfo(params);
    }

    // 通用方法（可选参数）
    // 自定义区间订单
    public OrderDashInfo getFilterCustomInfo(ManageQueryParams params){
        BigDecimal importAmount = sumImportAmountByChooseParams(params);
        BigDecimal saleAmount = sumAmountByChooseParams(params);
        BigDecimal profit = saleAmount.subtract(importAmount);
        BigDecimal marginRate = BigDecimal.ZERO;

        // 计算毛利率
        if(saleAmount.compareTo(BigDecimal.ZERO) != 0){
            marginRate = profit.divide(saleAmount, 4, RoundingMode.HALF_UP);
        }

        return OrderDashInfo.builder()
                .saleAmount(saleAmount)
                .importAmount(importAmount)
                .marginRate(marginRate)
                .profit(profit)
                .build();
    }

    // 辅助方法
    // 管理端查看总汇总（可选参数）
    private BigDecimal sumAmountByChooseParams(ManageQueryParams params){
        // 查筛选条件是否冲突
        Long wareHouseId = null;
        if(params.getEmployeeId() != null) {
            wareHouseId = employeeRepository.findById(params.getEmployeeId())
                    .orElseThrow(() -> new BusinessException(403, "员工不存在")).getWareHouse().getWareHouseId();
        }
        // 查筛选条件是否冲突
        if(params.getWareHouseId() != null && !params.getWareHouseId().equals(wareHouseId) && params.getEmployeeId() != null) {
            throw new BusinessException(403,"员工和仓库不匹配，建议修改查询条件");
        }
        return orderRepository.sumTotalAmountByTime(params.getStart().atStartOfDay(),
                params.getEnd().atStartOfDay(),
                DirectionEnum.IN,
                DirectionEnum.OUT,
                params.getOrderStatus(),
                params.getPayMethod(),
                params.getWareHouseId(),
                params.getEmployeeId());
    }

    // 管理端查看总进货汇总（可选参数）
    private BigDecimal sumImportAmountByChooseParams(ManageQueryParams params){
        // 查筛选条件是否冲突
        Long wareHouseId = null;
        if(params.getEmployeeId() != null) {
            wareHouseId = employeeRepository.findById(params.getEmployeeId())
                    .orElseThrow(() -> new BusinessException(403, "员工不存在")).getWareHouse().getWareHouseId();
        }
        // 查筛选条件是否冲突
        if(params.getWareHouseId() != null && !params.getWareHouseId().equals(wareHouseId) && params.getEmployeeId() != null) {
            throw new BusinessException(403,"员工和仓库不匹配，建议修改查询条件");
        }
        return orderRepository.sumImportAmountByTime(params.getStart().atStartOfDay(),
                params.getEnd().atStartOfDay(),
                DirectionEnum.IN,
                DirectionEnum.OUT,
                params.getOrderStatus(),
                params.getPayMethod(),
                params.getWareHouseId(),
                params.getEmployeeId());
    }
}
