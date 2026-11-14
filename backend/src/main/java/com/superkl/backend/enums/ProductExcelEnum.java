package com.superkl.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

// 商品导入模板的列定义
// 表头文字、列宽、取值下标都从这里长出来，改一处三处同步
// 注意：声明顺序就是模板里的列顺序
@Getter
@AllArgsConstructor
public enum ProductExcelEnum {
    CODE(0, "商品编码", 16),
    NAME(1, "商品名称", 30),
    SEASON(2, "季节", 12),
    TYPE(3, "类型", 14),
    CATEGORY(4, "种类", 14),
    UNIT(5, "单位", 10),
    COMPOSITION(6, "面料", 16),
    YEAR(7, "年份", 10),
    IMPORT_PRICE(8, "进价", 10),
    SALE_PRICE(9, "售价", 10),
    SPECIAL(10, "特价", 8),
    COLORS(11, "颜色", 28),
    SIZES(12, "尺码", 20);

    private final int index;
    private final String title;
    private final int width;

    public static List<String> getTitles() {
        return Arrays.stream(ProductExcelEnum.values())
                .map(ProductExcelEnum::getTitle)
                .toList();
    }

    public static List<Integer> getWidths() {
        return Arrays.stream(ProductExcelEnum.values())
                .map(ProductExcelEnum::getWidth)
                .toList();
    }
}
