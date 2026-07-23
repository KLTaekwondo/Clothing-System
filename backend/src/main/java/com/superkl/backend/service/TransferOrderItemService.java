package com.superkl.backend.service;

import com.superkl.backend.converter.TransferOrderItemConverter;
import com.superkl.backend.dto.TransferOrderItemCreateDto;
import com.superkl.backend.entity.ProductSku;
import com.superkl.backend.entity.TransferOrder;
import com.superkl.backend.entity.TransferOrderItem;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.TransferOrderItemInfo;
import com.superkl.backend.repository.ProductSkuRepository;
import com.superkl.backend.repository.TransferOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferOrderItemService {
    private final TransferOrderItemRepository transferOrderItemRepository;
    private final ProductSkuRepository productSkuRepository;

    // 新增
    public TransferOrderItem create(TransferOrderItemCreateDto dto , TransferOrder transferOrder) {
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode())
                .orElseThrow(()-> new BusinessException(403,"商品sku不存在！"));
        if(!productSku.getProduct().isEnabled()){
            throw new BusinessException(405,"商品已被禁用，请检查后再试");
        }
        if(!productSku.isEnabled()){
            throw new BusinessException(405,"商品sku已被禁用，请检查后再试");
        }
        TransferOrderItem item = TransferOrderItemConverter.toEntity(dto,productSku);
        item.setTransferOrder(transferOrder);
        return item;
    }

    // 新增列表
    public List<TransferOrderItem> createList(List<TransferOrderItemCreateDto> dtos, TransferOrder transferOrder) {
        List<TransferOrderItem> list = new ArrayList<>();
        for (TransferOrderItemCreateDto dto : dtos) {
            TransferOrderItem item = create(dto, transferOrder);
            list.add(item);
        }
        return list;
    }

    // 查找转移订单号下的订单项列表
    public List<TransferOrderItemInfo> findByTransferOrderId(Long transferOrderId) {
        List<TransferOrderItem> list = transferOrderItemRepository.findByTransferOrderId(transferOrderId);
        return TransferOrderItemConverter.toInfoList(list);
    }

    // 更新删除(只用于内部使用，不允许外部调用)
    public void updateDelete(Long transferOrderId) {
        List<TransferOrderItem> list = transferOrderItemRepository.findByTransferOrderId(transferOrderId);
        transferOrderItemRepository.deleteAll(list);
    }
}
