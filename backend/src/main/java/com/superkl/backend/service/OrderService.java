package com.superkl.backend.service;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.OrderConverter;
import com.superkl.backend.dto.OrderCreateDto;
import com.superkl.backend.entity.Employee;
import com.superkl.backend.entity.Order;
import com.superkl.backend.entity.OrderItem;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.enums.DirectionEnum;
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

    // 1.挂单操作
    @Transactional
    public void draft(OrderCreateDto dto) {
        Order order = OrderConverter.toEntity(dto);
        applyOrder(dto,order);
        order.setStatus(OrderStatusEnum.DRAFT);
        orderRepository.save(order);

    }

    // 2.完成订单
    @Transactional
    public void complete(OrderCreateDto dto) {
        // 先获取订单是否存在
        Long orderId = dto.getOrderId();
        Order order;

        // 情况一：新订单
        if(orderId == null) {
            // 1.创建新订单
            order = OrderConverter.toEntity(dto);

            // 2.创建新订单项
            applyOrder(dto,order);

            // 3.处理库存
            Long wareHouseId = dto.getWareHouseId();
            List<OrderItem> items = order.getOrderItems().stream().toList();
            stockManage(items,wareHouseId);

            // 4.设置为已完成状态
            order.setStatus(OrderStatusEnum.COMPLETED);

        } else {
            // 情况二：已保存订单
            // 1.校验订单是否存在
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(403, "订单不存在"));

            // 2.校验订单状态是否为草稿
            if(!order.isDraft()){
                throw new BusinessException(403, "订单不是草稿状态，不可完成！");
            }

            // 3.删除旧订单项，并添加新的订单项
            orderItemService.updateDelete(orderId);
            applyOrder(dto,order);

            // 4.处理库存
            Long wareHouseId = dto.getWareHouseId();
            List<OrderItem> items = order.getOrderItems().stream().toList();
            stockManage(items,wareHouseId);

            // 5.设置为已完成状态
            order.setStatus(OrderStatusEnum.COMPLETED);
        }

        // 6.保存订单
        orderRepository.save(order);
    }

    // 3.更新草稿订单
    @Transactional
    public void update(Long orderId , OrderCreateDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(403, "订单不存在"));
        if(!order.isDraft()){
            throw new BusinessException(403, "订单不是草稿状态，不可更新！");
        }
        orderItemService.updateDelete(orderId);
        applyOrder(dto,order);
        orderRepository.save(order);
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


    // 6.库存管理
    private void applyOrder(OrderCreateDto dto ,Order order) {
        // 1.校验仓库和销售员是否存在
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
        // 2.1 先检查是否有商品项为空
        boolean isEmpty = dto.getSaleItems().isEmpty() && dto.getRefundItems().isEmpty();
        if(isEmpty){
            throw new BusinessException("订单项不能为空");
        }
        List<OrderItem> saleItems = orderItemService.createList(dto.getSaleItems(), order , DirectionEnum.IN);
        List<OrderItem> refundItems = orderItemService.createList(dto.getRefundItems(), order, DirectionEnum.OUT);
        // 3.校验金额
        // 3.1 前端提交的金额
        BigDecimal f_ActualAmount = dto.getActualAmount();
        BigDecimal f_TotalAmount = dto.getTotalAmount();

        // 3.2 后端计算的订单项金额
        BigDecimal b_ActualAmount = BigDecimal.ZERO;
        BigDecimal b_TotalAmount = BigDecimal.ZERO;

        // 卖出去的总计金额
        for (OrderItem item : saleItems) {
            b_ActualAmount = b_ActualAmount.add(item.getActualPrice());
            b_TotalAmount = b_TotalAmount.add(item.getTotalPrice());
        }
        // 退款的总计金额
        for (OrderItem item : refundItems) {
            b_ActualAmount = b_ActualAmount.subtract(item.getActualPrice());
            b_TotalAmount = b_TotalAmount.subtract(item.getTotalPrice());
        }


        // 校验金额是否一致
        if (f_ActualAmount.compareTo(b_ActualAmount) != 0 || f_TotalAmount.compareTo(b_TotalAmount) != 0) {
            throw new BusinessException("订单金额与商品项金额不一致");
        }

        // 整合订单项
        Set<OrderItem> items = new HashSet<>();
        items.addAll(saleItems);
        items.addAll(refundItems);

        // 4.保存订单
        order.setActualPrice(b_ActualAmount);
        order.setTotalPrice(b_TotalAmount);
        order.setOrderItems(items);
        order.setEmployee(employee);
        order.setWareHouse(wareHouse);
    }

    // 库存管理
    private void stockManage(List<OrderItem> orderItems, Long wareHouseId) {
        for (OrderItem orderItem : orderItems) {
            if(DirectionEnum.IN.equals(orderItem.getDirection())) {
                // 出售商品（正向业务），更新库存状态
                wareHouseStockService.decreaseStock(wareHouseId, orderItem.getSkuId(), orderItem.getQuantity());
            }

            if(DirectionEnum.OUT.equals(orderItem.getDirection())) {
                // 退货商品（反向业务），更新库存状态
                wareHouseStockService.increaseStock(wareHouseId, orderItem.getSkuId(), orderItem.getQuantity());
            }
        }
    }
}
