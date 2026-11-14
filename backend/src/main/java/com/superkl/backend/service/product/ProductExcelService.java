package com.superkl.backend.service.product;

import com.superkl.backend.common.ExcelPullList;
import com.superkl.backend.enums.OptionTypeEnum;
import com.superkl.backend.enums.ProductExcelEnum;
import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.info.product.OptionValueInfo;
import com.superkl.backend.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductExcelService {
    private final OptionValueService optionValueService;

    private static final String SHEET_NAME = "商品导入";
    private static final String FILE_NAME = "商品导入模板";

    // 获取商品导入模板
    public void getTemplate(HttpServletResponse response) throws IOException {
        // 表头和列宽都从枚举长出来，保证和取值下标永远一致
        List<String> headers = ProductExcelEnum.getTitles();
        List<Integer> widths = ProductExcelEnum.getWidths();

        ExcelPullList[] pullLists = {
                new ExcelPullList(ProductExcelEnum.SEASON.getIndex(), getSeasonList()),
                new ExcelPullList(ProductExcelEnum.TYPE.getIndex(), getOptionList(OptionTypeEnum.TYPE)),
                new ExcelPullList(ProductExcelEnum.CATEGORY.getIndex(), getOptionList(OptionTypeEnum.CATEGORY)),
                new ExcelPullList(ProductExcelEnum.UNIT.getIndex(), getOptionList(OptionTypeEnum.UNIT)),
                new ExcelPullList(ProductExcelEnum.COMPOSITION.getIndex(), getOptionList(OptionTypeEnum.COMPOSITION)),
                new ExcelPullList(ProductExcelEnum.YEAR.getIndex(), getOptionList(OptionTypeEnum.YEAR)),
                new ExcelPullList(ProductExcelEnum.SPECIAL.getIndex(), List.of("是", "否")),
        };

        // 创建工作簿，同时自动兜底关闭流
        try (XSSFWorkbook wb = ExcelUtil.createWorkbook(SHEET_NAME, headers, widths, pullLists)) {
            // 写入响应
            ExcelUtil.writeToResponse(wb, response, FILE_NAME);
        }
    }

    // 提取选项值列表
    private List<String> getOptionList(OptionTypeEnum optionType) {
        // 查询选项值列表
        return optionValueService.searchListByType(optionType.name()).stream()
                .map(OptionValueInfo::getOptionValue)
                .toList();
    }

    // 提取季节枚举值列表
    private List<String> getSeasonList() {
        // 查询季节枚举值列表
        return Arrays.stream(SeasonEnum.values())
                .map(SeasonEnum::getDesc)
                .toList();
    }
}
