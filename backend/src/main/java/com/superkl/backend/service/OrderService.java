package com.superkl.backend.service;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.OrderConverter;
import com.superkl.backend.dto.OrderCreateDto;
import com.superkl.backend.entity.Employee;
import com.superkl.backend.entity.Order;
import com.superkl.backend.entity.OrderItem;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.OrderInfo;
import com.superkl.backend.info.OrderItemInfo;
import com.superkl.backend.info.OrderWithItemsInfo;
import com.superkl.backend.repository.EmployeeRepository;
import com.superkl.backend.repository.OrderRepository;
import com.superkl.backend.repository.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
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
                .orElseThrow(() -> new BusinessException(403, "订单不存在"));
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
                .orElseThrow(() -> new BusinessException(403, "销售员不存在"));
        WareHouse wareHouse = wareHouseRepository.findById(dto.getWareHouseId())
                .orElseThrow(() -> new BusinessException(403, "仓库不存在"));
        // 1.1 检查仓库和销售员状态是否正常
        if(!employee.isEnabled()){
            throw new BusinessException(405, "员工已禁用！不可创建订单！");
        }
        if(!wareHouse.isEnabled()){
            throw new BusinessException(405, "仓库已禁用！不可创建订单！");
        }

        // 1.2 检查员工是否属于该仓库
        Long employeeWareHouseId = employee.getWareHouse().getWareHouseId();
        Long targetWareHouseId = wareHouse.getWareHouseId();

        if(!employeeWareHouseId.equals(targetWareHouseId)){
            throw new BusinessException("员工不属于该仓库！不可创建订单！");
        }

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
        if (f_ActualAmount.compareTo(b_ActualAmount) != 0 || f_TotalAmount.compareTo(b_TotalAmount) != 0) {
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

        log.info("订单{}：{}，金额：{}，员工：{}，仓库：{}",
                orderStatus == OrderStatusEnum.COMPLETED ? "完成" :
                orderStatus == OrderStatusEnum.REFUND ? "退款" : "挂单",
                order.getOrderNo(), b_ActualAmount,
                employee.getEmployeeName(), wareHouse.getWareHouseName());
        RequestUser.log();

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
