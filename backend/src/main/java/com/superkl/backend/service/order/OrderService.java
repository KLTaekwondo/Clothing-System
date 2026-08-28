package com.superkl.backend.service.order;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.order.OrderConverter;
import com.superkl.backend.dto.order.OrderCreateDto;
import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.entity.basic.Member;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.entity.order.OrderItem;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.OrderInfo;
import com.superkl.backend.info.order.OrderItemInfo;
import com.superkl.backend.info.order.OrderWithItemsInfo;
import com.superkl.backend.repository.basic.EmployeeRepository;
import com.superkl.backend.repository.order.OrderRepository;
import com.superkl.backend.repository.basic.WareHouseRepository;
import com.superkl.backend.service.basic.MemberService;
import com.superkl.backend.service.dash.CacheDashService;
import com.superkl.backend.service.stock.WareHouseStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MemberService memberService;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final EmployeeRepository employeeRepository;
    private final WareHouseRepository wareHouseRepository;
    private final WareHouseStockService wareHouseStockService;
    private final CacheDashService cacheDashService;

    // 1.挂单操作
    @Transactional
    public void draft(OrderCreateDto dto) {
        // 检查权限
        checkPermission(dto.getWareHouseId());

        // 挂单
        Order order = OrderConverter.toEntity();
        applyOrder(dto,order);
        order.setStatus(OrderStatusEnum.DRAFT);
        orderRepository.save(order);
        // 日志记录
        RequestUser.log();
        log.info("挂单订单，订单编号：{} ，订单总价：{} ，数量：{}",
                order.getOrderNo(),order.getTotalPrice(),order.getOrderItems().size());
    }

    // 2.完成订单
    @Transactional
    public String complete(OrderCreateDto dto) {
        // 第一步权限校验
        checkPermission(dto.getWareHouseId());
        // 先获取订单是否存在
        Long orderId = dto.getOrderId();
        Order order;

        // 情况一：新订单
        if(orderId == null) {
            // 1.创建新订单
            order = OrderConverter.toEntity();

            // 2.创建新订单项
            applyOrder(dto,order);

            // 3.处理库存
            Long wareHouseId = order.getWareHouse().getWareHouseId();
            List<OrderItem> items = order.getOrderItems().stream().toList();
            StockContext context = StockContext.builder()
                    .sourceNo(order.getOrderNo())
                    .build();
            stockManage(items,wareHouseId,context);

            // 4.设置为已完成状态
            order.setStatus(OrderStatusEnum.COMPLETED);

        } else {
            // 情况二：已保存订单
            // 1.校验订单是否存在
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(403, "订单不存在"));
            checkBelongs(order.getWareHouse().getWareHouseId());
            // 2.校验订单状态是否为草稿
            if(!order.isDraft()){
                throw new BusinessException(403, "订单不是草稿状态，不可完成！");
            }

            // 3.删除旧订单项，并添加新的订单项
            orderItemService.updateDelete(orderId);
            applyOrder(dto,order);

            // 4.处理库存
            Long wareHouseId = order.getWareHouse().getWareHouseId();
            List<OrderItem> items = order.getOrderItems().stream().toList();
            StockContext context = StockContext.builder()
                    .sourceNo(order.getOrderNo())
                    .build();
            stockManage(items,wareHouseId,context);

            // 5.设置为已完成状态
            order.setStatus(OrderStatusEnum.COMPLETED);
        }

        // 6.保存订单
        orderRepository.save(order);
        // 7.清除缓存（在 save 之后，此时 createTime 已自动生成）
        clearCache(LocalDate.now());
        // 8.处理用户积分
        // 如果不是空，才处理会员积分，否则不处理
        if(order.getMemberPhone() != null && !order.getMemberPhone().isBlank()) {
            // 先拿取会员信息，避免链式调用过长
            Member member = memberService.getFromPhone(order.getMemberPhone());
            Long memberId = member.getMemberId();
            // 如果订单总价大于0，才处理会员积分
            if(order.getActualPrice().compareTo(BigDecimal.ZERO) > 0) {
                Integer points = order.getActualPrice().multiply(BigDecimal.valueOf(100)).intValueExact();
                memberService.increasePoints(memberId, points);
            }

            if(order.getActualPrice().compareTo(BigDecimal.ZERO) < 0){
                Integer points = order.getActualPrice().multiply(BigDecimal.valueOf(-100)).intValueExact();
                memberService.decreasePoints(memberId, points);
            }
        }

        // 日志记录
        RequestUser.log();
        log.info("完成订单，订单编号：{} ，订单总价：{} ，数量：{}",
                order.getOrderNo(),order.getActualPrice(),order.getOrderItems().size());
        return order.getOrderNo();
    }

    // 3.更新草稿订单
    @Transactional
    public void update(OrderCreateDto dto) {
        // 第一步权限校验
        checkPermission(dto.getWareHouseId());

        Long orderId = dto.getOrderId();
        if(orderId == null){
            throw new BusinessException(403, "订单ID为空！无法更新！");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(403, "订单不存在"));

        checkBelongs(order.getWareHouse().getWareHouseId());

        if(!order.isDraft()){
            throw new BusinessException(403, "订单不是草稿状态，不可更新！");
        }
        orderItemService.updateDelete(orderId);
        applyOrder(dto,order);
        orderRepository.save(order);

        // 日志记录
        RequestUser.log();
        log.info("更新订单，订单编号：{} ，订单总价：{} ，数量：{}",
                order.getOrderNo(),order.getTotalPrice(),order.getOrderItems().size());
    }

    // 4.查询单个订单
    @Transactional(readOnly = true)
    public OrderWithItemsInfo search(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(403, "订单不存在"));
        checkBelongs(order.getWareHouse().getWareHouseId());
        List<OrderItemInfo> items = orderItemService.findByOrderId(orderId);
        return OrderConverter.toInfoWithItems(order,items);
    }

    // 5.查询订单订单列表
    @Transactional(readOnly = true)
    public PageResult<OrderInfo> searchPage(Pageable pageable) {
        return OrderConverter.toInfoPage(orderRepository.findPage(pageable));
    }

    // 6.删除草稿订单
    @Transactional
    public void deleteDraft(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(403, "订单不存在"));
        checkBelongs(order.getWareHouse().getWareHouseId());
        if(!order.isDraft()){
            throw new BusinessException(403, "订单不是草稿状态，不可删除！");
        }
        orderRepository.delete(order);
        // 日志记录
        RequestUser.log();
        log.info("删除订单，订单编号：{}", order.getOrderNo());
    }

    // 7.查询当前仓库的挂单列表
    @Transactional(readOnly = true)
    public PageResult<OrderInfo> searchDraftByWareHouseId(Pageable pageable) {
        Long wareHouseId = RequestUser.notNull().getRequestId();
        Page<Order> orders = orderRepository.findStatusByWareHouseId(wareHouseId,OrderStatusEnum.DRAFT,pageable);
        return OrderConverter.toInfoPage(orders);
    }

    // 8.查询当前仓库的完成订单列表
    @Transactional(readOnly = true)
    public PageResult<OrderInfo> searchCompletePageByWareHouseId(LocalDateTime startTime,
                                                                 LocalDateTime endTime,
                                                                 Long employeeId,
                                                                 Pageable pageable) {
        Long wareHouseId = RequestUser.notNull().getRequestId();
        Page<Order> orders = orderRepository.findPageByTimeAndWId(startTime, endTime,
                OrderStatusEnum.COMPLETED,employeeId,wareHouseId,pageable);
        return OrderConverter.toInfoPage(orders);
    }

    // 8.处理订单项
    private void applyOrder(OrderCreateDto dto ,Order order) {
        // 1.校验仓库和销售员是否存在
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new BusinessException(403, "销售员不存在"));
        WareHouse wareHouse = wareHouseRepository.findById(dto.getWareHouseId())
                .orElseThrow(() -> new BusinessException(403, "仓库不存在"));
        // 1.1 检查仓库和销售员状态是否正常
        if(!employee.isEnabled()){
            throw new BusinessException(405, employee.getEmployeeName()+"员工已禁用！不可创建订单！");
        }
        if(!wareHouse.isEnabled()){
            throw new BusinessException(405, wareHouse.getWareHouseName()+"仓库已禁用！不可创建订单！");
        }

        // 1.2 检查员工是否属于该仓库
        Long employeeWareHouseId = employee.getWareHouse().getWareHouseId();
        Long targetWareHouseId = wareHouse.getWareHouseId();

        if(!employeeWareHouseId.equals(targetWareHouseId)){
            throw new BusinessException(405, employee.getEmployeeName()+"员工不属于"+wareHouse.getWareHouseName()+"仓库！不可创建订单！");
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
        order.setPayMethod(dto.getPayMethod());
        order.setRemark(dto.getRemark());
        order.setActualPrice(b_ActualAmount);
        order.setTotalPrice(b_TotalAmount);
        order.setOrderItems(items);
        order.setEmployee(employee);
        order.setWareHouse(wareHouse);
        order.setMemberPhone(dto.getMemberPhone());
    }

    // 库存管理
    private void stockManage(List<OrderItem> orderItems, Long wareHouseId , StockContext context) {
        for (OrderItem orderItem : orderItems) {
            if(DirectionEnum.IN.equals(orderItem.getDirection())) {
                // 出售商品（正向业务），更新库存状态
                context.setChangeType(StockChangeTypeEnum.SALE_OUT);
                context.setSourceType(StockSourceTypeEnum.ORDER);
                wareHouseStockService.decreaseStock(wareHouseId, orderItem.getSkuId(), orderItem.getQuantity(),context);
            }

            if(DirectionEnum.OUT.equals(orderItem.getDirection())) {
                // 退货商品（反向业务），更新库存状态
                context.setChangeType(StockChangeTypeEnum.SALE_RETURN);
                context.setSourceType(StockSourceTypeEnum.ORDER);
                wareHouseStockService.increaseStock(wareHouseId, orderItem.getSkuId(), orderItem.getQuantity(),context);
            }
        }
    }

    // 权限管理
    private void checkPermission(Long wareHouseId) {
        if(!RequestUser.isAdmin() && !RequestUser.isCurrentWareHouse(wareHouseId)) {
            throw new BusinessException(403, "您没有权限操作该订单！");
        }
    }

    // 检查所属权
    private void checkBelongs(Long wareHouseId) {
        if(!RequestUser.isAdmin() && !RequestUser.isCurrentWareHouse(wareHouseId)) {
            throw new BusinessException(403, "订单所属仓库与当前用户不一致！");
        }
    }

    // 清除缓存
    private void clearCache(LocalDate day) {
        cacheDashService.clearCache(day);
    }
}
