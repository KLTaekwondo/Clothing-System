package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.ImportOrderItemConverter;
import com.superkl.backend.dto.order.ImportOrderItemCreateDto;
import com.superkl.backend.entity.order.ImportOrder;
import com.superkl.backend.entity.order.ImportOrderItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.ImportOrderItemInfo;
import com.superkl.backend.repository.order.ImportOrderItemRepository;
import com.superkl.backend.repository.product.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportOrderItemService {
    private final ImportOrderItemRepository importOrderItemRepository;
    private final ProductSkuRepository productSkuRepository;

    // 1.创建订单项
    public ImportOrderItem create(ImportOrderItemCreateDto dto, ImportOrder importOrder, List<String> errors){
        // 查找商品SKU是否存在
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode()).orElse(null);
        if(productSku == null){
            errors.add("商品SKU[" + dto.getSkuCode() + "]不存在！");
            return null;
        }

        String productName = productSku.getProduct().getProductName();
        String productCode = productSku.getProduct().getProductCode();
        String skuCode = productSku.getSkuCode();

        if(!productSku.getProduct().isEnabled()){
            errors.add(productName + "[" + productCode + "]商品已禁用！请检查后再操作！");
        }

        if(!productSku.isEnabled()){
            errors.add(productName + "[" + productCode + "]商品SKU[" + skuCode + "]已禁用！请检查后再操作！");
        }

        // 有错误，不创建订单项，返回null，避免调用日志记录
        if(!errors.isEmpty()) {
            return null;
        }

        // 转换为实体
        ImportOrderItem importOrderItem = ImportOrderItemConverter.toEntity(dto,productSku);
        importOrderItem.setImportOrder(importOrder);

        log.info("创建进货订单项：SKU={}，数量={}，金额={}", dto.getSkuCode(), dto.getImportQuantity(), importOrderItem.getTotalPrice());
        return importOrderItem;
    }

    // 2.创建订单项列表
    public List<ImportOrderItem> createList(List<ImportOrderItemCreateDto> dtos, ImportOrder importOrder){
        List<String> errors = new ArrayList<>();
        List<ImportOrderItem> importOrderItems = new ArrayList<>();
        for (ImportOrderItemCreateDto dto : dtos) {
            ImportOrderItem item = create(dto, importOrder, errors);
            // 保证安全，只添加成功项，反正最后有异常都会抛出
            if(item != null) {
                importOrderItems.add(item);
            }
        }

        if(!errors.isEmpty()){
            throw new BusinessException(ErrorCodeEnum.RULE_STOCK_RUNOUT, String.join("\n", errors));
        }
        log.info("创建进货订单项列表成功，订单编号：{} , 商品数量：{}", importOrder.getImportOrderNo(), importOrderItems.size());
        return importOrderItems;
    }

    // 3.根据进货订单ID查询订单项列表
    public List<ImportOrderItemInfo> findByImportOrderId(Long importOrderId){
        List<ImportOrderItem> importOrderItems = importOrderItemRepository.listByImportOrder(importOrderId);
        return ImportOrderItemConverter.toInfoList(importOrderItems);
    }

    // 4.删除订单项（用于更新覆盖，不会提供删除接口）
    @Transactional
    public void updateDelete(Long importOrderId){
        List<ImportOrderItem> importOrderItems = importOrderItemRepository.listByImportOrder(importOrderId);
        importOrderItemRepository.deleteAll(importOrderItems);
        log.info("更新删除进货订单项成功，进货订单编号：{} , 商品数量：{}", importOrderId, importOrderItems.size());
    }
}
