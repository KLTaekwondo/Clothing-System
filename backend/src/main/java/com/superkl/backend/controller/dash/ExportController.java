package com.superkl.backend.controller.dash;

import com.superkl.backend.common.Result;
import com.superkl.backend.info.dash.OrderDashInfo;
import com.superkl.backend.info.product.ProductInfo;
import com.superkl.backend.service.dash.OrderDashboardService;
import com.superkl.backend.service.product.ProductService;
import com.superkl.backend.utils.CSVUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    private final OrderDashboardService orderDashboardService;
    private final ProductService productService;

    @GetMapping("/export-between")
    public void exportBetween(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
                              HttpServletResponse response) throws Exception {

        CSVUtil.setResponseHeaders(response, "期间订单总计_" + from + "_" + to);
        PrintWriter writer = response.getWriter();
        CSVUtil.writeBom(writer);
        CSVUtil.writeLine(writer, "日期", "销售额", "成本", "毛利", "毛利率");

        LocalDate currentDate = from;
        while (!currentDate.isBefore(to)) {
            OrderDashInfo info = orderDashboardService.getCustomInfo(currentDate, currentDate.plusDays(1));
            CSVUtil.writeLine(writer,
                    currentDate.toString(),
                    info.getSaleAmount().toString(),
                    info.getImportAmount().toString(),
                    info.getProfit().toString(),
                    info.getMarginRate().toString());
            currentDate = currentDate.plusDays(1);
        }
        writer.flush();
    }

    @GetMapping("/products")
    public void exportProducts(HttpServletResponse response) throws Exception {
        CSVUtil.setResponseHeaders(response, "商品列表_" + LocalDate.now());
        PrintWriter writer = response.getWriter();
        CSVUtil.writeBom(writer);
        CSVUtil.writeLine(writer, "商品编码", "商品名称", "季节", "类型", "种类", "单位", "面料", "年份", "进价", "售价", "特价", "状态");

        List<ProductInfo> products = productService.getProductList();
        for (ProductInfo p : products) {
            CSVUtil.writeLine(writer,
                    p.getCode(),
                    p.getName(),
                    p.getSeason().name(),
                    p.getType(),
                    p.getCategory(),
                    p.getUnit(),
                    p.getComposition(),
                    p.getYear(),
                    p.getImportPrice().toPlainString(),
                    p.getSalePrice().toPlainString(),
                    p.isSpecial() ? "是" : "否",
                    p.getStatus().name()
            );
        }
        writer.flush();
    }
}
