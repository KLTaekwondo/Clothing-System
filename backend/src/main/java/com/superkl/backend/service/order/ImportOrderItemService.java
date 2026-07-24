package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.ImportOrderItemConverter;
import com.superkl.backend.dto.order.ImportOrderItemCreateDto;
import com.superkl.backend.entity.order.ImportOrder;
import com.superkl.backend.entity.order.ImportOrderItem;
import com.superkl.backend.entity.product.ProductSku;
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
    @Transactional
    public ImportOrderItem create(ImportOrderItemCreateDto dto, ImportOrder importOrder){
        // 查找商品SKU是否存在
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode())
                .orElseThrow(() -> new BusinessException(403, "商品SKU不存在"));

        if(!productSku.getProduct().isEnabled()){
            throw new BusinessException(405, "商品已禁用！请检查后再操作！");
        }

        if(!productSku.isEnabled()){
            throw new BusinessException(405, "商品SKU已禁用！请检查后再操作！");
        }
        // 转换为实体
        ImportOrderItem importOrderItem = ImportOrderItemConverter.toEntity(dto,productSku);
        importOrderItem.setImportOrder(importOrder);

        log.info("创建进货订单项：SKU={}，数量={}，金额={}", dto.getSkuCode(), dto.getImportQuantity(), importOrderItem.getTotalPrice());
        return importOrderItem;
    }

    // 2.创建订单项列表
    @Transactional
    public List<ImportOrderItem> createList(List<ImportOrderItemCreateDto> dtos, ImportOrder importOrder){
        List<ImportOrderItem> importOrderItems = new ArrayList<>();
        for (ImportOrderItemCreateDto dto : dtos) {
            importOrderItems.add(create(dto,importOrder));
        }
        log.info("创建进货订单项列表成功，订单编号：{} , 商品数量：{}", importOrder.getImportOrderNo(), importOrderItems.size());
        return importOrderItems;
    }

    // 3.根据进货订单ID查询订单项列表
    public List<ImportOrderItemInfo> findByImportOrderId(Long importOrderId){
        List<ImportOrderItem> importOrderItems = importOrderItemRepository.findByImportOrderId(importOrderId);
        return ImportOrderItemConverter.toInfoList(importOrderItems);
    }

    // 4.删除订单项（用于更新覆盖，不会提供删除接口）
    @Transactional
    public void updateDelete(Long importOrderId){
        List<ImportOrderItem> importOrderItems = importOrderItemRepository.findByImportOrderId(importOrderId);
        importOrderItemRepository.deleteAll(importOrderItems);
        log.info("更新删除进货订单项成功，进货订单编号：{} , 商品数量：{}", importOrderId, importOrderItems.size());
    }
}
