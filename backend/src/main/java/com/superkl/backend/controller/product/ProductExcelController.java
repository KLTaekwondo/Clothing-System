package com.superkl.backend.controller.product;

import com.superkl.backend.service.product.ProductExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
