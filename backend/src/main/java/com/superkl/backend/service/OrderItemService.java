package com.superkl.backend.service;

import com.superkl.backend.converter.OrderItemConverter;
import com.superkl.backend.dto.OrderItemCreateDto;
import com.superkl.backend.entity.Order;
import com.superkl.backend.entity.OrderItem;
import com.superkl.backend.entity.ProductSku;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.OrderItemInfo;
import com.superkl.backend.repository.OrderItemRepository;
import com.superkl.backend.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductSkuRepository productSkuRepository;

    // 1.创建订单项
    public OrderItem create(OrderItemCreateDto dto , Order order){
        // 查找商品SKU是否存在
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode())
                .orElseThrow(() -> new BusinessException("商品SKU不存在"));

        if(!productSku.getProduct().isEnabled()){
            throw new BusinessException("商品已禁用！不可创建订单项！");
        }

        if(!productSku.isEnabled()){
            throw new BusinessException("商品SKU已禁用！不可创建订单项！");
        }
        // 转换为实体
        OrderItem orderItem = OrderItemConverter.toEntity(dto,productSku);
        orderItem.setOrder(order);

        return orderItemRepository.save(orderItem);
    }

    // 2.创建订单项列表
    public List<OrderItem> createList(List<OrderItemCreateDto> dtos, Order order){
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCreateDto dto : dtos) {
            orderItems.add(create(dto,order));
        }
        return orderItems;
    }

    // 3.根据订单ID查询订单项列表
    public List<OrderItemInfo> findByOrderId(Long orderId){
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return OrderItemConverter.toInfoList(orderItems);
    }
}
