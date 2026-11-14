package com.superkl.backend.service.product;

import com.superkl.backend.common.ExcelPullList;
import com.superkl.backend.common.ExcelRow;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.OptionTypeEnum;
import com.superkl.backend.enums.ProductExcelEnum;
import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.product.OptionValueInfo;
import com.superkl.backend.repository.product.ProductRepository;
import com.superkl.backend.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductExcelService {
    private final OptionValueService optionValueService;
    private final ProductRepository productRepository;

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

    // 检查商品导入模板是否符合要求
    public List<String> validate(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        // 1. 读取数据行
        List<ExcelRow> rows = ExcelUtil.readRows(file);
        // 2. 检查数据行是否符合要求
        // 2.1 检查数据行是否为空
        if (rows.isEmpty()) {
            errors.add("数据行不能为空");
        }

        // 获取所有季节列表
        List<String> seasons = getSeasonList();
        Set<String> seenCodes = new HashSet<>();
        // 提前检查所有商品编码是否都存在
        List<String> allCodes = rows.stream()
                .map(r -> r.getData()[ProductExcelEnum.CODE.getIndex()])
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .toList();

        List<String> existingCodes = productRepository.findExistingCodes(allCodes);

        for(ExcelRow row : rows) {
            String[] data = row.getData();
            // 2.2 检查数据库中是否已经有相同商品编码的商品
            String code = data[ProductExcelEnum.CODE.getIndex()];
            if (code == null || code.isBlank()) {
                errors.add("第 " + row.getRowIndex() + " 行:商品编码不能为空");
            } else {
                // 文件内重复
                if (!seenCodes.add(code)) {
                    errors.add("第 " + row.getRowIndex() + " 行:商品编码 " + code + " 在文件中重复");
                }
                // 库里已存在
                if (existingCodes.contains(code)) {
                    errors.add("第 " + row.getRowIndex() + " 行:商品编码 " + code + " 已存在");
                }
            }
            // 2.3 检查商品名称是否为空
            String name = data[ProductExcelEnum.NAME.getIndex()];
            if(name == null || name.isBlank()) {
                errors.add("第 " + row.getRowIndex() + " 行:商品名称不能为空");
            }
            // 2.4 检查导入价格是否大于等于销售价格
            BigDecimal importPrice = checkPrice(data[ProductExcelEnum.IMPORT_PRICE.getIndex()], row, "进货价格", errors);
            BigDecimal salePrice = checkPrice(data[ProductExcelEnum.SALE_PRICE.getIndex()], row, "销售价格", errors);

            if(importPrice != null && salePrice != null && importPrice.compareTo(salePrice) >= 0) {
                errors.add("第 " + row.getRowIndex() + " 行:进货价格不能大于等于销售价格");
            }
            // 2.5 检查规格在数据库中是否都存在
            // 2.5.1 季节检查
            String season = data[ProductExcelEnum.SEASON.getIndex()];
            if(season.isEmpty() || !seasons.contains(season)) {
                errors.add("第 " + row.getRowIndex() + " 行:季节 " + season + " 不存在");
            }
            // 2.5.2 类型检查
            checkSpec(OptionTypeEnum.TYPE, row, data[ProductExcelEnum.TYPE.getIndex()], errors);
            // 2.5.3 种类检查
            checkSpec(OptionTypeEnum.CATEGORY, row, data[ProductExcelEnum.CATEGORY.getIndex()], errors);
            // 2.5.4 单位检查
            checkSpec(OptionTypeEnum.UNIT, row, data[ProductExcelEnum.UNIT.getIndex()], errors);
            // 2.5.5 组合检查
            checkSpec(OptionTypeEnum.COMPOSITION, row, data[ProductExcelEnum.COMPOSITION.getIndex()], errors);
            // 2.5.6 年份检查
            checkSpec(OptionTypeEnum.YEAR, row, data[ProductExcelEnum.YEAR.getIndex()], errors);
            // 2.5.7.1 颜色检查，因为颜色有很多，所以需要逐个检查
            List<String> colors = Arrays.stream(data[ProductExcelEnum.COLORS.getIndex()].split("[,，/]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for(String color : colors) {
                checkSpec(OptionTypeEnum.COLOR, row, color, errors);
            }
            // 2.5.7.2 尺寸检查，因为尺寸有很多，所以需要逐个检查
            List<String> sizes = Arrays.stream(data[ProductExcelEnum.SIZES.getIndex()].split("[,，/]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for(String size : sizes) {
                checkSpec(OptionTypeEnum.SIZE, row, size, errors);
            }

            // 2.6 特价检查
            String special = data[ProductExcelEnum.SPECIAL.getIndex()];
            if(!"是".equals(special) && !"否".equals(special)) {
                errors.add("第 " + row.getRowIndex() + " 行:特价只能是是或否");
            }
        }
        return errors;
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

    // 规格检查
    private String checkSpec(OptionTypeEnum optionType, ExcelRow row, String value, List<String> errors) {
        // 检查规格值是否为空, 如果为空则抛出异常,校验直接失败，不返回任何错误报告，避免后续导入的时候引入空规格
        if(value == null || value.isEmpty()){
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR,
                    "第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + "不能为空");
        }
        // 不为空，检查规格值是否存在数据库中
        boolean exists = optionValueService.existsByTypeAndValue(optionType, value);


        // 如果没有出现，则模糊对比是否存在相似值
        if(!exists){
            List<String> likeValues = optionValueService.likeSearch(optionType, "%" + value + "%");
            // 如果返回结果只有一个，直接返回
            if(likeValues.size() == 1){
                return likeValues.get(0);
            }else if(likeValues.isEmpty()){
                // 如果没有相似值，提示用户输入错误
                errors.add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + "[" + value + "] 不存在");
            } else{
                // 如果存在多个值，提示用户手动精确确认
                errors.add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + "存在多个相似值:" + likeValues);
            }
        }

        return value;
    }

    // 数值检查
    private BigDecimal checkPrice(String value, ExcelRow row, String fieldName, List<String> errors) {
        if(value == null || value.isEmpty()){
            errors.add("第 " + row.getRowIndex() + " 行:" + fieldName + " 不能为空");
            return null;
        }

        try{
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            errors.add("第 " + row.getRowIndex() + " 行:" + value + " 不是纯数字");
        }

        return null;
    }
}
