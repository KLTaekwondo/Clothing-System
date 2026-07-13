package com.superkl.backend.service;

import com.superkl.backend.converter.OrderConverter;
import com.superkl.backend.dto.OrderCreateDto;
import com.superkl.backend.entity.Employee;
import com.superkl.backend.entity.Order;
import com.superkl.backend.entity.OrderItem;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.OrderInfo;
import com.superkl.backend.info.OrderItemInfo;
import com.superkl.backend.info.OrderWithItemsInfo;
import com.superkl.backend.repository.EmployeeRepository;
import com.superkl.backend.repository.OrderRepository;
import com.superkl.backend.repository.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final EmployeeRepository employeeRepository;
    private final WareHouseRepository wareHouseRepository;
    private final WareHouseStockService wareHouseStockService;


    // 1.完成订单
    @Transactional
    public void complete(OrderCreateDto dto) {
        saveOrder(dto,OrderStatusEnum.COMPLETED);
    }

    // 2.挂单订单
    @Transactional
    public void draft(OrderCreateDto dto) {
        saveOrder(dto,OrderStatusEnum.DRAFT);
    }

    // 3.退货订单
    @Transactional
    public void refund(OrderCreateDto dto) {
        saveOrder(dto,OrderStatusEnum.REFUND);
    }

    // 4.查询单个订单
    @Transactional
    public OrderWithItemsInfo search(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        List<OrderItemInfo> items = orderItemService.findByOrderId(orderId);
        return OrderConverter.toInfoWithItems(order,items);
    }

    // 5.查询订单订单列表
    @Transactional
    public List<OrderInfo> searchList() {
        List<Order> orders = orderRepository.findAll();
        return OrderConverter.toInfoList(orders);
    }



    private void saveOrder(OrderCreateDto dto,OrderStatusEnum orderStatus) {
        // 1.创建订单本体，同时校验仓库和销售员是否存在
        Order order = OrderConverter.toEntity(dto);
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new BusinessException("销售员不存在"));
        WareHouse wareHouse = wareHouseRepository.findById(dto.getWareHouseId())
                .orElseThrow(() -> new BusinessException("仓库不存在"));
        // 2.创建商品项
        List<OrderItem> items = orderItemService.createList(dto.getOrderItems(), order);
        // 3.校验金额
        // 3.1 前端提交的金额
        BigDecimal f_ActualAmount = dto.getActualAmount();
        BigDecimal f_TotalAmount = dto.getTotalAmount();

        // 3.2 后端计算的订单项金额
        BigDecimal b_ActualAmount = BigDecimal.ZERO;
        BigDecimal b_TotalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            b_ActualAmount = b_ActualAmount.add(item.getActualPrice());
            b_TotalAmount = b_TotalAmount.add(item.getTotalPrice());
        }
        // 校验金额是否一致
        if (!f_ActualAmount.equals(b_ActualAmount) || !f_TotalAmount.equals(b_TotalAmount)) {
            throw new BusinessException("订单金额与商品项金额不一致");
        }

        // 4.保存订单
        order.setActualPrice(b_ActualAmount);
        order.setTotalPrice(b_TotalAmount);
        order.setOrderItems(new HashSet<>(items));
        order.setEmployee(employee);
        order.setWareHouse(wareHouse);
        order.setStatus(orderStatus);
        orderRepository.save(order);

        // 5.更新库存
        for (OrderItem item : items) {
            if(orderStatus.equals(OrderStatusEnum.DRAFT)) {
                continue;
            }

            if(orderStatus.equals(OrderStatusEnum.REFUND)) {
                wareHouseStockService.increaseStock(wareHouse.getWareHouseId(), item.getSkuId(), item.getQuantity());
            }

            if(orderStatus.equals(OrderStatusEnum.COMPLETED)) {
                wareHouseStockService.decreaseStock(wareHouse.getWareHouseId(), item.getSkuId(), item.getQuantity());
            }
        }
    }
}
