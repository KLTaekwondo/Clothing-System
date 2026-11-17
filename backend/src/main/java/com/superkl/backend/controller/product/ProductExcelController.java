package com.superkl.backend.controller.product;

import com.superkl.backend.annotation.OpLogs;
import com.superkl.backend.common.excel.ExcelImportResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.service.product.ProductExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/product-excel")
@RequiredArgsConstructor
public class ProductExcelController {
    private final ProductExcelService productExcelService;

    // 获取商品导入模板
    @GetMapping("/template")
    public void getTemplate(HttpServletResponse response) throws IOException {
        productExcelService.getTemplate(response);
    }

    // 检查商品导入模板是否符合要求
    @PostMapping("/validate")
    public Result<ExcelImportResult> validate(MultipartFile file) throws IOException {
        return Result.success(productExcelService.validate(file));
    }

    // 导入商品
    @OpLogs(module = "商品", description = "一键导入")
    @PostMapping("/import")
    public Result<Void> importProducts(MultipartFile file, boolean autoCreateMissing) throws IOException {
        productExcelService.importProducts(file, autoCreateMissing);
        return Result.successMessage("商品导入成功");
    }
}
