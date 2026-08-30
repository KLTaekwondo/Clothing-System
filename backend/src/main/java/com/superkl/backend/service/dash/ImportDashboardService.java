package com.superkl.backend.service.dash;

import com.superkl.backend.common.SeasonStartEnd;
import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.dash.ImportDashInfo;
import com.superkl.backend.repository.order.ImportOrderItemRepository;
import com.superkl.backend.repository.order.ImportOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportDashboardService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportOrderItemRepository importOrderItemRepository;

    // 单季度查询总金额和总数量
    public ImportDashInfo sumSeasonImportAmount(LocalDate someday) {
        int year = someday.getYear();
        int month = someday.getMonthValue();
        return calculateSeasonByTime(year, month);
    }

    // 全年查询总金额和总数量
    public List<ImportDashInfo> sumYearImportAmount(LocalDate someday) {
        int year = someday.getYear();

        ImportDashInfo spring = calculateSeasonByTime(year, 3);
        ImportDashInfo summer = calculateSeasonByTime(year, 6);
        ImportDashInfo autumn = calculateSeasonByTime(year, 9);
        ImportDashInfo winter = calculateSeasonByTime(year, 12);
        return List.of(spring, summer, autumn, winter);
    }

    // 供应商查询进货季度统计
    public ImportDashInfo sumSeasonImportAmountBySupplier(LocalDate someday, Long supplierId) {
        int year = someday.getYear();
        int month = someday.getMonthValue();
        return calculateSeasonBySupplierAndTime(year, month, supplierId);
    }

    // 供应商查询进货季度统计（按供应商）
    public List<ImportDashInfo> sumYearImportAmountBySupplier(LocalDate someday, Long supplierId) {
        int year = someday.getYear();

        ImportDashInfo spring = calculateSeasonBySupplierAndTime(year, 3, supplierId);
        ImportDashInfo summer = calculateSeasonBySupplierAndTime(year, 6, supplierId);
        ImportDashInfo autumn = calculateSeasonBySupplierAndTime(year, 9, supplierId);
        ImportDashInfo winter = calculateSeasonBySupplierAndTime(year, 12, supplierId);
        return List.of(spring, summer, autumn, winter);
    }


    // 辅助方法
    // 自定义查询进货金额
    private BigDecimal sumCustomImportAmount(LocalDate start , LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atStartOfDay();
        return importOrderRepository.sumAmountByStatusAndTime(DirectionEnum.IN,
                DirectionEnum.OUT,
                AuditStatusEnum.APPROVED,
                startDateTime,
                endDateTime);
    }

    // 自定义查询进货数量
    private Long sumCustomImportQuantity(LocalDate start , LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atStartOfDay();
        return importOrderItemRepository.sumQuantityByStatusAndTime(DirectionEnum.IN,
                DirectionEnum.OUT,
                AuditStatusEnum.APPROVED,
                startDateTime,
                endDateTime);
    }

    // 自定义查询进货金额（按供应商）
    private BigDecimal sumCustomImportAmountBySupplier(LocalDate start , LocalDate end, Long supplierId) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atStartOfDay();
        return importOrderRepository.sumAmountByStatusAndSupplierAndTime(DirectionEnum.IN,
                DirectionEnum.OUT,
                AuditStatusEnum.APPROVED,
                startDateTime,
                endDateTime,
                supplierId);
    }

    // 自定义查询进货数量（按供应商）
    private Long sumCustomImportQuantityBySupplier(LocalDate start , LocalDate end, Long supplierId) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atStartOfDay();
        return importOrderItemRepository.sumQuantityByStatusAndSupplierAndTime(DirectionEnum.IN,
                DirectionEnum.OUT,
                AuditStatusEnum.APPROVED,
                startDateTime,
                endDateTime,
                supplierId);
    }

    // 计算全部的进货的季度统计
    private ImportDashInfo calculateSeasonByTime(int year, int month) {
        // 先检查月份是否有效
        SeasonStartEnd seasonStartEnd = getSeasonStartEnd(year, month);

        // 提取季度、开始日期、结束日期
        SeasonEnum season = seasonStartEnd.getSeason();
        LocalDate start = seasonStartEnd.getStart();
        LocalDate end = seasonStartEnd.getEnd();

        // 组装
        return ImportDashInfo.builder()
                .season(season)
                .amount(sumCustomImportAmount(start, end))
                .quantity(sumCustomImportQuantity(start, end))
                .build();
    }

    // 计算供应商的进货季度统计
    private ImportDashInfo calculateSeasonBySupplierAndTime(int year, int month, Long supplierId) {
        // 先检查月份是否有效
        SeasonStartEnd seasonStartEnd = getSeasonStartEnd(year, month);

        // 提取季度、开始日期、结束日期
        SeasonEnum season = seasonStartEnd.getSeason();
        LocalDate start = seasonStartEnd.getStart();
        LocalDate end = seasonStartEnd.getEnd();

        // 组装
        return ImportDashInfo.builder()
                .season(season)
                .amount(sumCustomImportAmountBySupplier(start, end, supplierId))
                .quantity(sumCustomImportQuantityBySupplier(start, end, supplierId))
                .build();
    }
    // 计算季度（按月份）,采用中间类返回，尽量不使用Map等容器
    private SeasonStartEnd getSeasonStartEnd(int year, int month) {
        // 先检查月份是否有效
        if (month < 1 || month > 12) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "无效的月份！");
        }

        // 提前声明，避免后续if判断中重复声明
        SeasonEnum season;
        LocalDate start;
        LocalDate end;


        if (month <= 3) {
            // 春季
            season = SeasonEnum.SPRING;
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 4, 1);
        } else if (month <= 6) {
            // 夏季
            season = SeasonEnum.SUMMER;
            start = LocalDate.of(year, 4, 1);
            end = LocalDate.of(year, 7, 1);
        } else if (month <= 9) {
            // 秋季
            season = SeasonEnum.AUTUMN;
            start = LocalDate.of(year, 7, 1);
            end = LocalDate.of(year, 10, 1);
        } else {
            // 冬季
            season = SeasonEnum.WINTER;
            start = LocalDate.of(year, 10, 1);
            end = LocalDate.of(year + 1, 1, 1);
        }

        return SeasonStartEnd.builder()
                .start(start)
                .end(end)
                .season(season)
                .build();
    }
}
