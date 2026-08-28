package com.superkl.backend.controller.dash;

import com.superkl.backend.common.Result;
import com.superkl.backend.info.dash.ImportDashInfo;
import com.superkl.backend.service.dash.ImportDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dash/import-order")
@RequiredArgsConstructor
public class ImportDashboardController {
    private final ImportDashboardService importDashboardService;


    @GetMapping("/season")
    public Result<ImportDashInfo> sumSeasonImportAmount(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate someday)  {
        return Result.success(importDashboardService.sumSeasonImportAmount(someday));
    }

    @GetMapping("/year")
    public Result<List<ImportDashInfo>> sumYearImportAmount(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate someday)  {
        return Result.success(importDashboardService.sumYearImportAmount(someday));
    }

    @GetMapping("/season/{supplierId}")
    public Result<ImportDashInfo> sumSeasonImportAmountBySupplier(@PathVariable Long supplierId,
                                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate someday)  {
        return Result.success(importDashboardService.sumSeasonImportAmountBySupplier(someday, supplierId));
    }

    @GetMapping("/year/{supplierId}")
    public Result<List<ImportDashInfo>> sumYearImportAmountBySupplier(@PathVariable Long supplierId,
                                                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate someday)  {
        return Result.success(importDashboardService.sumYearImportAmountBySupplier(someday, supplierId));
    }
}
