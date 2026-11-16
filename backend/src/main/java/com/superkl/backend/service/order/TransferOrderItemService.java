package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.TransferOrderItemConverter;
import com.superkl.backend.dto.order.TransferOrderItemCreateDto;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.order.TransferOrder;
import com.superkl.backend.entity.order.TransferOrderItem;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.TransferOrderItemInfo;
import com.superkl.backend.repository.product.ProductSkuRepository;
import com.superkl.backend.repository.order.TransferOrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferOrderItemService {
    private final TransferOrderItemRepository transferOrderItemRepository;
    private final ProductSkuRepository productSkuRepository;

    // 新增
    public TransferOrderItem create(TransferOrderItemCreateDto dto , TransferOrder transferOrder, List<String> errors) {
        // 1. 校验商品SKU是否存在
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode()).orElse(null);
        if(productSku == null){
            errors.add("商品SKU[" + dto.getSkuCode() + "]不存在！");
            return null;
        }

        // 2. 校验商品状态是否正常
        String productCode = productSku.getProduct().getProductCode();
        String productName = productSku.getProduct().getProductName();
        String skuCode = productSku.getSkuCode();

        if(!productSku.getProduct().isEnabled()){
            errors.add(productName + "[" + productCode + "]商品已被禁用，请检查后再试");
        }
        if(!productSku.isEnabled()){
            errors.add(productName + "[" + productCode + "]商品sku[" + skuCode + "]已被禁用，请检查后再试");
        }

        // 存在错误直接返回null
        if(!errors.isEmpty()){
            return null;
        }

        TransferOrderItem item = TransferOrderItemConverter.toEntity(dto,productSku);
        item.setTransferOrder(transferOrder);
        log.info("新增转移订单项：编码：{}，名称：{}，数量：{}，总价：{}", productCode, productName, dto.getQuantity(), item.getItemTotalPrice());
        return item;
    }

    // 新增列表
    public List<TransferOrderItem> createList(List<TransferOrderItemCreateDto> dtos, TransferOrder transferOrder) {
        List<String> errors = new ArrayList<>();
        List<TransferOrderItem> list = new ArrayList<>();
        for (TransferOrderItemCreateDto dto : dtos) {
            TransferOrderItem item = create(dto, transferOrder, errors);
            // 保证安全，只添加成功项，反正最后有异常都会抛出
            if(item != null) {
                list.add(item);
            }
        }

        if(!errors.isEmpty()){
            throw new BusinessException(ErrorCodeEnum.RULE_ITEM_FORBIDDEN, String.join("\n", errors));
        }

        return list;
    }

    // 查找转移订单号下的订单项列表
    @Transactional(readOnly = true)
    public List<TransferOrderItemInfo> findByTransferOrderId(Long transferOrderId) {
        List<TransferOrderItem> list = transferOrderItemRepository.listByTransferOrder(transferOrderId);
        return TransferOrderItemConverter.toInfoList(list);
    }

    // 更新删除(只用于内部使用，不允许外部调用)
    @Transactional
    public void updateDelete(Long transferOrderId) {
        List<TransferOrderItem> list = transferOrderItemRepository.listByTransferOrder(transferOrderId);
        transferOrderItemRepository.deleteAll(list);
    }
}
