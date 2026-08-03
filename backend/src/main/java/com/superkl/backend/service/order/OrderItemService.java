package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.OrderItemConverter;
import com.superkl.backend.dto.order.OrderItemCreateDto;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.entity.order.OrderItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.OrderItemInfo;
import com.superkl.backend.repository.order.OrderItemRepository;
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
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductSkuRepository productSkuRepository;

    // 1.创建订单项
    public OrderItem create(OrderItemCreateDto dto , Order order , DirectionEnum direction){
        // 查找商品SKU是否存在
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode())
                .orElseThrow(() -> new BusinessException(403, "商品SKU不存在"));

        if(!productSku.getProduct().isEnabled()){
            throw new BusinessException(405, "商品已禁用！不可创建订单项！");
        }

        if(!productSku.isEnabled()){
            throw new BusinessException(405, "商品SKU已禁用！不可创建订单项！");
        }
        // 转换为实体
        OrderItem orderItem = OrderItemConverter.toEntity(dto,productSku);
        orderItem.setOrder(order);
        orderItem.setDirection(direction);

        // 记录，同时返回即可，等到订单自动保存
        log.debug("创建订单项：SKU={}，数量={}，金额={}", dto.getSkuCode(), dto.getQuantity(), orderItem.getActualPrice());
        return orderItem;
    }

    // 2.创建订单项列表
    public List<OrderItem> createList(List<OrderItemCreateDto> dtos, Order order , DirectionEnum direction){
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCreateDto dto : dtos) {
            orderItems.add(create(dto,order,direction));
        }
        return orderItems;
    }

    // 3.根据订单ID查询订单项列表
    @Transactional(readOnly = true)
    public List<OrderItemInfo> findByOrderId(Long orderId){
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return OrderItemConverter.toInfoList(orderItems);
    }

    // 4.根据订单号，删除订单项(不会暴露接口，仅用于内部调用)
    @Transactional
    public void updateDelete(Long orderId){
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItemRepository.deleteAll(orderItems);
    }
}
