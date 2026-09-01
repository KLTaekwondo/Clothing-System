package com.superkl.backend.service.springai;

import com.superkl.backend.info.dash.OrderDashInfo;
import com.superkl.backend.service.dash.OrderDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AIToolService {
    private final OrderDashboardService orderDashboardService;

    @Tool(description = "查询自定义订单数据:根据用户给出的查询时间段，查询出这段时间的销售额，进货额，毛利率，利润")
    public String customDashTool(LocalDate startDate, LocalDate endDate) {

        OrderDashInfo orderDashInfo = orderDashboardService.getCustomInfo(startDate, endDate);

        return startDate.toString() + "至" + endDate.toString() + "的订单数据如下：\n" +
                "销售额：" + orderDashInfo.getSaleAmount() + "\n" +
                "进货额：" + orderDashInfo.getImportAmount() + "\n" +
                "毛利率：" + orderDashInfo.getMarginRate() + "\n" +
                "利润：" + orderDashInfo.getProfit();
    }
}
