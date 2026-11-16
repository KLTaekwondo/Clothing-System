package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.OrderItemConverter;
import com.superkl.backend.dto.order.OrderItemCreateDto;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.entity.order.OrderItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.ErrorCodeEnum;
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
    public OrderItem create(OrderItemCreateDto dto , Order order , DirectionEnum direction, List<String> errors){
        // 查找商品SKU是否存在
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode()).orElse(null);
        if(productSku == null){
            errors.add("商品SKU[" + dto.getSkuCode() + "]不存在！");
            return null;
        }

        String skuCode = productSku.getSkuCode();
        String productName = productSku.getProduct().getProductName();
        String productCode = productSku.getProduct().getProductCode();

        if(!productSku.getProduct().isEnabled()){
            errors.add(productName + "[" + productCode + "]商品已禁用！不可创建订单项！");
        }

        if(!productSku.isEnabled()){
            errors.add(productName + "[" + productCode + "]商品SKU[" + skuCode + "]已禁用！不可创建订单项！");
        }

        // 有错误，不创建订单项，返回null，避免调用日志记录
        if(!errors.isEmpty()) {
            return null;
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
        List<String> errors = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemCreateDto dto : dtos) {
            OrderItem item = create(dto, order, direction, errors);
            // 保证安全，只添加成功项，反正最后有异常都会抛出
            if(item != null) {
                orderItems.add(item);
            }
        }

        if(!errors.isEmpty()){
            throw new BusinessException(ErrorCodeEnum.RULE_ITEM_FORBIDDEN, String.join("\n", errors));
        }

        return orderItems;
    }

    // 3.根据订单ID查询订单项列表
    @Transactional(readOnly = true)
    public List<OrderItemInfo> findByOrderId(Long orderId){
        List<OrderItem> orderItems = orderItemRepository.listByOrder(orderId);
        return OrderItemConverter.toInfoList(orderItems);
    }

    // 4.根据订单号，删除订单项(不会暴露接口，仅用于内部调用)
    @Transactional
    public void updateDelete(Long orderId){
        List<OrderItem> orderItems = orderItemRepository.listByOrder(orderId);
        orderItemRepository.deleteAll(orderItems);
    }
}
