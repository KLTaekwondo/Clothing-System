package com.superkl.backend.service.product;

import com.superkl.backend.common.excel.ExcelImportResult;
import com.superkl.backend.common.excel.ExcelPullList;
import com.superkl.backend.common.excel.ExcelRow;
import com.superkl.backend.dto.product.ExcelPreProductCreateDto;
import com.superkl.backend.dto.product.OptionValueCreateDto;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductExcelService {
    private final OptionValueService optionValueService;
    private final ProductService productService;
    private final ProductRepository productRepository;

    private static final String SHEET_NAME = "商品导入";
    private static final String FILE_NAME = "商品导入模板";

    // 获取商品导入模板
    public void getTemplate(HttpServletResponse response) throws IOException {
        // 表头和列宽都从枚举长出来，保证和取值下标永远一致
        List<String> headers = ProductExcelEnum.getTitles();
        List<Integer> widths = ProductExcelEnum.getWidths();

        ExcelPullList[] pullLists = {
                new ExcelPullList(ProductExcelEnum.SEASON.getIndex(), SeasonEnum.getSeasonList(), true),
                new ExcelPullList(ProductExcelEnum.TYPE.getIndex(), getOptionList(OptionTypeEnum.TYPE)),
                new ExcelPullList(ProductExcelEnum.CATEGORY.getIndex(), getOptionList(OptionTypeEnum.CATEGORY)),
                new ExcelPullList(ProductExcelEnum.UNIT.getIndex(), getOptionList(OptionTypeEnum.UNIT)),
                new ExcelPullList(ProductExcelEnum.COMPOSITION.getIndex(), getOptionList(OptionTypeEnum.COMPOSITION)),
                new ExcelPullList(ProductExcelEnum.YEAR.getIndex(), getOptionList(OptionTypeEnum.YEAR)),
                new ExcelPullList(ProductExcelEnum.SPECIAL.getIndex(), List.of("是", "否"), true),
        };

        // 创建工作簿，同时自动兜底关闭流
        try (XSSFWorkbook wb = ExcelUtil.createWorkbook(SHEET_NAME, headers, widths, pullLists)) {
            // 写入响应
            ExcelUtil.writeToResponse(wb, response, FILE_NAME);
        }
    }

    // 检查商品导入模板是否符合要求
    public ExcelImportResult validate(MultipartFile file) {
        // 先准备数据结构
        ExcelImportResult result = new ExcelImportResult();
        // 获取所有季节列表
        List<String> seasons = SeasonEnum.getSeasonList();

        // 1. 读取数据行
        List<ExcelRow> rows = ExcelUtil.readRows(file);
        // 1.1 提取所有商品编码, 并检查是否存在重复
        Set<String> seenCodes = new HashSet<>();
        List<String> allCodes = getAllCodes(rows);
        List<String> existingCodes = productRepository.findExistingCodes(allCodes);

        // 2. 检查数据行是否符合要求
        // 2.1 检查全部数据行是否为空
        if (rows.isEmpty()) {
            result.getFatalErrors().add("数据行不能为空");
        }

        for(ExcelRow row : rows) {
            String[] data = row.getData();

            // 2.2 检查数据库中是否已经有相同商品编码的商品
            String code = data[ProductExcelEnum.CODE.getIndex()];
            if (code == null || code.isBlank()) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:商品编码不能为空");
            } else {
                // 文件内重复
                if (!seenCodes.add(code)) {
                    result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:商品编码 " + code + " 在文件中重复");
                }
                // 库里已存在
                if (existingCodes.contains(code)) {
                    result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:商品编码 " + code + " 已存在");
                }
            }

            // 2.3 检查商品名称是否为空
            String name = data[ProductExcelEnum.NAME.getIndex()];
            if(name == null || name.isBlank()) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:商品名称不能为空");
            }else if(name.length() > 20) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:商品名称不能超过20个字符");
            }

            // 2.4 检查导入价格是否大于等于销售价格
            BigDecimal importPrice = checkPrice(data[ProductExcelEnum.IMPORT_PRICE.getIndex()], row, "进货价格", result);
            BigDecimal salePrice = checkPrice(data[ProductExcelEnum.SALE_PRICE.getIndex()], row, "销售价格", result);

            if(importPrice != null && salePrice != null && importPrice.compareTo(salePrice) >= 0) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:进货价格不能大于等于销售价格");
            }

            // 2.5 检查规格在数据库中是否都存在
            // 2.5.1 季节检查
            String season = data[ProductExcelEnum.SEASON.getIndex()];
            if(season.isEmpty() || !seasons.contains(season)) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:季节 " + season + " 不存在");
            }

            // 2.5.2 类型检查
            String type = data[ProductExcelEnum.TYPE.getIndex()];
            data[ProductExcelEnum.TYPE.getIndex()] = checkSpec(OptionTypeEnum.TYPE, row, type, result);
            // 2.5.3 种类检查
            String category = data[ProductExcelEnum.CATEGORY.getIndex()];
            data[ProductExcelEnum.CATEGORY.getIndex()] = checkSpec(OptionTypeEnum.CATEGORY, row, category, result);
            // 2.5.4 单位检查
            String unit = data[ProductExcelEnum.UNIT.getIndex()];
            data[ProductExcelEnum.UNIT.getIndex()] = checkSpec(OptionTypeEnum.UNIT, row, unit, result);
            // 2.5.5 组合检查
            String composition = data[ProductExcelEnum.COMPOSITION.getIndex()];
            data[ProductExcelEnum.COMPOSITION.getIndex()] = checkSpec(OptionTypeEnum.COMPOSITION, row, composition, result);
            // 2.5.6 年份检查
            String year = data[ProductExcelEnum.YEAR.getIndex()];
            data[ProductExcelEnum.YEAR.getIndex()] = checkSpec(OptionTypeEnum.YEAR, row, year, result);

            // 2.5.7 文本长度检查（和 ProductCreateDto 的 @Size 保持一致，统一 20 位）
            checkLength(result.getFatalErrors(), row, "商品编码", data[ProductExcelEnum.CODE.getIndex()]);
            checkLength(result.getFatalErrors(), row, "类型", data[ProductExcelEnum.TYPE.getIndex()]);
            checkLength(result.getFatalErrors(), row, "种类", data[ProductExcelEnum.CATEGORY.getIndex()]);
            checkLength(result.getFatalErrors(), row, "单位", data[ProductExcelEnum.UNIT.getIndex()]);
            checkLength(result.getFatalErrors(), row, "面料", data[ProductExcelEnum.COMPOSITION.getIndex()]);

            // 2.5.7.1 颜色检查，因为颜色有很多，所以需要逐个检查
            List<String> colors = getAllOption(row, ProductExcelEnum.COLORS, OptionTypeEnum.COLOR, result);
            if (colors.isEmpty()) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:颜色不能为空");
            }


            // 2.5.7.2 尺寸检查，因为尺寸有很多，所以需要逐个检查
            List<String> sizes = getAllOption(row, ProductExcelEnum.SIZES, OptionTypeEnum.SIZE, result);
            if (sizes.isEmpty()) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:尺寸不能为空");
            }

            // 2.6 特价检查
            String special = data[ProductExcelEnum.SPECIAL.getIndex()];
            if(!"是".equals(special) && !"否".equals(special)) {
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:特价只能是是或否");
            }

            // 2.7 构造ExcelPreProductCreateDto对象
            ExcelPreProductCreateDto dto = ExcelPreProductCreateDto.builder()
                    .code(code)
                    .name(name)
                    .importPrice(importPrice)
                    .salePrice(salePrice)
                    .season(SeasonEnum.seasonOf(season))
                    .type(type)
                    .category(category)
                    .unit(unit)
                    .composition(composition)
                    .year(year)
                    .colors(colors)
                    .sizes(sizes)
                    .special("是".equals(special))
                    .build();

            result.getCheckedDtos().add(dto);
        }

        return result;
    }

    @Transactional
    public void importProducts(MultipartFile file , boolean autoCreateMissing) {
        ExcelImportResult result = validate(file);

        // 有任何验证问题就不导入商品
        if (!result.getFatalErrors().isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.RULE_EXCEL_ERROR , "校验失败，存在严重错误：" + result.getFatalErrors());
        }

        // 检查是否自动创建缺失的规格，如果没有直接抛出异常
        if(!autoCreateMissing && !result.getErrors().isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.RULE_EXCEL_ERROR , "你未自动创建缺失的规格，存在缺失的规格，无法导入" + result.getErrors());
        }

        // 启用了自动创建，并且存在缺失的规格，需要先创建缺失的规格，再导入商品
        if(autoCreateMissing && !result.getErrors().isEmpty()) {
            // 取出缺失的规格值
            result.getNotSeenOptions().forEach((optionType, notSeenValues) -> {
                if(!notSeenValues.isEmpty()){
                    // 创建缺失的规格
                    notSeenValues.forEach(value -> {
                        OptionValueCreateDto optionValueCreateDto = new OptionValueCreateDto(optionType , value);
                        optionValueService.generateOptions(optionValueCreateDto);
                    });
                }
            });
        }

        // 最后直接转换入库
        // 校验过的 DTO → 直接落库
        result.getCheckedDtos().stream()
                .map(ExcelPreProductCreateDto::toProductCreateDto)
                .forEach(productService::create);

    }



    // 提取选项值列表
    private List<String> getOptionList(OptionTypeEnum optionType) {
        // 查询选项值列表
        return optionValueService.searchListByType(optionType.name()).stream()
                .map(OptionValueInfo::getOptionValue)
                .toList();
    }

    // 规格检查
    private String checkSpec(OptionTypeEnum optionType, ExcelRow row, String value, ExcelImportResult result) {
        // 检查规格值是否为空, 如果为空则抛出异常,校验直接失败，不返回任何错误报告，避免后续导入的时候引入空规格
        if(value == null || value.isEmpty()){
            result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + "不能为空");
            return "";
        }
        // 不为空，检查规格值是否存在数据库中
        boolean exists = optionValueService.existsByTypeAndValue(optionType, value);

        // 如果是年份，需要额外检查
        if(OptionTypeEnum.YEAR.equals(optionType)){
            if(!value.matches("\\d{4}"))
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + " [" + value + "] 不是四位数字");
            else if(!value.matches("20\\d{2}"))
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + " [" + value + "] 不是20开头的年份");
            return value;
        }


        // 如果没有出现，则模糊对比是否存在相似值
        if(!exists){
            List<String> likeValues = optionValueService.likeSearch(optionType, "%" + value + "%");
            // 如果返回结果只有一个，直接返回
            if(likeValues.size() == 1){
                return likeValues.get(0);
            }else if(likeValues.isEmpty()){
                // 如果没有相似值，提示用户输入错误
                result.getErrors().add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + "[" + value + "] 不存在");
                result.getNotSeenOptions().get(optionType).add(value);
            } else{
                // 如果存在多个值，提示用户手动精确确认
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + optionType.getDesc() + "存在多个相似值:" + likeValues);
            }
        }

        return value;
    }

    // 文本长度检查：和 ProductCreateDto 的 @Size(max = 20) 对齐
    private void checkLength(List<String> fatalErrors, ExcelRow row, String label, String value) {
        if (value != null && value.length() > 20) {
            fatalErrors.add("第 " + row.getRowIndex() + " 行:" + label + "不能超过20个字符");
        }
    }

    // 数值检查
    private BigDecimal checkPrice(String value, ExcelRow row, String fieldName, ExcelImportResult result) {
        if(value == null || value.isEmpty()){
            result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + fieldName + " 不能为空");
            return null;
        }

        try{
            BigDecimal price = new BigDecimal(value);
            if(price.compareTo(BigDecimal.ZERO) < 0){
                result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + fieldName + " [" + value + "]不能小于0");
            }
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            result.getFatalErrors().add("第 " + row.getRowIndex() + " 行:" + fieldName + " [" + value + "]不是纯数字");
        }

        return null;
    }

    // 提取所有商品编码
    private List<String> getAllCodes(List<ExcelRow> rows) {
        return rows.stream()
                .map(ExcelRow::getData)
                .map(data -> data[ProductExcelEnum.CODE.getIndex()])
                .toList();
    }

    // 解析一格里的多个值：拆分 -> 逐个校验并修正 -> 去重
    // distinct 放在 map 之后，"红"和"红色"会合并成一个
    private List<String> getAllOption(ExcelRow row, ProductExcelEnum column,
                                      OptionTypeEnum optionType, ExcelImportResult result) {
        int index = column.getIndex();

        return Arrays.stream(row.getData()[index].split("[,，/]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> checkSpec(optionType, row, s, result))
                .distinct()
                .toList();
    }


}
