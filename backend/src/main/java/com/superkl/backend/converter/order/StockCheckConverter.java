package com.superkl.backend.converter.order;

import com.superkl.backend.entity.order.StockCheck;
import com.superkl.backend.entity.order.StockCheckItem;
import com.superkl.backend.info.order.StockCheckInfo;
import com.superkl.backend.info.order.StockCheckItemInfo;
import com.superkl.backend.info.order.StockCheckWithItemsInfo;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class StockCheckConverter {
    private StockCheckConverter() {}// 私有构造方法，防止外部实例化

    // 实体转info
    public static StockCheckInfo toInfo(StockCheck stockCheck) {
        return StockCheckInfo.builder()
                .id(stockCheck.getStockCheckId())
                .stockCheckNo(stockCheck.getStockCheckNo())
                .wareHouseName(stockCheck.getWareHouseName())
                .wareHouseCode(stockCheck.getWareHouseCode())
                .status(stockCheck.getStatus())
                .remark(stockCheck.getRemark())
                .createTime(stockCheck.getCreateTime())
                .updateTime(stockCheck.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<StockCheckInfo> toInfoList(List<StockCheck> stockCheckList) {
        return stockCheckList.stream()
                .map(StockCheckConverter::toInfo)
                .toList();
    }

    // 实体分页转Info分页
    public static Page<StockCheckInfo> toInfoPage(Page<StockCheck> stockCheckPage) {
        return stockCheckPage.map(StockCheckConverter::toInfo);
    }

    // dto转实体
    public static StockCheck toEntity() {
        return StockCheck.builder()
                .stockCheckNo(generateStockCheckNo())
                .build();
    }

    // 返回带item的Info
    public static StockCheckWithItemsInfo toInfoWithItems(StockCheck stockCheck , List<StockCheckItemInfo> items) {
        return StockCheckWithItemsInfo.builder()
                .id(stockCheck.getStockCheckId())
                .stockCheckItems(items)
                .stockCheckNo(stockCheck.getStockCheckNo())
                .wareHouseName(stockCheck.getWareHouseName())
                .wareHouseCode(stockCheck.getWareHouseCode())
                .status(stockCheck.getStatus())
                .remark(stockCheck.getRemark())
                .createTime(stockCheck.getCreateTime())
                .updateTime(stockCheck.getUpdateTime())
                .build();
    }

    private static String generateStockCheckNo() {
        // 生成库存盘点单编号
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString();
        return "SC" + date + uuid;
       }
}
